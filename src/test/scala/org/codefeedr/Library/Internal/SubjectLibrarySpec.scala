package org.codefeedr.Library.Internal

import org.codefeedr.Library.SubjectLibrary
import org.scalatest._

import scala.concurrent.ExecutionContextExecutor

case class TestTypeA(prop1: String)

/**
  * Created by Niels on 18/07/2017.
  */
class SubjectLibrarySpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {
  implicit override def executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.Implicits.global

  "A KafkaLibrary" should "be able to register a new type" in {
    for {
      subject <- SubjectLibrary.GetType[TestTypeA]()
    } yield assert(subject.properties.map(o => o.name).contains("prop1"))
  }

  "A KafkaLibrary" should "list all current registered types" in {
    assert(SubjectLibrary.GetSubjectNames().contains("TestTypeA"))
  }

  "A KafkaLibrary" should "be able to remove a registered type again" in {
    for {
      _ <- SubjectLibrary.UnRegisterSubject("TestTypeA")
    } yield assert(!SubjectLibrary.GetSubjectNames().contains("TestTypeA"))
  }

  "A KafkaLibrary" should "return the same subjecttype if GetType is called twice in parallel" in {
    val t1 = SubjectLibrary.GetType[TestTypeA]()
    val t2 = SubjectLibrary.GetType[TestTypeA]()
    for {
      r1 <- t1
      r2 <- t2
    } yield assert(r1.uuid == r2.uuid)
  }

  "A KafkaLibrary" should "return the same subjecttype if GetType is called twice sequential" in {
    for {
      r1 <- SubjectLibrary.GetType[TestTypeA]()
      r2 <- SubjectLibrary.GetType[TestTypeA]()
    } yield assert(r1.uuid == r2.uuid)
  }
}