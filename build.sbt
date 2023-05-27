import ResourceFilter.filterResources
import sbt.Keys.baseDirectory

import java.io._

// region 全プロジェクト共通のメタデータ

// TODO: Scala 2.13.11がリリースされたらリリースバージョンに変える
ThisBuild / scalaVersion := "2.13.11-bin-fd209dc-SNAPSHOT"
// ThisBuild / version はGitHub Actionsによって取得/自動更新される。
// 次の行は ThisBuild / version := "(\d*)" の形式でなければならない。
ThisBuild / version := "79"
ThisBuild / organization := "click.seichi"
ThisBuild / description := "ギガンティック☆整地鯖の独自要素を司るプラグイン"

// Scalafixが要求するため、semanticdbは有効化する
// TODO: Scala 2.13.11がリリースされたらバージョン指定演算子を`%%`に戻す
addCompilerPlugin("org.scalameta" % "semanticdb-scalac_2.13.10" % "4.7.6")
ThisBuild / scalacOptions += "-Yrangepos"

// endregion

// region 雑多な設定

// kind-projector 構文を使いたいため
// TODO: Scala 2.13.11がリリースされたらバージョン指定演算子を`%%`に戻す
addCompilerPlugin("org.typelevel" % "kind-projector_2.13.10" % "0.13.2")

// CIビルドで詳細なログを確認するため
ThisBuild / logLevel := {
  if (scala.sys.env.get("BUILD_ENVIRONMENT_IS_CI_OR_LOCAL").contains("CI")) {
    Level.Debug
  } else {
    Level.Info
  }
}

// テストが落ちた時にスタックトレースを表示するため。
// ScalaTest のオプションは https://www.scalatest.org/user_guide/using_the_runner を参照のこと。
Compile / testOptions += Tests.Argument("-oS")

ThisBuild / scalaBinaryVersion := "2.13"

// endregion

// region 依存関係

resolvers ++= Seq(
  "jitpack.io" at "https://jitpack.io",
  "maven.sk89q.com" at "https://maven.enginehub.org/repo/",
  "maven.playpro.com" at "https://maven.playpro.com",
  "repo.spring.io" at "https://repo.spring.io/plugins-release/",
  "repo.spongepowered.org" at "https://repo.spongepowered.org/maven",
  "repo.maven.apache.org" at "https://repo.maven.apache.org/maven2",
  "hub.spigotmc.org" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
  "oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots",
  "nexus.okkero.com" at "https://nexus.okkero.com/repository/maven-releases/",
  "maven.elmakers.com" at "https://maven.elmakers.com/repository/", // spigot-api 1.12.2がhub.spigotmc.orgからダウンロードできなくなったため
  "repo.phoenix616.dev" at "https://repo.phoenix616.dev", // authlibのための
  // ajd4jpのミラーのため
  "jitpack.io" at "https://jitpack.io",
  "scala-snapshots" at "https://scala-ci.typesafe.com/artifactory/scala-pr-validation-snapshots/",
)

// もし新しいライブラリを追加する場合、現在はプレリリース版を使っているので`... cross CrossVersion.binary`としないとmavenからバージョンを引っ張ってこられずに死ぬので注意
// TODO: Scala 2.13.11がリリースされたらCrossVersion.binaryは要らなくなるので取り外す
val providedDependencies = Seq(
  "org.jetbrains" % "annotations" % "17.0.0",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "commons-codec" % "commons-codec" % "1.15",
  "org.spigotmc" % "spigot-api" % "1.12.2-R0.1-SNAPSHOT",
  // https://maven.enginehub.org/repo/com/sk89q/worldedit/worldedit-bukkit/
  "com.sk89q.worldguard" % "worldguard-legacy" % "6.2",
  "net.coreprotect" % "coreprotect" % "2.14.2",
  "com.mojang" % "authlib" % "1.6.25",

  // no runtime
  "org.typelevel" %% "simulacrum" % "1.0.1" cross CrossVersion.binary
).map(_ % "provided")

val testDependencies = Seq(
  "org.scalamock" %% "scalamock" % "4.4.0" cross CrossVersion.binary,
  "org.scalatest" %% "scalatest" % "3.2.16" cross CrossVersion.binary,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" cross CrossVersion.binary,
  // テスト用のTestSchedulerを使うため
  "io.monix" %% "monix" % "3.4.1" cross CrossVersion.binary,
).map(_ % "test")

val dependenciesToEmbed = Seq(
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.3.0" cross CrossVersion.binary,

  // DB
  "org.mariadb.jdbc" % "mariadb-java-client" % "3.1.4",
  "org.flywaydb" % "flyway-core" % "5.2.4",
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0" cross CrossVersion.binary,

  // redis
  "com.github.etaty" %% "rediscala" % "1.9.0" cross CrossVersion.binary,

  // effect system
  "org.typelevel" %% "cats-core" % "2.9.0" cross CrossVersion.binary,
  "org.typelevel" %% "cats-effect" % "2.5.5" cross CrossVersion.binary,
  "co.fs2" %% "fs2-core" % "2.5.11" cross CrossVersion.binary,

  // algebra
  "io.chrisdavenport" %% "log4cats-core" % "1.1.1" cross CrossVersion.binary,
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1" cross CrossVersion.binary,
  "io.chrisdavenport" %% "cats-effect-time" % "0.1.2" cross CrossVersion.binary,

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.slf4j" % "slf4j-jdk14" % "1.7.36",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2",

  // type-safety utils
  "eu.timepit" %% "refined" % "0.10.3" cross CrossVersion.binary,
  "com.beachape" %% "enumeratum" % "1.7.2" cross CrossVersion.binary,

  // protobuf
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion cross CrossVersion.binary,

  // JSON
  "io.circe" %% "circe-core" % "0.14.5" cross CrossVersion.binary,
  "io.circe" %% "circe-generic" % "0.14.5" cross CrossVersion.binary,
  "io.circe" %% "circe-parser" % "0.14.5" cross CrossVersion.binary,

  // ajd4jp
  "com.github.KisaragiEffective" % "ajd4jp-mirror" % "8.0.2.2021",

  // Sentry
  "io.sentry" % "sentry" % "6.19.1"
)

// endregion

// region assemblyで含む依存関係の処理

// localDependenciesはprovidedとして扱い、jarに埋め込まない
assembly / assemblyExcludedJars := {
  (assembly / fullClasspath).value.filter { a =>
    def directoryContainsFile(directory: File, file: File) =
      file.absolutePath.startsWith(directory.absolutePath)

    directoryContainsFile(baseDirectory.value / "localDependencies", a.data)
  }
}

// protocol配下とルートのLICENSEが衝突してCIが落ちる
// cf. https://github.com/sbt/sbt-assembly/issues/141
assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last endsWith "LICENSE" => MergeStrategy.rename
  case PathList("org", "apache", "commons", "logging", xs @ _*) =>
    MergeStrategy.last
  case otherFile =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(otherFile)
}

// endregion

// region プラグインJarに埋め込むリソースの処理

val tokenReplacementMap =
  settingKey[Map[String, String]]("Map specifying what tokens should be replaced to")

tokenReplacementMap := Map("name" -> name.value, "version" -> version.value)

val filesToBeReplacedInResourceFolder = Seq("plugin.yml")

val filteredResourceGenerator = taskKey[Seq[File]]("Resource generator to filter resources")

Compile / filteredResourceGenerator :=
  filterResources(
    filesToBeReplacedInResourceFolder,
    tokenReplacementMap.value,
    (Compile / resourceManaged).value,
    (Compile / resourceDirectory).value
  )

Compile / resourceGenerators += (Compile / filteredResourceGenerator)

// トークン置換を行ったファイルをunmanagedResourcesのコピーから除外する
unmanagedResources / excludeFilter :=
  filesToBeReplacedInResourceFolder.foldLeft((unmanagedResources / excludeFilter).value)(
    _.||(_)
  )

// endregion

// region ScalaPBの設定

Compile / PB.protoSources := Seq(baseDirectory.value / "protocol")
Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value / "scalapb")

// endregion

// region 各プロジェクトの設定

lazy val root = (project in file(".")).settings(
  name := "SeichiAssist",
  assembly / assemblyOutputPath := baseDirectory.value / "target" / "build" / "SeichiAssist.jar",
  libraryDependencies := providedDependencies ++ testDependencies ++ dependenciesToEmbed,
  excludeDependencies := Seq(ExclusionRule(organization = "org.bukkit", name = "bukkit")),
  unmanagedBase := baseDirectory.value / "localDependencies",
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-unchecked",
    "-language:higherKinds",
    "-deprecation",
    "-Ypatmat-exhaust-depth",
    "320",
    "-Ymacro-annotations",
    "-Ywarn-unused"
  ),
  javacOptions ++= Seq("-encoding", "utf8"),
  assembly / assemblyShadeRules ++= Seq(
    ShadeRule.rename("org.mariadb.jdbc.**" -> "com.github.unchama.seichiassist.relocateddependencies.org.mariadb.jdbc.@1").inAll
  )
)

// endregion
