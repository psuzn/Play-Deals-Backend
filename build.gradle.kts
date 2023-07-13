@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
  kotlin("jvm") version Versions.KOTLIN
  id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
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

task("preCommitHook") {
  dependsOn(tasks.ktlintFormat)
  dependsOn(tasks.ktlintCheck)
}

task("installPreCommitHook") {
  delete(File(projectDir, ".git/hooks/pre-commit"))
  copy {
    from(File(projectDir, "pre-commit"))
    into(File(projectDir, ".git/hooks"))
    fileMode = 0b111101101
  }
}

tasks.withType<Assemble>() {
  dependsOn("installPreCommitHook")
}
