import ResourceFilter.filterResources
import sbt.Keys.baseDirectory

import java.io._

ThisBuild / scalaVersion := "2.13.1"
// ThisBuild / version はGitHub Actionsによって自動更新される。
// 次の行は ThisBuild / version := "(\d*)" の形式でなければならない。
ThisBuild / version := "21"
ThisBuild / organization := "click.seichi"
ThisBuild / description := "ギガンティック☆整地鯖の独自要素を司るプラグイン"

resolvers ++= Seq(
  "jitpack.io" at "https://jitpack.io",
  "maven.sk89q.com" at "https://maven.sk89q.com/repo/",
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
  "com.sk89q.worldguard" % "worldguard-legacy" % "6.2",
  "net.coreprotect" % "coreprotect" % "2.14.2",
  "com.mojang" % "authlib" % "1.5.25",

  // no runtime
  "org.typelevel" %% "simulacrum" % "1.0.0"
).map(_ % "provided")

val testDependencies = Seq(
  "org.scalamock" %% "scalamock" % "4.4.0",
  "org.scalatest" %% "scalatest" % "3.2.2",
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0",
  // テスト用のTestSchedulerを使うため
  "io.monix" %% "monix" % "3.2.2"
).map(_ % "test")

val dependenciesToEmbed = Seq(
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.2.1",

  // DB
  "org.flywaydb" % "flyway-core" % "5.2.4",
  "org.scalikejdbc" %% "scalikejdbc" % "3.4.2",

  // redis
  "com.github.etaty" %% "rediscala" % "1.9.0",

  // effect system
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.0",
  "co.fs2" %% "fs2-core" % "2.5.0",

  // algebra
  "io.chrisdavenport" %% "log4cats-core" % "1.1.1",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
  "io.chrisdavenport" %% "cats-effect-time" % "0.1.2",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.28",
  "org.slf4j" % "slf4j-jdk14" % "1.7.28",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2",

  // type-safety utils
  "eu.timepit" %% "refined" % "0.9.10",
  "com.beachape" %% "enumeratum" % "1.5.13",

  // protobuf
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)

// localDependenciesはprovidedとして扱い、jarに埋め込まない
assembly / assemblyExcludedJars := {
  (assembly / fullClasspath).value
    .filter { a =>
      def directoryContainsFile(directory: File, file: File) =
        file.absolutePath.startsWith(directory.absolutePath)

      directoryContainsFile(baseDirectory.value / "localDependencies", a.data)
    }
}

val tokenReplacementMap = settingKey[Map[String, String]]("Map specifying what tokens should be replaced to")

tokenReplacementMap := Map(
  "name" -> name.value,
  "version" -> version.value
)

val filesToBeReplacedInResourceFolder = Seq("plugin.yml")

val filteredResourceGenerator = taskKey[Seq[File]]("Resource generator to filter resources")

Compile / filteredResourceGenerator :=
  filterResources(
    filesToBeReplacedInResourceFolder,
    tokenReplacementMap.value,
    (Compile / resourceManaged).value, (Compile / resourceDirectory).value
  )

Compile / resourceGenerators += (Compile / filteredResourceGenerator)

Compile / unmanagedResources += baseDirectory.value / "LICENSE"

// トークン置換を行ったファイルをunmanagedResourcesのコピーから除外する
unmanagedResources / excludeFilter :=
  filesToBeReplacedInResourceFolder.foldLeft((unmanagedResources / excludeFilter).value)(_.||(_))

logLevel := Level.Debug

// ScalaPBの設定
Compile / PB.protoSources := Seq(baseDirectory.value / "protocol")
Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value / "scalapb")

Compile / testOptions += Tests.Argument("-oS")

lazy val root = (project in file("."))
  .settings(
    name := "SeichiAssist",
    assembly / assemblyOutputPath := baseDirectory.value / "target" / "build" / s"SeichiAssist.jar",
    libraryDependencies := providedDependencies ++ testDependencies ++ dependenciesToEmbed,
    excludeDependencies := Seq(
      ExclusionRule(organization = "org.bukkit", name = "bukkit")
    ),
    unmanagedBase := baseDirectory.value / "localDependencies",
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-language:higherKinds",
      "-deprecation",
      "-Ypatmat-exhaust-depth", "320",
      "-Ymacro-annotations",
    ),
    javacOptions ++= Seq("-encoding", "utf8")
  )
