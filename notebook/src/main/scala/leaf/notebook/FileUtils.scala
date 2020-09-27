package leaf.notebook

import java.io.{File, FileWriter}

import scala.io.Source
import scala.io.BufferedSource

private[leaf] object FileUtils {
  def readFile[T](file: File)(f: BufferedSource => T): T = {
    val source = Source.fromFile(file)
    try f(source)
    finally source.close()
  }

  def writeFile(file: File, content: String): Unit = {
    val fw = new FileWriter(file)
    try fw.write(content)
    finally fw.close()
  }
}
