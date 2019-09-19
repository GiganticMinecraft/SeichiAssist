import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    java
    scala
    maven
    kotlin("jvm").version("1.3.40")
    id("nebula.dependency-lock").version("2.2.4")
    id("org.jetbrains.kotlin.kapt").version("1.3.40")
}

group = "click.seichi"
version = "1.1.4"
description = """ギガンティック☆整地鯖の独自要素を司るプラグイン"""

project.sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")

        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src/main/java")
        }
    }
    getByName("test") {
        java.srcDir("src/test/java")

        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src/test/java")
        }
    }
}

repositories {
    maven { url = URI("https://jitpack.io") }
    maven { url = URI("http://maven.sk89q.com/repo/") }
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

configurations.implementation { extendsFrom(embed) }

dependencies {
    implementation(fileTree(mapOf("dir" to "localDependencies", "include" to arrayOf("*.jar"))))

    implementation("org.jetbrains:annotations:17.0.0")

    implementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    implementation("org.apache.commons:commons-lang3:3.9")

    implementation("commons-codec:commons-codec:1.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("junit:junit:4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    embed("org.flywaydb:flyway-core:5.2.4")
    embed(kotlin("stdlib-jdk8"))
    embed("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")

    embed("com.okkero.skedule:skedule:1.2.6")

    embed("org.scala-lang:scala-library:2.11.6")

    // arrow依存
    val arrowVersion = "0.9.0"
    embed("io.arrow-kt:arrow-core-data:$arrowVersion")
    embed("io.arrow-kt:arrow-core-extensions:$arrowVersion")
    embed("io.arrow-kt:arrow-syntax:$arrowVersion")
    embed("io.arrow-kt:arrow-typeclasses:$arrowVersion")
    embed("io.arrow-kt:arrow-extras-data:$arrowVersion")
    embed("io.arrow-kt:arrow-extras-extensions:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    embed("io.arrow-kt:arrow-effects-data:$arrowVersion")
    embed("io.arrow-kt:arrow-effects-extensions:$arrowVersion")
    embed("io.arrow-kt:arrow-effects-io-extensions:$arrowVersion")
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

tasks.jar {
    // Configurationをコピーしないと変更を行っているとみなされて怒られる
    val embedConfiguration = embed.copy()

    from(embedConfiguration.map { if (it.isDirectory) it else zipTree(it) })
}

val compilerArgument = listOf("-Xlint:unchecked", "-Xlint:deprecation")
val kotlinCompilerArgument = listOf("-Xjsr305=strict")

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = compilerArgument + kotlinCompilerArgument
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = compilerArgument + kotlinCompilerArgument
}

val compileJava: JavaCompile by tasks
compileJava.options.compilerArgs.addAll(compilerArgument)

