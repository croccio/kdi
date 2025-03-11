import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

var androidTarget: String = ""

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    alias(libs.plugins.vanniktech.mavenPublish)
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
        publishLibraryVariants("release", "debug")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/croccio/KDI-Kotlin-Dependency-Injection")
            credentials {
                username = System.getenv()["MYUSER"]
                password = System.getenv()["MYPAT"]
            }
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = libs.versions.library.group.get(),
        artifactId = "kdi",
        version = libs.versions.library.version.get()
    )

    pom {
        name = "KDI Kotlin Dependency Injection"
        description =
            "KDI (Kotlin Dependency Injection) allows you to inject dependencies dynamically, without annotations, and with maximum flexibility."
        inceptionYear = "2025"
        url = "https://github.com/croccio/KDI-Kotlin-Dependency-Injection"

        licenses {
            license {
                name = "MIT"
                url =
                    "https://github.com/croccio/KDI-Kotlin-Dependency-Injection?tab=MIT-1-ov-file#readme"
                distribution =
                    "https://github.com/croccio/KDI-Kotlin-Dependency-Injection?tab=MIT-1-ov-file#readme"
            }
        }

        developers {
            developer {
                id = "croccio"
                name = "croccio"
                url = "https://github.com/croccio"
            }
        }

        scm {
            url = "https://github.com/croccio/KDI-Kotlin-Dependency-Injection.git"
        }
    }
}

tasks.register("createTag") {
    val libVersion = libs.versions.library.version.get()
    doLast {
        exec { commandLine = listOf("git", "tag", libVersion) }
        exec { commandLine = listOf("git", "push", "--tags") }
    }
}