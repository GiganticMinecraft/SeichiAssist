import java.io._

import ResourceFilter.filterResources
import sbt.Keys.baseDirectory

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "1.2.7"
ThisBuild / organization     := "click.seichi"
ThisBuild / description      := "ギガンティック☆整地鯖の独自要素を司るプラグイン"

resolvers ++= Seq(
  "jitpack.io"             at "https://jitpack.io",
  "maven.sk89q.com"        at "https://maven.sk89q.com/repo/",
  "maven.playpro.com"      at "https://maven.playpro.com",
  "repo.spring.io"         at "https://repo.spring.io/plugins-release/",
  "repo.spongepowered.org" at "https://repo.spongepowered.org/maven",
  "repo.maven.apache.org"  at "https://repo.maven.apache.org/maven2",
  "hub.spigotmc.org"       at "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
  "oss.sonatype.org"       at "https://oss.sonatype.org/content/repositories/snapshots",
  "nexus.okkero.com"       at "https://nexus.okkero.com/repository/maven-releases/"
)

val providedDependencies = Seq(
  "org.jetbrains" % "annotations" % "17.0.0",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "commons-codec" % "commons-codec" % "1.12",
  "org.spigotmc" % "spigot-api" % "1.12.2-R0.1-SNAPSHOT",
  "com.sk89q.worldguard" % "worldguard-legacy" % "6.2",
  "net.coreprotect" % "coreprotect" % "2.14.2"
).map(_ % "provided")

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.1.0"
).map(_ % "test")

val dependenciesToEmbed = Seq(
  "org.flywaydb" % "flyway-core" % "5.2.4",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "eu.timepit" %% "refined" % "0.9.10",
  "com.beachape" %% "enumeratum" % "1.5.13"
)

// localDependenciesはprovidedとして扱い、jarに埋め込まない
assemblyExcludedJars in assembly := {
  (fullClasspath in assembly).value
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

filteredResourceGenerator in Compile :=
  filterResources(
    filesToBeReplacedInResourceFolder,
    tokenReplacementMap.value,
    (resourceManaged in Compile).value, (resourceDirectory in Compile).value
  )

resourceGenerators in Compile += (filteredResourceGenerator in Compile)

unmanagedResources in Compile += baseDirectory.value / "LICENSE"

// トークン置換を行ったファイルをunmanagedResourcesのコピーから除外する
excludeFilter in unmanagedResources :=
  filesToBeReplacedInResourceFolder.foldLeft((excludeFilter in unmanagedResources).value)(_.||(_))

lazy val root = (project in file("."))
  .settings(
    name := "SeichiAssist",
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "build" / s"SeichiAssist-${version.value}.jar",
    libraryDependencies := providedDependencies ++ testDependencies ++ dependenciesToEmbed,
    unmanagedBase := baseDirectory.value / "localDependencies",
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-deprecation",
      "-Ypatmat-exhaust-depth", "320",
    ),
    javacOptions ++= Seq("-encoding", "utf8")
  )
