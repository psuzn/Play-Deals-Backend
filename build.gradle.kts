
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
  kotlin("jvm") version Versions.KOTLIN
  id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

buildscript {
  repositories {
    mavenCentral()
  }
}

allprojects {
  apply<KotlinPlatformJvmPlugin>()
  apply<JacocoPlugin>()
  apply<KtlintPlugin>()

  repositories {
    mavenCentral()
  }

  val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
      jvmTarget = "17"
    }
  }

  val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(
      TestLogEvent.PASSED,
      TestLogEvent.SKIPPED,
      TestLogEvent.FAILED
    )
  }
}
