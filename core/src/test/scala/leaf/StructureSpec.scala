package leaf

import org.scalatest.FunSuite

class StructureSpec extends FunSuite {
  test("tree() (1)") {
    val structure = Structure.tree(
      NodeType.Chapter,
      List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1"))))
      )
    )

    assert(
      structure == List(
        Structure(
          NodeType.Section,
          None,
          List(Node(NodeType.Text("Section 1"))),
          List.empty
        )
      )
    )
  }

  test("tree() (2)") {
    val structure = Structure.tree(
      NodeType.Chapter,
      List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1")))),
        Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 1"))))
      )
    )

    assert(
      structure == List(
        Structure(
          NodeType.Section,
          None,
          List(Node(NodeType.Text("Section 1"))),
          List(
            Structure(
              NodeType.Subsection,
              None,
              List(Node(NodeType.Text("Subsection 1"))),
              List.empty
            )
          )
        )
      )
    )
  }

  test("tree() (3)") {
    val structure = Structure.tree(
      NodeType.Chapter,
      List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1")))),
        Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 1")))),
        Node(NodeType.Section, List(Node(NodeType.Text("Section 2"))))
      )
    )

    assert(
      structure == List(
        Structure(
          NodeType.Section,
          None,
          List(Node(NodeType.Text("Section 1"))),
          List(
            Structure(
              NodeType.Subsection,
              None,
              List(Node(NodeType.Text("Subsection 1"))),
              List.empty
            )
          )
        ),
        Structure(
          NodeType.Section,
          None,
          List(Node(NodeType.Text("Section 2"))),
          List.empty
        )
      )
    )
  }

  test("tree() (4)") {
    val structure = Structure.tree(
      NodeType.Chapter,
      List(
        Node(NodeType.Section, List(Node(NodeType.Text("Section 1")))),
        Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 1")))),
        Node(NodeType.Section, List(Node(NodeType.Text("Section 2"))))
      ).asInstanceOf[List[Node[NodeType]]].map(pipeline.SetIds.convert)
    )

    assert(
      structure == List(
        Structure(
          NodeType.Section,
          Some("section-1"),
          List(Node(NodeType.Text("Section 1"))),
          List(
            Structure(
              NodeType.Subsection,
              Some("subsection-1"),
              List(Node(NodeType.Text("Subsection 1"))),
              List.empty
            )
          )
        ),
        Structure(
          NodeType.Section,
          Some("section-2"),
          List(Node(NodeType.Text("Section 2"))),
          List.empty
        )
      )
    )
  }
}
