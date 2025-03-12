import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "kdi"
            isStatic = true
        }
    }

    cocoapods {
        name = "kdi"
        summary = ""
        homepage = "https://github.com/croccio/kdi.git"
        authors = "croccio"
        version = libs.versions.library.version.get()
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "kdi"
            isStatic = false
            transitiveExport = true
        }
        specRepos {
            url("https://github.com/croccio/kdi.git")
        }
        publishDir = rootProject.file("./")
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "kdi"
        binaries.library()
    }

    jvm("desktop")

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting
        val wasmJsMain by getting
        val nativeMain by getting
        val desktopMain by getting
        val androidMain by getting

        val jvmMain by creating {
            dependsOn(commonMain)
        }
        androidMain.dependsOn(jvmMain)
        androidMain.dependencies { }
        commonMain.dependencies { }

        val nonJvmMain by creating {
            dependsOn(commonMain)
            nativeMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies { }
        }
        desktopMain.dependsOn(jvmMain)
        desktopMain.dependencies { }
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {

    pom {
        name = "kdi"
        description = "KDI (Kotlin Dependency Injection) allows you to inject dependencies dynamically, without annotations, and with maximum flexibility."
        inceptionYear = "2025"
        url = "https://github.com/croccio/"
        licenses {
            license {
                name = "MIT"
                url = "https://github.com/croccio/kdi/blob/main/LICENSE"
                distribution = "https://github.com/croccio/kdi/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "croccio"
                name = "croccio"
                url = "https://github.com/croccio/"
            }
        }
        scm {
            url = "https://github.com/croccio/kdi/"
            connection = "scm:git:git://github.com/croccio/kdi.git"
            developerConnection = "scm:git:ssh://git@github.com/croccio/kdi.git"
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