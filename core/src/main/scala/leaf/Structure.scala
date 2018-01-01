package leaf

import scala.collection.mutable

/** @param caption May also contain formattings */
case class Structure(level  : NodeType.ChildLevel,
                     id     : Option[String],
                     caption: List[Node[_]],
                     var children: List[Structure])

object Structure {
  def levelsFor(base: NodeType.TreeBase): List[NodeType.ChildLevel] =
    base match {
      case NodeType.Book =>
        List(
          NodeType.Chapter,
          NodeType.Section,
          NodeType.Subsection,
          NodeType.Subsubsection)

      case NodeType.Article =>
        List(
          NodeType.Section,
          NodeType.Subsection,
          NodeType.Subsubsection)

      case NodeType.Chapter =>
        List(
          NodeType.Section,
          NodeType.Subsection,
          NodeType.Subsubsection)
    }

  /** Create node type for relative level number */
  def levelNodeType(base: NodeType.TreeBase, level: Int): NodeType.ChildLevel =
    levelsFor(base)(level - 1)

  /** @return Absolute level */
  def level(base: NodeType.TreeBase, tpe: NodeType.ChildLevel): Int =
    levelsFor(base).indexOf(tpe)

  /** @param nodes Top-level nodes */
  def tree(base: NodeType.TreeBase, nodes: List[Node[_]]): List[Structure] = {
    val stack = mutable.Stack[Structure](
      Structure(base, None, List.empty, List.empty))

    def iter(node: Node[_]): Unit =
      node.tpe match {
        case chldLevel: NodeType.ChildLevel =>
          while (
            stack.nonEmpty &&
            level(base, stack.top.level) >= level(base, chldLevel)
          ) stack.pop()

          val id = node.children.map(_.tpe).collectFirst {
            case NodeType.Id(id) => id
          }

          val newStructure = Structure(
            chldLevel,
            id,
            node.children.filter(!_.tpe.isInstanceOf[NodeType.Id]),
            List.empty)

          stack.top.children :+= newStructure
          stack.push(newStructure)

        case _ => node.children.foreach(iter)
      }

    nodes.foreach(iter)
    stack.last.children
  }
}
