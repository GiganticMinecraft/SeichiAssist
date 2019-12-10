import org.apache.tools.ant.filters.ReplaceTokens
import java.net.URI

plugins {
    java
    scala
    maven
}

group = "click.seichi"
version = "1.2.4"
description = """ギガンティック☆整地鯖の独自要素を司るプラグイン"""

project.sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")
    }
    getByName("test") {
        java.srcDir("src/test/java")
    }
}

repositories {
    maven { url = URI("https://jitpack.io") }
    maven { url = URI("http://maven.sk89q.com/repo/") }
    maven { url = URI("http://maven.playpro.com") }
    maven { url = URI("http://repo.spring.io/plugins-release/") }
    maven { url = URI("https://repo.spongepowered.org/maven") }
    maven { url = URI("https://repo.maven.apache.org/maven2") }
    maven { url = URI("https://hub.spigotmc.org/nexus/content/repositories/snapshots")}
    maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots")}
    maven {
        name = "okkero's repository"
        url = URI("http://nexus.okkero.com/repository/maven-releases/")
    }
    jcenter()
    mavenCentral()
}

val embed: Configuration by configurations.creating
val scalacPlugin: Configuration by configurations.creating

configurations.implementation { extendsFrom(embed) }

val scalaVersion = "2.13"
val scalaVersionFull = "2.13.1"

dependencies {
    implementation(fileTree(mapOf("dir" to "localDependencies", "include" to arrayOf("*.jar"))))

    implementation("org.jetbrains:annotations:17.0.0")

    implementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    implementation("com.sk89q.worldguard:worldguard-legacy:6.2")
    implementation("net.coreprotect:coreprotect:2.14.2")

    implementation("org.apache.commons:commons-lang3:3.9")

    implementation("commons-codec:commons-codec:1.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("junit:junit:4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    embed("org.flywaydb:flyway-core:5.2.4")

    embed("org.scala-lang:scala-library:${scalaVersionFull}")

    // cats依存
    embed("org.typelevel:cats-core_${scalaVersion}:2.0.0")
    embed("org.typelevel:cats-effect_${scalaVersion}:2.0.0")

    embed("eu.timepit:refined_${scalaVersion}:0.9.10")

    embed("com.beachape:enumeratum_${scalaVersion}:1.5.13")

    // compiler plugins
    scalacPlugin(group = "org.typelevel", name = "kind-projector_${scalaVersionFull}", version = "0.11.0")
}

task("repl", JavaExec::class) {
    main = "scala.tools.nsc.MainGenericRunner"
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    args = listOf("-usejavacp")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    from(sourceSets.main.get().resources.srcDirs) {
        include("**")

        val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.rootProject.name
        )

        filter<ReplaceTokens>("tokens" to tokenReplacementMap)
    }
    from(projectDir) { include("LICENSE") }
}


tasks.withType(JavaCompile::class.java).all {
    this.options.encoding = "UTF-8"
}

tasks.withType(ScalaCompile::class.java).all {
    this.scalaCompileOptions.additionalParameters = listOf(
        "-Ypatmat-exhaust-depth", "40"
    ).plus(scalacPlugin.copy().flatMap { listOf("-Xplugin", it.path) })

    this.scalaCompileOptions.forkOptions.jvmArgs = listOf("-Xss64m")
    this.options.encoding = "UTF-8"

    val compilerArgument = listOf(
        "-Xlint:unchecked",
        "-Xlint:deprecation"
    )
    this.options.compilerArgs.addAll(compilerArgument)
}

tasks.jar {
    // Configurationをコピーしないと変更を行っているとみなされて怒られる
    val embedConfiguration = embed.copy()

    from(embedConfiguration.map { if (it.isDirectory) it else zipTree(it) })
}
