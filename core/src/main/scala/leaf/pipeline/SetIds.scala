package leaf.pipeline

import leaf._

object SetIds {
  def generateId(node: Node[_]): String = {
    val text = node.byType[NodeType.Text].map(_.tpe.text).mkString("")

    text.map {
      case c if c.isLetterOrDigit => c
      case _                      => '-'
    }.toLowerCase
  }

  /** If a child level does not contain any ID node, insert one */
  def convert[T <: NodeType](node: Node[T]): Node[T] =
    node.tpe match {
      case _: NodeType.ChildLevel =>
        if (node.children.exists(_.tpe.isInstanceOf[NodeType.Id])) node
        else
          node.copy(
            children = node.children :+ Node(NodeType.Id(generateId(node)))
          )
      case _ => node
    }
}
