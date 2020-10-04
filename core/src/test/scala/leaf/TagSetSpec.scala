package leaf

import org.scalatest.funsuite.AnyFunSuite

class TagSetSpec extends AnyFunSuite {
  test("Code") {
    assert(
      TagSet
        .resolveTag("listing", Map("id" -> "test"))
        .contains(NodeType.Listing(Some("test")))
    )
  }
}
