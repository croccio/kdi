import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

var androidTarget: String = ""

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    id("maven-publish")
}

group = libs.versions.library.group.get()
version = libs.versions.library.version.get()

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        name = "kdi"
        summary = ""
        homepage = "https://github.com/croccio/KDI-Kotlin-Dependency-Injection.git"
        authors = "croccio"
        version = libs.versions.library.version.get()
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "kdi"
            isStatic = false
            transitiveExport = true
        }
        specRepos {
            url("https://github.com/croccio/KDI-Kotlin-Dependency-Injection.git")
        }
        publishDir = rootProject.file("./")
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = libs.versions.library.group.get()
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    beforeEvaluate {
        libraryVariants.all {
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

publishing {
    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/croccio/KDI-Kotlin-Dependency-Injection")
            credentials {
                username = System.getenv()["MYUSER"]
                password = System.getenv()["MYPAT"]
            }
        }
    }
    val thePublications = listOf(androidTarget) + "kotlinMultiplatform"
    publications {
        matching { it.name in thePublications }.all {
            val targetPublication = this@all
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == targetPublication }
                .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
        }
        matching { it.name.contains("ios", true) }.all {
            val targetPublication = this@all
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == targetPublication }
                .forEach { it.enabled = false }
        }
    }
}

afterEvaluate {
    tasks.named("podPublishDebugXCFramework") {
        enabled = false
    }
    tasks.named("podSpecDebug") {
        enabled = false
    }
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("started", "skipped", "passed", "failed")
            showStandardStreams = true
        }
    }
}

val buildIdAttribute = Attribute.of("buildIdAttribute", String::class.java)
configurations.forEach {
    it.attributes {
        attribute(buildIdAttribute, it.name)
    }
}

val moveIosPodToRoot by tasks.registering {
    group = libs.versions.library.group.get()
    doLast {
        val releaseDir = rootProject.file(
            "./release"
        )
        releaseDir.copyRecursively(
            rootProject.file("./"),
            true
        )
        releaseDir.deleteRecursively()
    }
}

tasks.named("podPublishReleaseXCFramework") {
    finalizedBy(moveIosPodToRoot)
}

val publishPlatforms by tasks.registering {
    group = libs.versions.library.group.get()
    dependsOn(
        tasks.named("publishAndroidReleasePublicationToGithubRepository"),
        tasks.named("podPublishReleaseXCFramework")
    )
    doLast {
        exec { commandLine = listOf("git", "add", "-A") }
        exec {
            commandLine = listOf(
                "git",
                "commit",
                "-m",
                "iOS binary lib for version ${libs.versions.library.version.get()}"
            )
        }
        exec { commandLine = listOf("git", "push", "origin", "main") }
        exec { commandLine = listOf("git", "tag", libs.versions.library.version.get()) }
        exec { commandLine = listOf("git", "push", "--tags") }
        println("version ${libs.versions.library.version.get()} built and published")
    }
}

val compilePlatforms by tasks.registering {
    group = libs.versions.library.group.get()
    dependsOn(
        tasks.named("compileKotlinIosArm64"),
        tasks.named("compileKotlinIosX64"),
        tasks.named("compileKotlinIosSimulatorArm64"),
        tasks.named("compileReleaseKotlinAndroid")
    )
    doLast {
        println("Finished compilation")
    }
}