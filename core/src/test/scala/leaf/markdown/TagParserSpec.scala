package leaf.markdown

import scala.meta.internal.fastparse.core.Parsed
import leaf.NodeType

import scala.util.{Failure, Success, Try}
import org.scalatest.FunSuite

class TagParserSpec extends FunSuite {
  def parse(input: String): Try[List[NodeType.Tag]] =
    TagParser.tags.parse(input) match {
      case Parsed.Success(value, _) => Success(value)
      case f: Parsed.Failure[Char, String] => Failure(new Exception(f.toString))
    }

  test("Parse tag without arguments") {
    assert(parse("<scala>") == Success(List(NodeType.OpenTag("scala", Map.empty))))
  }

  test("Parse tag with one argument") {
    assert(parse("""<scala type="imports">""") == Success(List(
      NodeType.OpenTag("scala", Map("type" -> "imports")))))
  }

  test("Generate close tags") {
    assert(parse("""<scala type="imports"/>""") == Success(List(
      NodeType.OpenTag("scala", Map("type" -> "imports")),
      NodeType.CloseTag("scala"))))

    assert(parse("""<scala type="imports"   />""") == Success(List(
      NodeType.OpenTag("scala", Map("type" -> "imports")),
      NodeType.CloseTag("scala"))))
  }

  test("Parse tag with multiple arguments") {
    assert(parse("""<scala type="imports" name="test">""") == Success(List(
      NodeType.OpenTag("scala", Map("type" -> "imports", "name" -> "test")))))
  }

  test("Parse block extension") {
    assert(parse("""<tag key="value">""") == Success(List(
      NodeType.OpenTag("tag", Map("key" -> "value")))))

    assert(parse("""<tag   key="value">""") == Success(List(
      NodeType.OpenTag("tag", Map("key" -> "value")))))

    assert(parse("""<tag   key="val&quot;ue">""") == Success(List(
      NodeType.OpenTag("tag", Map("key" -> "val\"ue")))))

    assert(parse("""<tag   key="val&gt;ue">""") == Success(List(
      NodeType.OpenTag("tag", Map("key" -> "val>ue")))))

    assert(parse("""<tag   key="val ue">""") == Success(List(
      NodeType.OpenTag("tag", Map("key" -> "val ue")))))

    assert(parse("""<tag key="value" key2="value2">""") == Success(List(
      NodeType.OpenTag("tag", Map(
        "key"  -> "value",
        "key2" -> "value2")))))

    assert(parse("""<tag key="value" key2="value2"   >""") == Success(List(
      NodeType.OpenTag("tag", Map(
        "key"  -> "value",
        "key2" -> "value2")))))

    assert(parse("""<tag key="value"   key2="value2">""") == Success(List(
      NodeType.OpenTag("tag", Map(
        "key"  -> "value",
        "key2" -> "value2")))))
  }

  test("Parse text nodes") {
    assert(parse("test") == Success(List(NodeType.Text("test"))))
  }
}
