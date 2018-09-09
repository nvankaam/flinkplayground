package org.codefeedr.core.library.metastore

import org.codefeedr.core.library.internal.zookeeper._
import org.codefeedr.model.zookeeper.Consumer

class ConsumerCollection(subjectName: String, parent: ZkNodeBase)(implicit override val zkClient: ZkClient)
    extends ZkCollectionNode[ConsumerNode, Unit]("consumers",
                                                 parent,
                                                 (name, parent) => new ConsumerNode(name, parent))
    with ZkCollectionStateNode[ConsumerNode, Unit, Consumer, Boolean, Boolean] {

  override def initial(): Boolean = false
  override def mapChild(child: Boolean): Boolean = child
  override def reduceAggregate(left: Boolean, right: Boolean): Boolean = left || right
  def querySource(): QuerySourceNode = parent().asInstanceOf[QuerySourceNode]
}
