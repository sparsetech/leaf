package leaf

import java.io.File

import scala.collection.mutable.ListBuffer

package object notebook {
  case class Session(listings: ListBuffer[Listing] = ListBuffer.empty)

  case class Line(value: String, `type`: String)

  case class Listing(
    name: String,
    file: String,
    lineStart: Int,
    var lineEnd: Int,
    outputLines: ListBuffer[Line]
  )

  case class ListingResult(
    code: String,
    language: Option[String],
    result: Option[String]
  )

  def listing(name: String)(implicit
    session: Session,
    file: sourcecode.File,
    line: sourcecode.Line
  ): Unit = {
    if (session.listings.nonEmpty)
      session.listings.last.lineEnd = line.value - 1
    session.listings += Listing(
      name,
      file.value,
      line.value,
      -1,
      ListBuffer.empty
    )
  }

  def println[T](
    str: T
  )(implicit session: Session, manifest: Manifest[T]): Unit = {
    if (session.listings.isEmpty) throw new Exception("No listings defined")
    session.listings.last.outputLines += Line(str.toString, manifest.toString)
  }

  def end()(implicit session: Session, line: sourcecode.Line): Unit = {
    if (session.listings.isEmpty) throw new Exception("No listings defined")
    session.listings.last.lineEnd = line.value - 1
  }

  def serialise()(implicit session: Session): Map[String, ListingResult] =
    session.listings.map { b =>
      val lines = FileUtils.readFile(new File(b.file))(_.getLines().toList)
      b.name ->
        ListingResult(
          TextHelpers.reindent(
            lines.slice(b.lineStart, b.lineEnd).mkString("\n")
          ),
          Some("scala"),
          if (b.outputLines.isEmpty) None
          else Some(b.outputLines.map(_.value).mkString("\n"))
        )
    }.toMap

  def write(path: String)(implicit session: Session): Unit = {
    import io.circe._, io.circe.generic.auto._, io.circe.parser._,
    io.circe.syntax._
    FileUtils.writeFile(new File(path), serialise().asJson.spaces2)
  }
}
