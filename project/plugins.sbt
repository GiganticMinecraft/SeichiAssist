// プラグインJarを(依存関係にあるJarをすべて同梱して)出力するため
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

// Lintを掛けるため
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")

// コードフォーマットするため
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
