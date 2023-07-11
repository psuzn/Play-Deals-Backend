import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  id("com.github.johnrengelman.shadow") version Versions.SHADOW
  id("com.google.cloud.tools.jib") version "3.3.2"
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
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")

  implementation(platform("io.vertx:vertx-stack-depchain:${Versions.VERTX}"))
  "io.vertx".let { vertx ->
    implementation("$vertx:vertx-core")
    implementation("$vertx:vertx-web-graphql")
    implementation("$vertx:vertx-auth-jwt")
    implementation("$vertx:vertx-sql-client-templates")
    implementation("$vertx:vertx-web")
    implementation("$vertx:vertx-pg-client")
    implementation("$vertx:vertx-lang-kotlin-coroutines")
    implementation("$vertx:vertx-lang-kotlin")
    implementation("$vertx:vertx-health-check")
    implementation("$vertx:vertx-web-client")

    testImplementation("$vertx:vertx-junit5")
  }

  implementation("com.michael-bull.kotlin-result:kotlin-result:${Versions.KOTLIN_RESULT}")

  implementation("org.flywaydb:flyway-core:${Versions.FLYWAY}")
  implementation("org.postgresql:postgresql:${Versions.POSTGRES}")
  implementation("com.ongres.scram:client:${Versions.ONGRESS_SCARM}")

  implementation("org.slf4j:slf4j-api:${Versions.SLF4J}")
  implementation("org.slf4j:slf4j-simple:${Versions.SLF4J}")
  implementation("io.github.microutils:kotlin-logging-jvm:${Versions.JVM_LOGGER}")

  implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JACKSON}")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.JACKSON}")

  implementation("org.kodein.di:kodein-di:${Versions.KODEIN_DI}")

  implementation("org.jobrunr:jobrunr:${Versions.JOB_RUNNER}")
  implementation("org.jobrunr:jobrunr-kotlin-1.8-support:${Versions.JOB_RUNNER}")

  testImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT_JUPITER}")
  testImplementation("io.kotest:kotest-assertions-core:${Versions.KO_TEST}")
  with("org.testcontainers") {
    testImplementation("$this:testcontainers:${Versions.TEST_CONTAINERS}")
    testImplementation("$this:junit-jupiter:${Versions.TEST_CONTAINERS}")
    testImplementation("$this:postgresql:${Versions.TEST_CONTAINERS}")
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
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange"
  )
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
  useJUnitPlatform()
}
