package leaf.markdown

import leaf.NodeType._
import fastparse.all._

/** Parser for HTML tags such as: <tag name="value" name2="value2"> */
object TagParser {
  val whitespaces = P(CharsWhile(_.isWhitespace))
  val stringChars = P(CharsWhile(!Set('"').contains(_)))
  val value = P("\"" ~/ stringChars.rep.! ~ "\"")
    .map(_.replaceAllLiterally("&quot;", "\""))
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

  val tag: Parser[List[Tag]] = P(openTag | closeTag.map(List(_)))
  val tags = P(whitespaces.? ~ tag.rep(min = 1, sep = whitespaces) ~ End).map(_.toList.flatten)

  def parse(input: String): List[Tag] = tags.parse(input).get.value
}