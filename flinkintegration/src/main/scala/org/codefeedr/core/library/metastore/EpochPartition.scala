package org.codefeedr.core.library.metastore

import org.codefeedr.core.library.internal.zookeeper.{ZkClient, ZkNode, ZkNodeBase, ZkStateNode}
import org.codefeedr.model.zookeeper.Partition

import scala.reflect.ClassTag

class EpochPartition(name: String, parent: ZkNodeBase)(implicit override val zkClient: ZkClient)
    extends ZkNode[Partition](name, parent)
    with ZkStateNode[Partition, Boolean] {
  override def typeT(): ClassTag[Boolean] = ClassTag(classOf[Boolean])
  override def initialState(): Boolean = false
}
