package leaf.markdown

import java.util

import scala.collection.JavaConverters._
import com.vladsch.flexmark.ast
import com.vladsch.flexmark.ext.footnotes.{Footnote, FootnoteBlock, FootnoteExtension}
import com.vladsch.flexmark.ext.tables._
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet

import leaf._
import leaf.notebook.TextHelpers

object Reader extends Reader(NodeType.Chapter)

class Reader(treeBase: NodeType.TreeBase) {
  def visit(node: ast.Text): List[Node[_]] =
    List(Node(NodeType.Text(node.getChars.toString)))

  def visit(node: ast.IndentedCodeBlock): List[Node[_]] =
    List(Node(
      NodeType.Listing(
        code = Some(TextHelpers.reindent(node.getChars.toString)))))

  def visit(node: ast.FencedCodeBlock): List[Node[_]] =
    List(Node(
      NodeType.Listing(
        language = Some(node.getInfo.toString),
        code     = Some(TextHelpers.reindent(node.getChildChars.toString)))))

  def visit(node: TableBlock): List[Node[_]] =
    List(Node(NodeType.Table, children(node)))

  def visit(node: TableHead): List[Node[_]] =
    List(Node(NodeType.TableHead, children(node)))

  def visit(node: TableBody): List[Node[_]] =
    List(Node(NodeType.TableBody, children(node)))

  def visit(node: TableRow): List[Node[_]] =
    List(Node(NodeType.TableRow, children(node)))

  def visit(node: TableCell): List[Node[_]] =
    List(Node(NodeType.TableCell, children(node)))

  def visit(node: TableCaption): List[Node[_]] =
    List(Node(NodeType.TableCaption, children(node)))

  def visit(node: ast.Emphasis): List[Node[_]] =
    List(Node(NodeType.Italic, children(node)))

  def visit(node: ast.StrongEmphasis): List[Node[_]] =
    List(Node(NodeType.Bold, children(node)))

  def visit(node: ast.Heading): List[Node[_]] =
    List(Node(Structure.levelNodeType(treeBase, node.getLevel), children(node)))

  def visit(node: ast.Link): List[Node[_]] = {
    val url = node.getUrl.toString
    val nt =
      if (url.head != '#') NodeType.Url(url)
      else NodeType.Jump(url.tail)
    List(Node(nt, children(node)))
  }

  def visit(node: ast.LinkRef): List[Node[_]] = {
    val anchor = node.getReference.toString
    val nt =
      if (anchor.head != '#') NodeType.Url(anchor)
      else NodeType.Jump(anchor.tail)
    List(Node(nt))
  }

  def visit(node: Footnote): List[Node[_]] =
    List(Node(NodeType.Footnote(node.getReference.toString)))

  def visit(node: FootnoteBlock): List[Node[_]] =
    List(Node(
      NodeType.FootnoteBlockItem(node.getText.toString),
      paragraphChildren(node)))

  def visit(node: ast.HtmlBlock): List[Node[_]] = {
    val html = node.getChars.toString
    val tags = TagParser.parse(html)
    tags.map(Node(_, List.empty))
  }

  def visit(node: ast.HtmlInline): List[Node[_]] = {
    val html = node.getChars.toString
    val tags = TagParser.parse(html)
    tags.map(Node(_, List.empty))
  }

  // Ignore HTML comments for now
  def visit(node: ast.HtmlCommentBlock): List[Node[_]] = List.empty

  def visit(node: ast.Image): List[Node[_]] =
    List(Node(NodeType.Image(node.getUrl.toString)))

  def visit(node: ast.Code): List[Node[_]] =
    List(Node(
      NodeType.Code,
      List(Node(NodeType.Text(node.getChildChars.toString)))))

  def visit(node: ast.BulletList): List[Node[_]] =
    List(Node(NodeType.BulletList, children(node)))

  def visit(node: ast.BulletListItem): List[Node[_]] =
    List(Node(NodeType.ListItem, paragraphChildren(node)))

  def visit(node: ast.OrderedList): List[Node[_]] =
    List(Node(NodeType.OrderedList, children(node)))

  def visit(node: ast.OrderedListItem): List[Node[_]] =
    List(Node(NodeType.ListItem, paragraphChildren(node)))

  def visit(node: ast.BlockQuote): List[Node[_]] =
    List(Node(NodeType.Quote, children(node)))

  def visit(node: ast.Paragraph): List[Node[_]] =
    List(Node(NodeType.Paragraph, children(node)))

  def visit(node: ast.SoftLineBreak): List[Node[_]] =
    List(Node(NodeType.Text("\n")))

  def dispatch(node: ast.Node): List[Node[_]] =
    node match {
      case n: ast.Paragraph => visit(n)
      case n: ast.Emphasis => visit(n)
      case n: ast.StrongEmphasis => visit(n)
      case n: ast.Text => visit(n)
      case n: ast.Code => visit(n)
      case n: ast.Link => visit(n)
      case n: ast.LinkRef => visit(n)
      case n: ast.Image => visit(n)
      case n: ast.OrderedList => visit(n)
      case n: ast.OrderedListItem => visit(n)
      case n: ast.BulletList => visit(n)
      case n: ast.BulletListItem => visit(n)
      case n: ast.Heading => visit(n)
      case n: ast.BlockQuote => visit(n)
      case n: ast.SoftLineBreak => visit(n)
      case n: ast.IndentedCodeBlock => visit(n)
      case n: ast.FencedCodeBlock => visit(n)
      case n: TableBlock => visit(n)
      case n: TableHead => visit(n)
      case n: TableBody => visit(n)
      case n: TableRow => visit(n)
      case n: TableCell => visit(n)
      case n: TableCaption => visit(n)
      case n: Footnote => visit(n)
      case n: FootnoteBlock => visit(n)
      case n: ast.HtmlBlock => visit(n)
      case n: ast.HtmlInline => visit(n)
      case n: ast.HtmlCommentBlock => visit(n)
    }

  def mergeFootnodeBlockItems(nodes: List[Node[_]]): List[Node[_]] = {
    val before = nodes
      .takeWhile(!_.tpe.isInstanceOf[NodeType.FootnoteBlockItem])
    val blockItems = nodes.drop(before.length)
      .takeWhile(_.tpe.isInstanceOf[NodeType.FootnoteBlockItem])
    val after = nodes.drop(before.length + blockItems.length)

    val nestedBlockItems =
      if (blockItems.isEmpty) List.empty
      else List(Node(NodeType.FootnoteBlock, blockItems))

    before ++ nestedBlockItems ++ after
  }

  def convertTags(nodes: List[Node[_]]): List[Node[_]] = {
    def f(nodes: List[Node[_]], tag: Option[String]): (List[Node[_]], List[Node[_]]) =
      nodes match {
        case Nil => (List.empty, List.empty)
        case x :: xs =>
          x.tpe match {
            case NodeType.OpenTag(t, a) =>
              val (children , remainder ) = f(xs, Some(t))
              val (children2, remainder2) = f(remainder, tag)

              val nt = TagSet.resolveTag(t, a)
                .getOrElse(NodeType.Html(t, a))

              (List(Node(nt, children)) ++ children2, remainder2)

            case NodeType.CloseTag(t) if !tag.contains(t) =>
              throw new Exception(s"Closing wrong tag ($tag expected, $t given)")

            case NodeType.CloseTag(_) => (List.empty, xs)

            case _ =>
              val (children, remainder) = f(xs, tag)
              (x +: children, remainder)
          }
      }

    val (children, remainder) = f(nodes, Option.empty)
    assert(remainder.isEmpty)
    children
  }

  def children(node: ast.Node): List[Node[_]] =
    convertTags(
      mergeFootnodeBlockItems(
        node.getChildren.asScala.toList
          .filter(!_.isInstanceOf[TableSeparator])
          .flatMap(dispatch)
      )
    )

  /** TODO Flexmark packs simple list items and footnote blocks in paragraphs */
  def paragraphChildren(node: ast.Node): List[Node[_]] =
    node.getChildren.asScala.toList match {
      case (c: ast.Paragraph) :: Nil => children(c)
      case _ => children(node)
    }

  def parse(input: String): List[Node[NodeType]] = {
    val options  = new MutableDataSet()
      .set(Parser.EXTENSIONS, util.Arrays.asList(
        TablesExtension.create, FootnoteExtension.create))
    val parser   = Parser.builder(options).build
    val document = parser.parse(input)

    children(document)
      .asInstanceOf[List[Node[NodeType]]]
  }
}