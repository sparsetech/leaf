package leaf

object TagSet {
  def resolveTag(
    tag: String,
    parameters: Map[String, String]
  ): Option[NodeType] =
    tag match {
      case "id" =>
        Some(
          NodeType.Id(
            parameters.getOrElse(
              "value",
              throw new Exception("Attribute `value` not set on `id`")
            )
          )
        )
      case "listing" =>
        Some(
          NodeType.Listing(
            id = parameters.get("id"),
            language = parameters.get("language"),
            code = parameters.get("code"),
            result = parameters.get("result")
          )
        )
      case "todo" => Some(NodeType.Todo)
      case _      => None
    }
}
