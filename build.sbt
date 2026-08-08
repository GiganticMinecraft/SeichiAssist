import ResourceFilter.filterResources

import java.io._

// region 全プロジェクト共通のメタデータ

ThisBuild / scalaVersion := "3.3.8"
// ThisBuild / version はGitHub Actionsによって取得/自動更新される。
// 次の行は ThisBuild / version := "(\d*)" の形式でなければならない。
ThisBuild / version := "106"
ThisBuild / organization := "click.seichi"
ThisBuild / description := "ギガンティック☆整地鯖の独自要素を司るプラグイン"

// Scalafixが要求するため、semanticdbは有効化する
// Scala 3ではコンパイラ内蔵の-Xsemanticdbが使われるため、semanticdbVersionの指定は不要
ThisBuild / semanticdbEnabled := true

// endregion

// region 雑多な設定

// テストが落ちた時にスタックトレースを表示するため。
// ScalaTest のオプションは https://www.scalatest.org/user_guide/using_the_runner を参照のこと。
Compile / testOptions += Tests.Argument("-oS")

// endregion

// region 依存関係

resolvers ++= Seq(
  // ajd4jpのミラーのため
  "jitpack.io" at "https://jitpack.io",
  "maven.sk89q.com" at "https://maven.enginehub.org/repo/",
  "maven.playpro.com" at "https://maven.playpro.com",
  "repo.spring.io" at "https://repo.spring.io/plugins-release/",
  "repo.spongepowered.org" at "https://repo.spongepowered.org/maven",
  "repo.maven.apache.org" at "https://repo.maven.apache.org/maven2",
  "hub.spigotmc.org" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
  "oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots",
  "repo.phoenix616.dev" at "https://repo.phoenix616.dev" // authlibのため
)

val providedDependencies = Seq(
  "org.jetbrains" % "annotations" % "26.1.0",
  "org.apache.commons" % "commons-lang3" % "3.20.0",
  "commons-codec" % "commons-codec" % "1.22.1",
  "org.spigotmc" % "spigot-api" % "1.18.2-R0.1-SNAPSHOT",
  // https://maven.enginehub.org/repo/com/sk89q/worldedit/worldedit-bukkit/
  "com.sk89q.worldguard" % "worldguard-bukkit" % "7.0.7",
  "net.coreprotect" % "coreprotect" % "21.3",
  "com.mojang" % "authlib" % "6.0.59"
).map(_ % "provided")

val testDependencies = Seq(
  "org.scalamock" %% "scalamock" % "6.2.0",
  "org.scalatest" %% "scalatest" % "3.2.20",
  "org.scalatestplus" %% "scalacheck-1-19" % "3.2.20.0",
  // テスト用のTestSchedulerを使うため
  "io.monix" %% "monix" % "3.4.1"
).map(_ % "test")

val dependenciesToEmbed = Seq(
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.4.0",

  // DB
  "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.9",
  "org.flywaydb" % "flyway-core" % "13.0.0",
  "org.flywaydb" % "flyway-mysql" % "13.0.0",
  "org.scalikejdbc" %% "scalikejdbc" % "4.3.5",

  // redis
  "io.github.rediscala" %% "rediscala" % "2.1.0",

  // effect system
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.typelevel" %% "cats-effect" % "2.5.5",
  "co.fs2" %% "fs2-core" % "2.5.13",

  // algebra
  "org.typelevel" %% "log4cats-core" % "1.7.0",
  "org.typelevel" %% "log4cats-slf4j" % "1.7.0",
  "io.chrisdavenport" %% "cats-effect-time" % "0.1.3",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.slf4j" % "slf4j-jdk14" % "1.7.36",

  // type-safety utils
  "io.github.iltotore" %% "iron" % "3.3.2",
  "com.beachape" %% "enumeratum" % "1.9.8",

  // protobuf
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,

  // JSON
  "io.circe" %% "circe-core" % "0.14.16",
  "io.circe" %% "circe-generic" % "0.14.16",
  "io.circe" %% "circe-parser" % "0.14.16",

  // ajd4jp
  "com.github.KisaragiEffective" % "ajd4jp-mirror" % "8.0.2.2021",

  // Sentry
  "io.sentry" % "sentry" % "8.50.0"
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
  // cf. https://qiita.com/yokra9/items/1e72646623f962ce02ee と ChatGPTに聞いた
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case "module-info.class" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith "LICENSE" => MergeStrategy.rename
  case PathList("org", "apache", "commons", "logging", xs @ _*) => MergeStrategy.last
  case PathList("plugin.yml") => MergeStrategy.first
  case PathList("config.yml") => MergeStrategy.first
  case PathList("defaults", "config.yml") => MergeStrategy.first
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
Compile / PB.targets :=
  Seq(scalapb.gen(scala3Sources = true) -> (Compile / sourceManaged).value / "scalapb")

// endregion

// region 各プロジェクトの設定

lazy val root = (project in file(".")).settings(
  name := "SeichiAssist",
  assembly / assemblyOutputPath := baseDirectory.value / "target" / "build" / "SeichiAssist.jar",
  libraryDependencies := providedDependencies ++ testDependencies ++ dependenciesToEmbed,
  // src/scalafix配下のカスタムルール2つ（.scalafix.confにもCIにも未参照）のコンパイルを
  // 無効化する。scalafix-coreはScala 2.13向けにのみ公開されており、Scala 3コンパイラでは
  // コンパイルできない。再度有効化する場合は、ルールを別プロジェクトへ切り出して
  // 2.13でクロスビルドする必要がある。
  ScalafixConfig / sources := Nil,
  excludeDependencies := Seq(ExclusionRule(organization = "org.bukkit", name = "bukkit")),
  unmanagedBase := baseDirectory.value / "localDependencies",
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-unchecked",
    "-deprecation",
    // enumeratumのfindValuesがマクロでコンパニオンのツリーを参照するため
    "-Yretain-trees",
    "-Wunused:all",
    // implicit valの初期化が自分自身を暗黙引数として解決すると、フィールドに
    // 未初期化のnullが格納される実行時バグになる（例: 匿名クラス内の
    // `override protected implicit val F: Monad[F] = implicitly`）。
    // この種の警告は見逃すと危険なため、コンパイルエラーへ昇格させる。
    "-Wconf:msg=Infinite loop in function body:e"
  ),
  javacOptions ++= Seq("-encoding", "utf8"),
  assembly / assemblyShadeRules ++= Seq(
    ShadeRule
      .rename(
        "org.mariadb.jdbc.**" -> "com.github.unchama.seichiassist.relocateddependencies.org.mariadb.jdbc.@1"
      )
      .inAll
  ),
  // assemblyの実行時にテストを走らせる（sbt-assembly 1.0.0からはデフォルトで実行されない）。
  // assemblyタスクが参照するのは `assembly / test` スコープであることに注意。
  // 誤って `Compile / assembly / test` へ配線するとテストが実行されないままassemblyが成功する。
  assembly / test := (Test / test).value
)

// endregion
