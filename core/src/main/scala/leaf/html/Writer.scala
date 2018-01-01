package leaf.html

import pine._
import leaf.{NodeType, Structure, Node => LNode}

object Writer extends Writer

trait Writer {
  def children(n: LNode[_]): List[pine.Node] = n.children.flatMap(node)
  def children[T <: NodeType]
              (n: LNode[_],
               f: LNode[T] => List[pine.Node]): List[pine.Node] =
    n.children.flatMap(child => f(child.asInstanceOf[LNode[T]]))

  def getIdFromChildren(nodes: List[LNode[_]]): Option[String] =
    nodes.map(_.tpe).collectFirst { case NodeType.Id(id) => id }

  val id = (_: LNode[NodeType.Id]) => List.empty

  val html = { html: LNode[NodeType.Html] =>
    List(Tag(html.tpe.tag, html.tpe.attributes).set(children(html)))
  }

  val table = { table: LNode[NodeType.Table.type] =>
    List(html"<table>${children(table)}</table>")
  }

  val tableHead = { head: LNode[NodeType.TableHead.type] =>
    List(html"<thead>${children(head, tableHeadRow)}</thead>")
  }

  val tableHeadRow = { row: LNode[NodeType.TableRow.type] =>
    List(html"<tr>${children(row, tableHeadCell)}</tr>")
  }

  val tableHeadCell = { column: LNode[NodeType.TableCell.type] =>
    List(html"<th>${children(column)}</th>")
  }

  val tableBody = { body: LNode[NodeType.TableBody.type] =>
    List(html"<tbody>${children(body)}</tbody>")
  }

  val tableRow = { row: LNode[NodeType.TableRow.type] =>
    List(html"<tr>${children(row)}</tr>")
  }

  val tableCell = { cell: LNode[NodeType.TableCell.type] =>
    List(html"<td>${children(cell)}</td>")
  }

  val tableCaption = { caption: LNode[NodeType.TableCaption.type] =>
    List(html"<caption>${children(caption)}</caption>")
  }

  val jump = { jump: LNode[NodeType.Jump] =>
    val href = "#" + jump.tpe.target
    List(html"<a href=$href>${children(jump)}</a>")
  }

  val bold = { bold: LNode[NodeType.Bold.type] =>
    List(html"<b>${children(bold)}</b>")
  }

  val italic = { italic: LNode[NodeType.Italic.type] =>
    List(html"<i>${children(italic)}</i>")
  }

  val code = { code: LNode[NodeType.Code.type] =>
    List(html"""<span class="code">${children(code)}</span>""")
  }

  val subsubsection = { subsubsection: LNode[NodeType.Subsubsection.type] =>
    List(html"<h4 id=${getIdFromChildren(subsubsection.children)}>${children(subsubsection)}</h4>")
  }

  val subsection = { subsection: LNode[NodeType.Subsection.type] =>
    List(html"<h3 id=${getIdFromChildren(subsection.children)}>${children(subsection)}</h3>")
  }

  val section = { section: LNode[NodeType.Section.type] =>
    List(html"<h2 id=${getIdFromChildren(section.children)}>${children(section)}</h2>")
  }

  val chapter = { chapter: LNode[NodeType.Chapter.type] =>
    List(html"<h1 id=${getIdFromChildren(chapter.children)}>${children(chapter)}</h1>")
  }

  val listItem = { listItem: LNode[NodeType.ListItem.type] =>
    List(html"<li>${children(listItem)}</li>")
  }

  val bulletList = { list: LNode[NodeType.BulletList.type] =>
    List(html"<ul>${children(list)}</ul>")
  }

  val orderedList = { list: LNode[NodeType.OrderedList.type] =>
    List(html"<ol>${children(list)}</ol>")
  }

  val listing = { listing: LNode[NodeType.Listing] =>
    val lng = listing.tpe.language.getOrElse("")
    val cls = s"sourceCode $lng"
    val code = html"<pre class=$cls><code data-lang=$lng>${listing.tpe.code.getOrElse("")}</code></pre>"
    val result = listing.tpe.result.map { result =>
      List(
        html"<b>Output:</b>",
        html"""<pre class="sourceCode"><code>$result</code></pre>""")
    }

    code +: result.getOrElse(List.empty)
  }

  val todo = { todo: LNode[NodeType.Todo.type] =>
    List(html"""<div class="todo">${children(todo)}</div>""")
  }

  val url = { url: LNode[NodeType.Url] =>
    List(html"<a href=${url.tpe.href}>${children(url)}</a>")
  }

  val image = { image: LNode[NodeType.Image] =>
    List(html"<img src=${image.tpe.url}/>")
  }

  val paragraph = { paragraph: LNode[NodeType.Paragraph.type] =>
    List(html"<p>${children(paragraph)}</p>")
  }

  val quote = { quote: LNode[NodeType.Quote.type] =>
    List(html"<blockquote>${children(quote)}</blockquote>")
  }

  val text = { text: LNode[NodeType.Text] =>
    List(pine.Text(text.tpe.text))
  }

  val footnote = { fn: LNode[NodeType.Footnote] =>
    val id     = fn.tpe.target  // TODO Check for valid characters
    val refId  = s"fnref$id"
    val target = s"#fn$id"
    List(html"""<a href=$target id=$refId class="footnote">[$id]</a>""")
  }

  val footnoteBlock = { fn: LNode[NodeType.FootnoteBlock.type] =>
    List(html"""<ul>${children(fn)}</ul>""")
  }

  val footnoteBlockItem = { fn: LNode[NodeType.FootnoteBlockItem] =>
    val id     = fn.tpe.id
    val fnId   = s"fn$id"
    val target = s"#fnref$id"

    List(html"""
      <li id=$fnId>
        [$id]
        ${children(fn)}
        <a href=$target class="reversefootnote">&#160;&#8617;</a>
      </li>
    """)
  }

  def node(node: LNode[_]): List[Node] = node.tpe match {
    case _: NodeType.Id => id(node.asInstanceOf[LNode[NodeType.Id]])
    case _: NodeType.Html => html(node.asInstanceOf[LNode[NodeType.Html]])
    case NodeType.Table => table(node.asInstanceOf[LNode[NodeType.Table.type]])
    case NodeType.TableHead => tableHead(node.asInstanceOf[LNode[NodeType.TableHead.type]])
    case NodeType.TableBody => tableBody(node.asInstanceOf[LNode[NodeType.TableBody.type]])
    case NodeType.TableRow => tableRow(node.asInstanceOf[LNode[NodeType.TableRow.type]])
    case NodeType.TableCell => tableCell(node.asInstanceOf[LNode[NodeType.TableCell.type]])
    case NodeType.TableCaption => tableCaption(node.asInstanceOf[LNode[NodeType.TableCaption.type]])
    case _: NodeType.Jump => jump(node.asInstanceOf[LNode[NodeType.Jump]])
    case NodeType.Bold => bold(node.asInstanceOf[LNode[NodeType.Bold.type]])
    case NodeType.Italic => italic(node.asInstanceOf[LNode[NodeType.Italic.type]])
    case NodeType.Code => code(node.asInstanceOf[LNode[NodeType.Code.type]])
    case NodeType.Subsubsection => subsubsection(node.asInstanceOf[LNode[NodeType.Subsubsection.type]])
    case NodeType.Subsection => subsection(node.asInstanceOf[LNode[NodeType.Subsection.type]])
    case NodeType.Section => section(node.asInstanceOf[LNode[NodeType.Section.type]])
    case NodeType.Chapter => chapter(node.asInstanceOf[LNode[NodeType.Chapter.type]])
    case NodeType.ListItem => listItem(node.asInstanceOf[LNode[NodeType.ListItem.type]])
    case NodeType.BulletList => bulletList(node.asInstanceOf[LNode[NodeType.BulletList.type]])
    case NodeType.OrderedList => orderedList(node.asInstanceOf[LNode[NodeType.OrderedList.type]])
    case _: NodeType.Listing => listing(node.asInstanceOf[LNode[NodeType.Listing]])
    case NodeType.Todo => todo(node.asInstanceOf[LNode[NodeType.Todo.type]])
    case _: NodeType.Url => url(node.asInstanceOf[LNode[NodeType.Url]])
    case _: NodeType.Image => image(node.asInstanceOf[LNode[NodeType.Image]])
    case NodeType.Paragraph => paragraph(node.asInstanceOf[LNode[NodeType.Paragraph.type]])
    case NodeType.Quote => quote(node.asInstanceOf[LNode[NodeType.Quote.type]])
    case _: NodeType.Text => text(node.asInstanceOf[LNode[NodeType.Text]])
    case _: NodeType.Footnote => footnote(node.asInstanceOf[LNode[NodeType.Footnote]])
    case NodeType.FootnoteBlock => footnoteBlock(node.asInstanceOf[LNode[NodeType.FootnoteBlock.type]])
    case _: NodeType.FootnoteBlockItem => footnoteBlockItem(node.asInstanceOf[LNode[NodeType.FootnoteBlockItem]])
  }

  def tableOfContents(references: List[Structure],
                      maxDepth: Int): Option[pine.Node] = {
    def iterate(reference: Structure, depth: Int): Option[pine.Node] =
      if (depth >= maxDepth) None
      else {
        val caption = reference.caption.flatMap(n => leaf.html.Writer
          .node(n.asInstanceOf[leaf.Node[NodeType]]))

        val children = reference.children
          .flatMap(iterate(_, depth + 1))
          .map(tag.Ul.set)

        val url = reference.id.map("#" + _)
        Some(html"<li><a href=$url>$caption</a>$children</li>")
      }

    val toc = references.flatMap(iterate(_, 0))
    if (toc.isEmpty) None else Some(tag.Ul.set(toc))
  }
}