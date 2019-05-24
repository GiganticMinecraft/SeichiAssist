import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    java
    maven
    kotlin("jvm").version("1.3.30")
    id("nebula.dependency-lock").version("2.2.4")
    id("org.jetbrains.kotlin.kapt").version("1.3.30")
}

group = "click.seichi"
version = "1.1.1"
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

    // arrow依存
    val arrowVersion = "0.9.0"
    compile("io.arrow-kt:arrow-core-data:$arrowVersion")
    compile("io.arrow-kt:arrow-core-extensions:$arrowVersion")
    compile("io.arrow-kt:arrow-syntax:$arrowVersion")
    compile("io.arrow-kt:arrow-typeclasses:$arrowVersion")
    kapt("io.arrow-kt:arrow-annotations-processor:$arrowVersion")
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjsr305=strict")
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjsr305=strict")
}