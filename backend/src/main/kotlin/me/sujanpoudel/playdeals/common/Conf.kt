package me.sujanpoudel.playdeals.common

import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.Environment
import me.sujanpoudel.playdeals.logger
import java.util.Base64

class BootstrapException(val violations: List<String>) : RuntimeException()

fun buildConf(env: Map<String, String>) = com.github.michaelbull.result.runCatching {
  val violations = mutableListOf<String>()

  val environment = env.getOrDefault("ENV", Environment.PRODUCTION.name).asEnumOrNull<Environment>()

  if (environment == null) {
    violations += "Invalid ENV"
  }

  val dashboardEnabled = env.getOrDefault("DASHBOARD", "true").toBooleanStrictOrNull()
  if (dashboardEnabled == null) {
    violations += "Invalid DASHBOARD"
  }

  val appPort = env.getOrDefault("APP_PORT", "8888").toIntOrNull()

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

  fun envVar(envVarName: String): String? {
    val value = env[envVarName]

    return if (value.isNullOrBlank()) {
      violations += "No $envVarName env var defined!".also { logger.error { it } }
      null
    } else {
      value
    }
  }

  val dbHost: String = envVar("DB_HOST").orEmpty()
  val dbUsername: String = envVar("DB_USERNAME").orEmpty()
  val firebaseAuthCredential = envVar("FIREBASE_ADMIN_AUTH_CREDENTIALS")?.let {
    Base64.getDecoder().decode(it).decodeToString()
  }.orEmpty()

  if (violations.isNotEmpty()) {
    throw BootstrapException(violations)
  } else {
    Conf(
      api = Conf.Api(appPort!!, cors = env.getOrDefault("CORS", ".*.")),
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
      ),
      firebaseAuthCredential = firebaseAuthCredential
    )
  }
}
