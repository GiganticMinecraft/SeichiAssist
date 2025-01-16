// プラグインJarを(依存関係にあるJarをすべて同梱して)出力するため
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.2.0")

// Lintを掛けるため
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.0")

// コードフォーマットするため
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.3")
