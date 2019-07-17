package leaf.markdown

import leaf.NodeType._
import scala.meta.internal.fastparse.all._

/** Parser for HTML tags such as: <tag name="value" name2="value2"> */
object TagParser {
  val whitespaces = P(CharsWhile(_.isWhitespace))
  val stringChars = P(CharsWhile(!Set('"').contains(_)))
  val value = P("\"" ~/ stringChars.rep.! ~ "\"")
    .map(v => pine.HtmlHelpers.decodeAttributeValue(v))
  val identifier = P(CharsWhile(_.isLetterOrDigit).!)
  val argument   = P(identifier ~ "=" ~ value)
  val arguments  = P(argument.rep(sep = whitespaces))

  val openTag: Parser[List[Tag]] =
    P("<" ~ identifier ~ whitespaces.? ~ arguments ~ whitespaces.? ~ "/".!.? ~ ">")
      .map { case (name, attrs, close) =>
        val tag = OpenTag(name, attrs.toMap)
        if (close.isEmpty) List(tag) else List(tag, CloseTag(name))
      }

  val closeTag: Parser[Tag] = P("</" ~ identifier ~ whitespaces.? ~ ">")
    .map(CloseTag)

  val text: Parser[Tag] = P(CharsWhile(!"<>".contains(_)).!)
    .map(t => Text(pine.HtmlHelpers.decodeText(t, xml = false)))

  val tag: Parser[List[Tag]] = P(openTag | (closeTag | text).map(List(_)))
  val tags = P(whitespaces.? ~ tag.rep(min = 1) ~ End).map(_.toList.flatten)

  def parse(input: String): List[Tag] = tags.parse(input).get.value
}