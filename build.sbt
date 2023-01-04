import ResourceFilter.filterResources
import sbt.Keys.baseDirectory

import java.io._

// region 全プロジェクト共通のメタデータ

ThisBuild / scalaVersion := "3.2.1"
// ThisBuild / version はGitHub Actionsによって取得/自動更新される。
// 次の行は ThisBuild / version := "(\d*)" の形式でなければならない。
ThisBuild / version := "68"
ThisBuild / organization := "click.seichi"
ThisBuild / description := "ギガンティック☆整地鯖の独自要素を司るプラグイン"

// Scalafixが要求するため、semanticdbは有効化する
ThisBuild / semanticdbEnabled := true

// endregion

// region 雑多な設定

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
  "repo.phoenix616.dev" at "https://repo.phoenix616.dev" // authlibのための
)

val providedDependencies = Seq(
  "org.jetbrains" % "annotations" % "17.0.0",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "commons-codec" % "commons-codec" % "1.12",
  "org.spigotmc" % "spigot-api" % "1.12.2-R0.1-SNAPSHOT",
  // https://maven.enginehub.org/repo/com/sk89q/worldedit/worldedit-bukkit/
  "com.sk89q.worldguard" % "worldguard-legacy" % "6.2",
  "net.coreprotect" % "coreprotect" % "2.14.2",
  "com.mojang" % "authlib" % "1.5.25",

  // no runtime
  // "org.typelevel" %% "simulacrum" % "1.0.0"
).map(_ % "provided")

val testDependencies = Seq(
  // "org.scalamock" %% "scalamock" % "4.4.0",
  "org.scalatest" %% "scalatest" % "3.2.14",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
  // テスト用のTestSchedulerを使うため
  "io.monix" %% "monix" % "3.4.1"
).map(_ % "test")

val dependenciesToEmbed = Seq(
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.3.0",

  // DB
  "org.flywaydb" % "flyway-core" % "5.2.4",
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",

  // redis
  "com.github.etaty" % "rediscala_2.13" % "1.9.0",

  // effect system
  "org.typelevel" %% "cats-core" % "2.9.0",
  "org.typelevel" %% "cats-effect" % "2.5.5",
  "co.fs2" %% "fs2-core" % "2.5.10",

  // algebra
  "org.typelevel" %% "log4cats-core" % "1.7.0",
  "org.typelevel" %% "log4cats-slf4j" % "1.7.0",
  "io.chrisdavenport" %% "cats-effect-time" % "0.1.3",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.28",
  "org.slf4j" % "slf4j-jdk14" % "1.7.28",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2",

  // type-safety utils
  "eu.timepit" %% "refined" % "0.9.29",
  "com.beachape" %% "enumeratum" % "1.7.1",

  // protobuf
  "com.thesamet.scalapb" %% "scalapb-runtime" % "0.11.12",

  // JSON
  "io.circe" %% "circe-core" % "0.14.3",
  "io.circe" %% "circe-generic" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.3",
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

Compile / unmanagedResources += baseDirectory.value / "LICENSE"

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
    "-Ywarn-unused",
    "-source:3.0-migration",
    "-rewrite",
    "-Ykind-projector",
  ),
  javacOptions ++= Seq("-encoding", "utf8")
)

// endregion
