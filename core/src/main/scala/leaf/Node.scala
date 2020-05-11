package leaf

import scala.reflect.ClassTag

abstract class NodeType

object NodeType {
  sealed trait ChildLevel extends NodeType
  sealed trait TreeBase   extends ChildLevel

  case object Book          extends TreeBase
  case object Chapter       extends TreeBase
  case object Article       extends TreeBase
  case object Section       extends ChildLevel
  case object Subsection    extends ChildLevel
  case object Subsubsection extends ChildLevel

  case class Text(text: String) extends NodeType with Tag
  case class Id  (id  : String) extends NodeType

  case object HardLineBreak extends NodeType

  case object Subscript   extends NodeType
  case object Superscript extends NodeType
  case object SmallCaps   extends NodeType
  case object Bold        extends NodeType
  case object Italic      extends NodeType
  case object Anchor      extends NodeType
  case object Todo        extends NodeType
  case object Code        extends NodeType

  case class Url(href: String) extends NodeType

  case class  Footnote         (target: String) extends NodeType
  case object FootnoteBlock                     extends NodeType
  case class  FootnoteBlockItem(id: String    ) extends NodeType

  case class Image(url: String) extends NodeType

  case class Jump(target: String) extends NodeType

  case object BulletList extends NodeType
  case class OrderedList(start: Int) extends NodeType
  case object ListItem    extends NodeType

  case object Table        extends NodeType
  case object TableHead    extends NodeType
  case object TableBody    extends NodeType
  case object TableRow     extends NodeType
  case object TableCell    extends NodeType
  case object TableCaption extends NodeType

  case object Paragraph extends NodeType
  case object Quote     extends NodeType

  sealed trait Tag extends NodeType
  case class OpenTag (tag: String, attributes: Map[String, String]) extends Tag
  case class CloseTag(tag: String) extends Tag

  case class Html(tag: String, attributes: Map[String, String]) extends NodeType

  case class Listing(id      : Option[String] = None,
                     language: Option[String] = None,
                     code    : Option[String] = None,
                     result  : Option[String] = None
                    ) extends NodeType
}

case class Node[T <: NodeType](tpe: T, children: List[Node[_]] = List.empty) {
  def map(f: Node[_] => Node[_]): Node[T] =
    copy(children = children.map(f(_).map(f)))

  def flatMap(f: Node[_] => List[Node[_]]): Node[T] =
    copy(children = children.flatMap(n => f(n.flatMap(f))))

  private def filterChildren(f: Node[_] => Boolean): List[Node[_]] = {
    val seq = if (f(this)) List(this) else List.empty
    seq ++ children.flatMap(_.filterChildren(f))
  }

  def filter(f: Node[_] => Boolean): List[Node[_]] =
    children.flatMap(_.filterChildren(f))

  def foreach[U](f: Node[_] => U): Unit = filter(_ => true).foreach(f)

  def byType[U <: NodeType](implicit ut: ClassTag[U]): List[Node[U]] =
    filter(_.tpe match {
      case _: U => true
      case _    => false
    }).asInstanceOf[List[Node[U]]]
}
