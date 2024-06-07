@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.shadow) apply false
  alias(libs.plugins.jib) apply false
  alias(libs.plugins.ktlint)
}

allprojects {
  apply<JacocoPlugin>()
  apply<KtlintPlugin>()

  repositories {
    mavenCentral()
  }

  task("preCommitHook") {
    dependsOn(tasks.ktlintCheck)
  }

  extensions.configure<KtlintExtension> {
    version = rootProject.libs.versions.ktlint.get()
    enableExperimentalRules = false
    coloredOutput = true

    filter {
      exclude {
        it.file.absoluteFile.startsWith(layout.buildDirectory.asFile.get().absolutePath)
      }
    }
  }

  tasks.withType<KotlinCompile> {
    compilerOptions {
      allWarningsAsErrors.set(true)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(
      TestLogEvent.PASSED,
      TestLogEvent.SKIPPED,
      TestLogEvent.FAILED,
    )
  }
}

task("installPreCommitHook") {
  delete(File(projectDir, ".git/hooks/pre-commit"))
  copy {
    from(File(projectDir, "pre-commit"))
    into(File(projectDir, ".git/hooks"))
    fileMode = 0b111101101
  }
}

tasks.withType<Assemble> {
  dependsOn("installPreCommitHook")
}
