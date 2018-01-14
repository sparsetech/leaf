package leaf.markdown

import leaf.{Node, NodeType}
import org.scalatest.FunSuite

class ReaderSpec extends FunSuite {
  test("Bold") {
    assert(Reader.parse("**Hello**") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Bold, List(Node(NodeType.Text("Hello"))))))))
  }

  test("Italic") {
    assert(Reader.parse("*Hello*") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Italic, List(Node(NodeType.Text("Hello"))))))))
  }

  test("Change ID of section") {
    assert(Reader.parse("# Title <id value=\"title-2\"/>") == List(
      Node(NodeType.Section, List(
        Node(NodeType.Text("Title ")),
        Node(NodeType.Id("title-2"))))))
  }

  test("Code") {
    assert(Reader.parse("`code`") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Code, List(Node(NodeType.Text("code"))))))))
    assert(Reader.parse("``code``") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Code, List(Node(NodeType.Text("code"))))))))
    assert(Reader.parse("``Ref[_]``") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Code, List(Node(NodeType.Text("Ref[_]"))))))))
  }

  test("Link") {
    assert(Reader.parse("[Google](http://google.com/)") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Url("http://google.com/"), List(
          Node(NodeType.Text("Google"))))))))
  }

  test("Link without title") {
    assert(Reader.parse("[http://google.com/]") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Url("http://google.com/"))))))
  }

  test("Jump") {
    assert(Reader.parse("[Section](#section)") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Jump("section"), List(
          Node(NodeType.Text("Section"), List.empty)))))))
  }

  test("Jump without title") {
    assert(Reader.parse("[#section]") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Jump("section"))))))
  }

  test("Convert self-closing HTML tags") {
    assert(Reader.parse("""<br/>""") == List(Node(NodeType.Html("br", Map.empty))))
  }

  test("Map supported tag") {
    assert(Reader.parse("""<listing result="test"/>""") == List(
      Node(NodeType.Listing(result = Some("test")), List.empty)))
  }

  test("Map supported tag (2)") {
    assert(Reader.parse(s"""test\n\n<listing result="test"/>""") == List(
      Node(NodeType.Paragraph, List(Node(NodeType.Text("test")))),
      Node(NodeType.Listing(result = Some("test")), List.empty)))
  }

  test("Map supported tag (3)") {
    assert(Reader.parse("""<todo>**Hello** World</todo>""") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Todo, List(
          Node(NodeType.Bold, List(Node(NodeType.Text("Hello")))),
          Node(NodeType.Text(" World"))))))))
  }

  test("Map supported tag (4)") {
    assert(Reader.parse("""<todo><b>*Hello* World</b></todo>""") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Todo, List(
          Node(NodeType.Html("b", Map.empty), List(
            Node(NodeType.Italic, List(Node(NodeType.Text("Hello")))),
            Node(NodeType.Text(" World"))))))))))
  }

  test("Map multiple supported tags") {
    assert(Reader.parse(s"""<listing result="test"/><listing result="test2"/><listing result="test3"/>""") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Listing(result = Some("test")),  List.empty),
        Node(NodeType.Listing(result = Some("test2")), List.empty),
        Node(NodeType.Listing(result = Some("test3")), List.empty)))))
  }

  test("Map multiple supported tags (2)") {
    assert(Reader.parse(s"""  <listing result="test"/>\n  <listing result="test2"/>""") == List(
      Node(NodeType.Listing(result = Some("test")),  List.empty),
      Node(NodeType.Text("\n  "), List.empty),
      Node(NodeType.Listing(result = Some("test2")), List.empty)))
  }

  test("Preserve underscores in code") {
    assert(Reader.parse("`Tag[_]`") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Code, List(
          Node(NodeType.Text("Tag[_]"))))))))
  }

  test("Do not unescape HTML entities in code") {
    assert(Reader.parse("`&apos;`") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Code, List(
          Node(NodeType.Text("&apos;"))))))))
  }

  test("Image (1)") {
    assert(Reader.parse("![Traits](images/traits.png)") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Image("images/traits.png"))))))
  }

  test("Image (2)") {
    assert(Reader.parse("![Traits](images/traits.png)") == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Image("images/traits.png"))))))
  }

  test("Footnote") {
    assert(Reader.parse(
      """
        |Hello[^first]
        |
        |[^first]: First footnote
        |[^second]: Second footnote
      """.stripMargin) == List(
      Node(NodeType.Paragraph, List(
        Node(NodeType.Text("Hello")),
        Node(NodeType.Footnote("first"))
      )),
      Node(NodeType.FootnoteBlock, List(
        Node(NodeType.FootnoteBlockItem("first"), List(Node(NodeType.Text("First footnote")))),
        Node(NodeType.FootnoteBlockItem("second"), List(Node(NodeType.Text("Second footnote"))))))))
  }

  test("Source code") {
    assert(Reader.parse(
      """
        |```scala
        |test()
        |```
      """.stripMargin) == List(
      Node(NodeType.Listing(code = Some("test()"), language = Some("scala")))))
  }

  test("Ordered list") {
    assert(Reader.parse(
      """
        |1. Item 1
        |2. Item 2
      """.stripMargin) == List(
      Node(NodeType.OrderedList, List(
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 1")))),
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))))
  }

  test("Unordered list") {
    assert(Reader.parse(
      """
        |* Item 1
        |* Item 2
      """.stripMargin) == List(
      Node(NodeType.BulletList, List(
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 1")))),
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))))
  }

  test("Unordered list (2)") {
    assert(Reader.parse(
      """
        |* **Item** 1
        |* Item 2
      """.stripMargin) == List(
      Node(NodeType.BulletList, List(
        Node(NodeType.ListItem, List(
          Node(NodeType.Bold, List(Node(NodeType.Text("Item")))),
          Node(NodeType.Text(" 1")))),
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))))
  }

  test("Nested list") {
    assert(Reader.parse(
      """
        |* Item 1
        |    * Subitem 1
        |* Item 2
      """.stripMargin) == List(
      Node(NodeType.BulletList, List(
        Node(NodeType.ListItem, List(
          Node(NodeType.Paragraph, List(Node(NodeType.Text("Item 1")))),
          Node(NodeType.BulletList, List(
            Node(NodeType.ListItem, List(
              Node(NodeType.Text("Subitem 1")))))))),
        Node(NodeType.ListItem, List(Node(NodeType.Text("Item 2"))))))))
  }

  test("Tables") {
    assert(Reader.parse(
      """
        || H1 | H2 |
        ||----|----|
        || B1 | B2 |
        |[caption]
      """.stripMargin) == List(
      Node(NodeType.Table, List(
        Node(NodeType.TableHead, List(
          Node(NodeType.TableRow, List(
            Node(NodeType.TableCell, List(Node(NodeType.Text("H1")))),
            Node(NodeType.TableCell, List(Node(NodeType.Text("H2"))))
          ))
        )),
        Node(NodeType.TableBody, List(
          Node(NodeType.TableRow, List(
            Node(NodeType.TableCell, List(Node(NodeType.Text("B1")))),
            Node(NodeType.TableCell, List(Node(NodeType.Text("B2"))))
          ))
        )),
        Node(NodeType.TableCaption, List(Node(NodeType.Text("caption"))))
      ))))
  }

  test("Sections") {
    assert(Reader.parse(
      """
        |# Section 1
        |Content 1
        |# Section 2
        |Content 2
      """.stripMargin) == List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Content 1")))),
        Node(NodeType.Section, List(Node(NodeType.Text("Section 2")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Content 2"))))))
  }

  test("Chapters with subsections") {
    assert(Reader.parse(
      """
        |# Section 1
        |Content 1
        |## Subsection 1
        |Subcontent 1
        |## Subsection 2
        |Subcontent 2
        |# Section 2
        |Content 2
      """.stripMargin) == List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Content 1")))),
        Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 1")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Subcontent 1")))),
        Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 2")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Subcontent 2")))),
        Node(NodeType.Section, List(Node(NodeType.Text("Section 2")))),
        Node(NodeType.Paragraph, List(Node(NodeType.Text("Content 2"))))))
  }

  test("Quoted") {
    assert(Reader.parse("\"test\"") == List(
      Node(NodeType.Paragraph, List(Node(NodeType.Text("\"test\""))))))
  }

  test("Block quote") {
    assert(Reader.parse(
      """> A
> B
> C""") == List(
      Node(NodeType.Quote, List(
        Node(NodeType.Paragraph, List(
          Node(NodeType.Text("A")),
          Node(NodeType.Text("\n")),
          Node(NodeType.Text("B")),
          Node(NodeType.Text("\n")),
          Node(NodeType.Text("C"))))))))
  }

  test("HTML") {
    assert(Reader.parse("<p><i>hello</i> world</p>") == List(
      Node(NodeType.Html("p", Map.empty), List(
        Node(NodeType.Html("i", Map.empty), List(
          Node(NodeType.Text("hello")))),
        Node(NodeType.Text(" world"))))))
  }

  test("HTML with entity") {
    assert(Reader.parse("<p>&auml;</p>") == List(
      Node(NodeType.Html("p", Map.empty), List(
        Node(NodeType.Text("ä"))))))
  }

  test("HTML comments") {
    assert(Reader.parse("<!--Hello-->") == List.empty)
  }

  test("Indented code blocks") {
    val html = "\t<ul></ul>"
    assert(Reader.parse(html) == List(
      Node(NodeType.Listing(code = Some("<ul></ul>")))))

    val html2 = "\t <ul></ul>"
    assert(Reader.parse(html2) == List(
      Node(NodeType.Listing(code = Some("<ul></ul>")))))
  }

  test("Parse nested HTML with subsequent paragraph") {
    val html =
      """
        |<p><strong>First</strong></p>
        |
        |Second
      """.stripMargin

    assert(Reader.parse(html) == List(
      Node(NodeType.Html("p", Map.empty), List(
        Node(NodeType.Html("strong", Map.empty), List(
          Node(NodeType.Text("First"))
        ))
      )),
      Node(NodeType.Text("\n")),
      Node(NodeType.Paragraph, List(
        Node(NodeType.Text("Second"))
      ))
    ))
  }
}
