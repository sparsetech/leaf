package leaf.pipeline

import org.scalatest.FunSuite

import leaf._

class SetIdsSpec extends FunSuite {
  test("Set IDs") {
    val nodes = List(
      Node(NodeType.Section   , List(Node(NodeType.Text("Section 1")))),
      Node(NodeType.Subsection, List(Node(NodeType.Text("Subsection 1"))))
    ).asInstanceOf[List[Node[NodeType]]]

    assert(nodes.map(SetIds.convert) == List(
      Node(NodeType.Section, List(
        Node(NodeType.Text("Section 1")),
        Node(NodeType.Id("section-1")))),

      Node(NodeType.Subsection, List(
        Node(NodeType.Text("Subsection 1")),
        Node(NodeType.Id("subsection-1"))))))
  }

  test("Skip IDs") {
    val node =
      Node(NodeType.Section, List(
        Node(NodeType.Text("Section 1")),
        Node(NodeType.Id("section"))))

    assert(SetIds.convert(node) ==
      Node(NodeType.Section, List(
        Node(NodeType.Text("Section 1")),
        Node(NodeType.Id("section")))))
  }
}
