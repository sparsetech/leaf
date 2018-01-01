package leaf.notebook

object TextHelpers {
  def reindent(text: String): String = {
    val lines = text.split("\n").dropWhile(_.trim.isEmpty)

    if (lines.isEmpty) ""
    else {
      val offsets = lines.map { line =>
        line.zipWithIndex.collectFirst {
          case (c, i) if !c.isWhitespace => i
        }
      }

      val leftMargin = offsets.collect { case Some(i) => i }.min

      val reindented = lines.map(_.drop(leftMargin))
      reindented.mkString("\n").trim
    }
  }
}
