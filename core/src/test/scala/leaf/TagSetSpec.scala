package leaf

import org.scalatest.FunSuite

class TagSetSpec extends FunSuite {
  test("Code") {
    assert(TagSet.resolveTag("listing", Map("id" -> "test"))
      .contains(NodeType.Listing(Some("test"))))
  }
}
