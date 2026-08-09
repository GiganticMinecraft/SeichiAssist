// プラグインJarを(依存関係にあるJarをすべて同梱して)出力するため
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.4.1")

// Lintを掛けるため
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.7")

// コードフォーマットするため
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
