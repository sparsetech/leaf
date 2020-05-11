package leaf

import org.scalatest.FunSuite

class WriterSpec extends FunSuite {
  test("Generate link") {
    val url = Node(NodeType.Url("http://google.com/"),
      List(Node(NodeType.Text("Google"))))

    assert(html.Writer.node(url) ==
      List(pine.tag.A.href("http://google.com/").set("Google")))
  }

  test("Generate ordered list") {
    val list = Node(NodeType.OrderedList(1), List(
      Node(NodeType.ListItem, List(Node(NodeType.Text("Item 1")))),
      Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))

    assert(html.Writer.node(list) == List(
      pine.tag.Ol.start("1").set(List(
        pine.tag.Li.set("Item 1"),
        pine.tag.Li.set("Item 2")))))
  }

  test("Generate table") {
    val table =
      Node(NodeType.Table, List(
        Node(NodeType.TableHead, List(
          Node(NodeType.TableRow, List(
            Node(NodeType.TableCell, List(Node(NodeType.Text("H1")))),
            Node(NodeType.TableCell, List(Node(NodeType.Text("H2")))))))),
        Node(NodeType.TableBody, List(
          Node(NodeType.TableRow, List(
            Node(NodeType.TableCell, List(Node(NodeType.Text("B1")))),
            Node(NodeType.TableCell, List(Node(NodeType.Text("B2")))))))),
        Node(NodeType.TableCaption, List(Node(NodeType.Text("caption"))))))

    val nodes = html.Writer.node(table)
    assert(nodes.head.asInstanceOf[pine.Tag[_]].toHtml ==
      "<table><thead><tr><th>H1</th><th>H2</th></tr></thead><tbody><tr><td>B1</td><td>B2</td></tr></tbody><caption>caption</caption></table>")
  }

  test("Generate footnote") {
    val footnote = List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Footnote("first")))),
      Node(NodeType.FootnoteBlock, List(
        Node(NodeType.FootnoteBlockItem("first"), List(
          Node(NodeType.Text("First footnote")))))))

    val nodes = footnote.flatMap(html.Writer.node(_))
    assert(nodes.nonEmpty)
  }

  test("Generate todo box") {
    val todo =
      Node(NodeType.Todo, List(
        Node(NodeType.Text("Hello World"))))

    val nodes = html.Writer.node(todo)
    assert(nodes == List(pine.tag.Div.`class`("todo").set("Hello World")))
  }

  test("Generate section with ID") {
    val section = Node(NodeType.Section, List(Node(NodeType.Text("Section 1"))))
    val sectionWithId = pipeline.SetIds.convert(section)

    assert(html.Writer.node(sectionWithId) ==
      List(pine.tag.H2.id("section-1").set("Section 1")))
  }

  test("Generate HTML tag") {
    val hr = Node(NodeType.Html("hr", Map.empty), List())
    assert(html.Writer.node(hr) == List(pine.tag.Hr))
  }

  test("Generate HTML tag with children") {
    val div =
      Node(NodeType.Html("div", Map("id" -> "test")), List(
        Node(NodeType.OrderedList(1), List(
          Node(NodeType.ListItem, List(Node(NodeType.Text("Item 1")))),
          Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))))

    assert(html.Writer.node(div) == List(
      pine.tag.Div.id("test").set(
        // TODO Ol.start should be integer
        pine.tag.Ol.start("1").set(List(
          pine.tag.Li.set("Item 1"),
          pine.tag.Li.set("Item 2"))))))
  }
}
