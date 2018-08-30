package org.codefeedr.configuration


import org.codefeedr.util.OptionExtensions.DebuggableOption
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * This component handles the global configuration
  */

trait ConfigurationProviderComponent {
  val configurationProvider: ConfigurationProvider


  trait ConfigurationProvider extends Serializable {
    /**
      * Sets the configuration
      *
      * @param configuration custom configuration to initialize with
      */
    def initConfiguration(configuration: ParameterTool): Unit

    /**
      * Retrieve the parameterTool for the global configuration
      *
      * @return
      */
    def parameterTool: ParameterTool

    /**
      * Tries to key the value for the given key from the parametertool
      *
      * @param key key value to search for
      * @return value if the key exists in the parameter tool, Otherwise None
      */
    def tryGet(key: String): Option[String]

    /**
      * Returns the value of the given key
      * If no default value is passed and the key does not exist, an exception is thrown
      *
      * @param key     key to look for in the configuration
      * @param default default value
      * @return
      */
    def get(key: String, default: Option[String] = None): String

    /**
      * Returns the value of the given key
      * If no default value is passed and the key does not exist, an exception is thrown
      *
      * @param key     key to look for in the configuration
      * @param default default value
      * @return
      */
    def getInt(key: String, default: Option[Int] = None): Int =
      get(key, default.map(o => o.toString)).toInt
  }
}

trait FlinkConfigurationProviderComponent extends ConfigurationProviderComponent {
  val configurationProvider:ConfigurationProvider = new ConfigurationProviderImpl()
  class ConfigurationProviderImpl
    extends ConfigurationProvider
    with LazyLogging
  {
    //All these variables are transient, because the configuration provider should
    // re-initialize after deserialization, and use the configuration from the execution environment
    @transient @volatile private var requested = false
    @transient @volatile private var _parameterTool: Option[ParameterTool] = None
    @transient lazy val parameterTool: ParameterTool = getParameterTool

    /**
      * Sets the configuration
      *
      * @param configuration custom configuration to initialzie with
      */
    def initConfiguration(configuration: ParameterTool) {
      if (requested) {
        //Just to validate, the configuration is not modified after it has already been retrieved by some component
        throw new IllegalStateException("Cannot set parametertool after parametertool was already requested")
      }

      setJobParameters(configuration)

      //Store parameter tool in the static context
      //Needed to also make the components work when there is no stream execution environment
      logger.debug("Setting parameter tool")
      _parameterTool = Some(configuration)

    }


    /**
      * Sets the parametertool as jobParameter
      *
      * @param pt parametertool to set
      */
    private def setJobParameters(pt: ParameterTool): Unit = {
      try {
        val env = ExecutionEnvironment.getExecutionEnvironment
        env.getConfig.setGlobalJobParameters(pt)
      } catch {
        case e: Exception => logger.warn(s"Cannot retrieve execution environment. Did you use this configuration provider outside a job? Error: ${e.getMessage}", e)
      }
    }


    /**
      * Attempts to load the codefeedr.properties file
      *
      * @return A paremetertool if the file was found, none otherwise
      */
    private def loadPropertiesFile(): Option[ParameterTool] =
      Option(getClass.getResourceAsStream("/codefeedr.properties"))
        .info("Loading codefeedr.properties file", "No codefeedr.properties resource file found.")
        .map(ParameterTool.fromPropertiesFile)


    /**
      * Loads the parametertool from the execution environment
      *
      * @return the parametertool
      */
    private def loadEnvironment(): ParameterTool = {
      logger.debug("Retrieving parameters from the execution environment")
      val env = ExecutionEnvironment.getExecutionEnvironment
      ParameterTool.fromMap(env.getConfig.getGlobalJobParameters.toMap)
    }


    /**
      * Reads the parameterTool from the codefeedr.properties file, execution environment, or the explicitly initialized parametertool
      * Makes sure that if the properties file was used, the properties are also registered as jobParameters
      *
      * @return
      */
    private def getParameterTool: ParameterTool = {
      requested = true
      val pt = _parameterTool.getOrElse[ParameterTool]({
        loadPropertiesFile() match {
          case Some(pt) => {
            setJobParameters(pt)
            pt
          }
          case None => {
            logger.debug("Retrieving parameters from the execution environment")
            val jobParams = Option(ExecutionEnvironment.getExecutionEnvironment.getConfig.getGlobalJobParameters)
            jobParams match {
              case Some(jp) => ParameterTool.fromMap(jp.toMap)
              case None => throw new IllegalArgumentException("Cannot retrieve job parameters because no global job parameters were set. Did you forget to set a \"codefeedr.properties\" file when running outside of the Flink execution environment?")
            }
          }
        }
      })
      //TODO: Maybe only print this in debug mode? This might expose username/password
      logger.info(s"Configuration initialized with values: \n ${pt.toMap.asScala.toString}")
      pt
    }

    /**
      * Tries to key the value for the given key from the parametertool
      *
      * @param key key value to search for
      * @return value if the key exists in the parameter tool, Otherwise None
      */
    def tryGet(key: String): Option[String] = {
      if (parameterTool.has(key)) {
        Some(parameterTool.get(key))
      } else {
        None
      }
    }

    /**
      * @param key     key to look for in the configuration
      * @param default default value
      * @return
      */
    override def get(key: String, default: Option[String]): String = {
      tryGet(key) match {
        case None => default match {
          case None => throw new IllegalArgumentException(s"Cannot find a configuration for $key, and no default value was passed")
          case Some(v) => v
        }
        case Some(v) => v
      }
    }
  }
}

