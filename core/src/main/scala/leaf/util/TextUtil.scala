package leaf.util

import leaf._

object TextUtil {
  def prependText(nodes: List[Node[_]], text: String): List[Node[_]] =
    nodes.headOption match {
      case None => List(Node(NodeType.Text(text)))
      case Some(Node(NodeType.Text(t), c)) =>
        Node(NodeType.Text(text + t), c) +: nodes.tail
      case Some(_) => Node(NodeType.Text(text)) +: nodes
    }

  def appendText(nodes: List[Node[_]], text: String): List[Node[_]] =
    nodes.lastOption match {
      case None => List(Node(NodeType.Text(text)))
      case Some(Node(NodeType.Text(t), c)) =>
        nodes.init :+ Node(NodeType.Text(t + text), c)
      case Some(_) => nodes :+ Node(NodeType.Text(text))
    }
}
