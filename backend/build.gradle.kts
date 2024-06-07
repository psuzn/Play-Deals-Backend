import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.jib)
  alias(libs.plugins.shadow)
}

group = Artifact.GROUP
version = "1.0.0-SNAPSHOT"

val mainVerticleName = "$group.MainVerticle"
val launcherClassName = "me.sujanpoudel.playdeals.MainKt"

val watchForChange = "src/**/*"
val doOnChange = "$projectDir/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

jib {
  from {
    image = "openjdk:17-slim"
  }

  to {
    image = "play-deals-backend"
  }

  container {
    mainClass = launcherClassName
    ports = listOf("8888", "8000")
    labels.put("org.opencontainers.image.source", "https://github.com/psuzn/play-deals-backend")
  }
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)

  implementation(platform(libs.vertx.depchain))

  with(libs.vertx) {
    implementation(core)
    implementation(web)
    implementation(pgClient)
    implementation(coroutines)
    implementation(kotlin)
    implementation(healthCheck)
    implementation(webClient)

    testImplementation(junit5)
  }

  implementation(libs.kotlinResult)

  implementation(libs.flyway.core)
  implementation(libs.flyway.postgresql)
  implementation(libs.postgresql)
  implementation(libs.scramOngressClient)

  implementation(libs.slf4j.api)
  implementation(libs.slf4j.simpe)
  implementation(libs.kotlinLoggingJvm)

  implementation(libs.jackson.databind)
  implementation(libs.jackson.moduleKotlin)
  implementation(libs.jackson.datatype.jsr310)

  implementation(libs.kodein)
  implementation(libs.jobrunr)
  implementation(libs.jobrunr.kotlin)
  implementation(libs.firebaseAdmin)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.mockk)

  with(libs.testcontainers) {
    testImplementation(this)
    testImplementation(junit)
    testImplementation(postgresql)
  }
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  archiveFileName.set("play-deals-backend.jar")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<JavaExec> {
  args =
    listOf(
      "run",
      mainVerticleName,
      "--redeploy=$watchForChange",
      "--launcher-class=$launcherClassName",
      "--on-redeploy=$doOnChange",
    )
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
  useJUnitPlatform()
}
