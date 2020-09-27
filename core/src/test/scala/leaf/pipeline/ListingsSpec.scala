package leaf.pipeline

import leaf.notebook.ListingResult
import leaf.{Node, NodeType}
import org.scalatest.FunSuite

class ListingsSpec extends FunSuite {
  test("embed()") {
    val result = Listings.embed(
      Node(NodeType.Listing(id = Some("print"))),
      Map("print" -> ListingResult("println()", Some("scala"), None))
    )

    assert(
      result == Node(
        NodeType.Listing(
          id = Some("print"),
          language = Some("scala"),
          code = Some("println()")
        )
      )
    )
  }
}
