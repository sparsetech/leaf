package leaf.pipeline

import java.io.File

import leaf.notebook.{FileUtils, ListingResult}
import leaf.{Node, NodeType}

object Listings {
  def read(path: String): Map[String, ListingResult] = {
    import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
    FileUtils.readFile(new File(path)) { json =>
      decode[Map[String, ListingResult]](json.mkString) match {
        case Left(_) => throw new Exception(s"Cannot read listing file $path")
        case Right(s) => s
      }
    }
  }

  def embed(root: Node[_], notebook: Map[String, ListingResult]): Node[_] = {
    def iterate(node: Node[_]): Node[_] =
      node.tpe match {
        case code @ NodeType.Listing(Some(id), _, _, _) =>
          val block = notebook.getOrElse(id,
            throw new Exception(s"Notebook does not define '$id'"))
          node.copy(tpe = code.copy(
            code     = Some(block.code),
            language = block.language,
            result   = block.result))

        case _ => node.map(iterate)
      }

    iterate(root)
  }
}