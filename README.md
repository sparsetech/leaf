# Leaf
[![Build Status](https://travis-ci.org/sparsetech/leaf.svg?branch=master)](https://travis-ci.org/sparsetech/leaf)
[![Join the chat at https://gitter.im/sparsetech/leaf](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sparsetech/leaf?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://img.shields.io/maven-central/v/tech.sparse/leaf-core_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22tech.sparse%22%20AND%20a%3A%22leaf-core_2.12%22)

Leaf is a lightweight Scala library which provides functionality to instantiate, manipulate and convert document ASTs. As an example, you could instantiate an AST from a Markdown document, load and embed code listings from a file, derive a table of contents and then compile an HTML document.

## Features
* AST for documents
    * Headings
    * Text formatting
    * Links and anchors
    * Tables
    * Code listings
    * Footnotes
* *Readers:* Markdown
* *Writers:* HTML
* AST manipulation
    * Table of contents
* Notebook support

## Installation
Add the following dependencies to your build configuration:

```sbt
libraryDependencies += "tech.sparse" %% "leaf-core" % "0.1.0"

// If you would like to create notebooks, include:
libraryDependencies += "tech.sparse" %% "leaf-notebook" % "0.1.0"
```

## Example
You can parse Markdown documents to an AST as follows:

```scala
import leaf._

val nodes = markdown.Reader.parse("**Hello**")
```

The tree representation is:

```scala
List(
  Node(NodeType.Paragraph, List(
    Node(NodeType.Bold, List(Node(NodeType.Text("Hello")))))))
```

You can render it to HTML using `html.Writer.node()` which returns a list of [Pine](https://github.com/sparsetech/pine) tags:

```scala
val output = nodes.flatMap(html.Writer.node)  // : List[Tag[_]]
val html   = pine.tag.Div.set(output).toHtml  // : String
html  // <div><p><b>Hello</b></p></div>
```

### Traversal and Manipulation
You can use regular combinators like `map`, `flatMap` or `filter` to traverse or manipulate Leaf trees. These functions iterate all children recursively. For example, extracting all to-do blocks can be done with one function call:

```scala
val todos = nodes.flatMap(_.filter(_.tpe == NodeType.Todo))
```

## Extensions
Leaf supports the following tags:

* `id`: Override auto-generated ID of a heading; see below
* `todo`: To-do block
* `listing`: Code listing; see below

### Markdown
In Markdown, you can use the regular HTML tag notation:

```html
<todo>Hello World</todo>
```

As per the [CommonMark](http://spec.commonmark.org/) specification, it is permitted to use Markdown formattings in the children:

```markdown
<todo>*text*</todo>
```

It is parsed to the following AST:

```scala
List(
  Node(NodeType.Paragraph, List(
    Node(NodeType.Todo, List(
      Node(NodeType.Italic, List(
        Node(NodeType.Text("text"), List()))))))))
```

Any other tag will be retained and interpreted as regular HTML content.

### Table of Contents
If you would like to generate a table of contents and make it possible to jump to the headings, the ID tag should be set. To do so, call `pipeline.SetIds.convert()` on all top-level nodes. The ID of a heading will be derived from its caption.

Sometimes there are multiple headings with the same name. In that case, it is possible to rename the ID:

```markdown
# Title <id value="title-2"/>
```

You can use the function `Structure.tree()` to obtain a nested table of contents. You can pass this value to `html.Writer.tableOfContents()` which will render it using nested `<ul>` lists.

### Listings and Notebooks
Leaf provides a module which allows to create Scala notebooks for documenting libraries. These notebooks are separated into *sections* which can be referenced by your document. Leaf provides an AST pass which embeds the code listings. The advantage of using this functionality is that the notebook is compiled, so all examples in a library's documentation will be guaranteed to work. The second advantage is that a listing's output will be included as well.

First, you need to import `leaf.notebook._`. This will override `println` and make a few helper functions available. Next, define an implicit `Session`. Now, you can write a listing using `listing("<id>")`. After the last listing, you must call `end()`.

All code listings and their results will be recorded in the `Session` object. You can call `write()` which creates a `Map[String, ListingResult]` and save this to a JSON file which Leaf can read and embed into an AST.

```scala
import leaf.notebook._

object Listings extends App {
  implicit val session = Session()

  listing("sum")
  val sum = 1 + 1
  println(s"1 + 1 = $sum")

  listing("division")
  val div = sum / 2
  println(s"$sum / 2 = $div")

  end()
  write("listings.json")
}
```

The JSON file looks as follows:

```json
{
  "sum" : {
    "code" : "val sum = 1 + 1\nprintln(s\"1 + 1 = $sum\")",
    "language" : "scala",
    "result" : "1 + 1 = 2"
  },
  "division" : {
    "code" : "val div = sum / 2\nprintln(s\"$sum / 2 = $div\")",
    "language" : "scala",
    "result" : "2 / 2 = 1"
  }
}
```

Finally, you can embed the listings into your AST:

```scala
val listings = pipeline.Listings.read("listings.json")
val nodes    = markdown.Reader.parse("""
  <listing id="sum"/>
  <listing id="division"/>
""")
val embedded = nodes.map(pipeline.Listings.embed(_, listings))
```

## Customisation
It is possible to add node types and define a custom writer:

```scala
import pine._

import leaf.{Node => LNode}
import leaf.NodeType
import leaf.html.Writer

object CustomNodeType {
  case class SourceFile(path: String) extends NodeType
}

class CustomWriter(editSourceUrl: String) extends Writer {
  import CustomNodeType._

  val sourceFile = { sourceFile: LNode[SourceFile] =>
    val url = editSourceUrl + sourceFile.tpe.path
    List(html"""<a class="edit" href=$url>Edit chapter ⤴</a>""")
  }

  override def node(node: LNode[_]): List[Node] = node.tpe match {
    case _: SourceFile => sourceFile(node.asInstanceOf[LNode[SourceFile]])
    case _ => super.node(node)
  }
}
```

Instead of writing your own traversal function, you can re-use Leaf's Writer and only override those nodes you would like to render differently.

## Links
* [ScalaDoc](https://www.javadoc.io/doc/tech.sparse/leaf-core_2.12/)

## Credits
The [Flexmark](https://github.com/vsch/flexmark-java) library is used for parsing Markdown.

## Licence
Leaf is licensed under the terms of the Apache v2.0 licence.

## Authors
* Tim Nieradzik
