@file:Suppress("SpellCheckingInspection")

// Common tasks
// Generate badges: ./gradlew --continue generateBadges

plugins {
    kotlin("multiplatform") version "2.0.0"
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.0"
}

group = "com.sztorm"
version = "2.0.1"

repositories {
    mavenCentral()
}

val sourcesJar by tasks.named("sourcesJar")

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
            artifactId = project.name
            version = project.version as String
            groupId = project.group as String
            artifact(file("./build/libs/${project.name}-${project.version}.jar"))
            artifact(sourcesJar)
        }
    }
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

//./gradlew --continue jvmTest generateTestStatusBadge
tasks.register<GenerateTestsStatusBadge>("generateTestStatusBadge") {
    testsStatusReportInput.set(project.layout.buildDirectory.file("reports/tests/jvmTest/index.html"))
    badgeOutput.set(project.layout.projectDirectory.file("misc/testsStatus.svg"))
    mustRunAfter("jvmTest")
}

//./gradlew --continue koverXmlReport generateCoverageBadge
tasks.register<GenerateCoverageBadge>("generateCoverageBadge") {
    testsStatusReportInput.set(project.layout.buildDirectory.file("reports/tests/jvmTest/index.html"))
    coverageReportInput.set(project.layout.buildDirectory.file("reports/kover/report.xml"))
    badgeOutput.set(project.layout.projectDirectory.file("misc/testCoverage.svg"))
    mustRunAfter("koverXmlReport")
}

//./gradlew --continue generateBadges
tasks.register("generateBadges") {
    dependsOn("jvmTest", "generateTestStatusBadge", "koverXmlReport", "generateCoverageBadge")
    tasks.findByPath("koverXmlReport")!!.mustRunAfter("generateTestStatusBadge")
}