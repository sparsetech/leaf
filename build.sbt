val Pine       = "0.1.1"
val Circe      = "0.8.0"
val FlexMark   = "0.28.24"
val FastParse  = "1.0.0"
val ScalaTest  = "3.0.4"
val SourceCode = "0.1.4"

name := "leaf"

lazy val commonSettings = Seq(
  organization      := "tech.sparse",
  scalaVersion      := "2.12.4-bin-typelevel-4",
  scalaOrganization := "org.typelevel",
  scalacOptions     += "-Yliteral-types",
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"    % Circe,
    "io.circe" %% "circe-generic" % Circe,
    "io.circe" %% "circe-parser"  % Circe
  ),

  pomExtra :=
    <url>https://github.com/sparsetech/leaf</url>
    <licenses>
      <license>
        <name>Apache-2.0</name>
        <url>https://www.apache.org/licenses/LICENSE-2.0.html</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:sparsetech/leaf.git</url>
    </scm>
    <developers>
      <developer>
        <id>tindzk</id>
        <name>Tim Nieradzik</name>
        <url>http://github.com/tindzk</url>
      </developer>
    </developers>
)

lazy val root = project.in(file("."))
  .aggregate(core, notebook)
  .settings(
    commonSettings,
    skip in publish := true
  )

lazy val core = project
  .in(file("core"))
  .dependsOn(notebook)
  .settings(
    commonSettings,
    name := "leaf-core",
    libraryDependencies ++= Seq(
      "tech.sparse"            %% "pine"                   % Pine,
      "com.lihaoyi"            %% "fastparse"              % FastParse,
      "com.vladsch.flexmark"   %  "flexmark"               % FlexMark,
      "com.vladsch.flexmark"   %  "flexmark-ext-tables"    % FlexMark,
      "com.vladsch.flexmark"   %  "flexmark-ext-footnotes" % FlexMark,
      "org.scalatest"          %% "scalatest"              % ScalaTest % "test"
    )
  )

lazy val notebook = project
  .in(file("notebook"))
  .settings(
    name := "leaf-notebook",
    commonSettings,
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % SourceCode
  )
