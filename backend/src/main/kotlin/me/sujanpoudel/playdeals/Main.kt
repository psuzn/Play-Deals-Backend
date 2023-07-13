package me.sujanpoudel.playdeals

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.runCatching
import io.vertx.core.Vertx
import me.sujanpoudel.playdeals.common.asEnumOrNull
import mu.KLogger
import mu.KotlinLogging
import org.kodein.di.direct
import org.kodein.di.instance

val log: KLogger = KotlinLogging.logger {}

class BootstrapException(val violations: List<String>) : RuntimeException()

fun main() {
  val vertx = Vertx.vertx()
  val conf = when (val result = buildConf(System.getenv())) {
    is Ok -> result.value
    is Err -> {
      (result.error as BootstrapException).violations.forEach(::println)
      return
    }
  }

  val di = DIConfigurer.configure(vertx, conf)

  vertx.deployVerticle(di.direct.instance<MainVerticle>())
    .onSuccess { log.info("Deployed MainVerticle : $it") }
    .onFailure {
      log.error(it) { "Error deploying main verticle" }
      vertx.close()
    }
  di.direct.instance<ObjectMapper>()
}

fun buildConf(env: Map<String, String>) = runCatching {
  val violations = mutableListOf<String>()

  val environment = env.getOrDefault("ENV", Environment.PRODUCTION.name).asEnumOrNull<Environment>()

  if (environment == null) {
    violations += "Invalid ENV"
  }

  val dashboardEnabled = env.getOrDefault("DASHBOARD", "true").toBooleanStrictOrNull()
  if (dashboardEnabled == null) {
    violations += "Invalid DASHBOARD"
  }

  val appPort = (env.getOrDefault("APP_PORT", "8888")).toIntOrNull()

  if (appPort == null) {
    violations += "Invalid APP_PORT"
  }

  val dbPort = env.getOrDefault("DB_PORT", "5432").toIntOrNull()
  if (dbPort == null) {
    violations += "Invalid DB_PORT"
  }

  val dbName = env.getOrDefault("DB_NAME", "play_deals")
  val dbPoolSize = (env["DB_POOL_SIZE"] ?: "5").toIntOrNull()
  if (dbPoolSize == null) {
    violations += "Invalid DB_POOL_SIZE"
  }

  fun withEnvVar(envVarName: String, block: (String) -> Unit) {
    val value = env[envVarName]
    if (value.isNullOrBlank()) {
      violations += "No $envVarName env var defined!".also { log.error { it } }
    } else {
      block(value)
    }
  }

  lateinit var dbHost: String
  lateinit var dbUsername: String
  withEnvVar("DB_HOST") { dbHost = it }
  withEnvVar("DB_USERNAME") { dbUsername = it }

  if (violations.isNotEmpty()) {
    throw BootstrapException(violations)
  } else {
    Conf(
      api = Conf.Api(
        appPort!!,
        cors = env.getOrDefault("CORS", ".*.")
      ),
      environment = environment!!,
      db = Conf.DB(
        host = dbHost,
        port = dbPort!!,
        name = dbName,
        username = dbUsername,
        password = env.getOrDefault("DB_PASSWORD", "password"),
        poolSize = dbPoolSize!!
      ),
      backgroundTask = Conf.BackgroundTask(
        dashboardEnabled!!,
        env.getOrDefault("DASHBOARD_USER", "admin"),
        env.getOrDefault("DASHBOARD_PASS", "admin")
      )
    )
  }
}
