@file:Suppress("SpellCheckingInspection")

// Common tasks
// Generate badges: ./gradlew :generateBadges

plugins {
    kotlin("multiplatform") version "2.0.0"
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.0"
}

group = "com.sztorm"
version = "2.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.9.2")
                implementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
            }
        }
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

tasks.register<GenerateBadges>("generateBadges") {
    testCoverageInputFile.set(project.layout.buildDirectory.file("reports/kover/report.xml"))
    testCoverageBadgeOutputFile.set(project.layout.projectDirectory.file("misc/testCoverage.svg"))
    dependsOn("koverXmlReport")
}