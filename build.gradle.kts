import io.papermc.paperweight.checkstyle.PaperCheckstyleExt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.24"
}

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("nabulus-server/build.gradle.kts")
            patchFile = file("nabulus-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("nabulus-api/build.gradle.kts")
            patchFile = file("nabulus-api/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-checkstyle/build.gradle.kts"
            outputFile = file("nabulus-checkstyle/build.gradle.kts")
            patchFile = file("nabulus-checkstyle/build.gradle.kts.patch")
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("nabulus-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchDir("paperCheckstyle") {
            upstreamPath = "paper-checkstyle"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("nabulus-checkstyle/paper-patches")
            outputDir = file("paper-checkstyle")
        }
        patchDir("paperCheckstyleConfig") {
            upstreamPath = ".checkstyle"
            patchesDir = file("nabulus-checkstyle/config-patches")
            outputDir = file(".checkstyle")
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    if (name !in setOf("nabulus-server", "paper-server")) {
        apply { plugin("io.papermc.paperweight.paper-checkstyle") }
        extensions.configure<PaperCheckstyleExt> {
            typeUseAnnotationsFile.set(rootProject.layout.projectDirectory.file(".checkstyle/type_use_annotations.txt"))
        }
        dependencies {
            "checkstyle"(project(":nabulus-checkstyle"))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            /*
            maven("https://repo.papermc.io/repository/maven-snapshots/") {
                name = "paperSnapshots"
                credentials(PasswordCredentials::class)
            }
             */
        }
    }
}
