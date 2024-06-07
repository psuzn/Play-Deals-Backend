@file:Suppress("UNCHECKED_CAST")

package me.sujanpoudel.playdeals.common

import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.Environment
import me.sujanpoudel.playdeals.logger
import java.util.Base64

class BootstrapException(val violations: List<String>) : RuntimeException()

fun buildConf(envs: Map<String, String>) = com.github.michaelbull.result.runCatching {
  val violations = mutableListOf<String>()

  @Suppress("UNCHECKED_CAST")
  fun <T> env(envVarName: String, default: String? = null, converter: (String) -> T? = { it as? T }): T? = (
    envs[envVarName] ?: default ?: run {
      violations += "No '$envVarName' env var defined!".also { logger.error { it } }
      null
    }
  )?.let(converter) ?: run {
    violations += "Invalid '$envVarName'"
    null
  }

  val environment = env("ENV", Environment.PRODUCTION.name) { it.asEnumOrNull<Environment>() }

  val appPort = env("APP_PORT", "8888") { it.toIntOrNull() }
  val cors = env<String>("CORS", ".*.")

  val dbPort = env("DB_PORT", "5432") { it.toIntOrNull() }
  val dbName = env<String>("DB_NAME", "play_deals")
  val dbPoolSize = env("DB_POOL_SIZE", "5") { it.toIntOrNull() }
  val dbHost = env<String>("DB_HOST")
  val dbUsername = env<String>("DB_USERNAME")
  val dbPassword = env<String>("DB_PASSWORD", "password")

  val dashboardEnabled = env("DASHBOARD", "true") { it.toBooleanStrictOrNull() }
  val dashboardUser = env<String>("DASHBOARD_USER", "admin")
  val dashboardPassword = env<String>("DASHBOARD_PASS", "admin")

  val firebaseAuthCredential =
    env("FIREBASE_ADMIN_AUTH_CREDENTIALS") {
      Base64.getDecoder().decode(it).decodeToString()
    }

  val forexApiKey = env<String>("FOREX_API_KEY")

  if (violations.isNotEmpty()) {
    throw BootstrapException(violations)
  } else {
    Conf(
      api = Conf.Api(appPort!!, cors = cors!!),
      environment = environment!!,
      db =
        Conf.DB(
          host = dbHost!!,
          port = dbPort!!,
          name = dbName!!,
          username = dbUsername!!,
          password = dbPassword!!,
          poolSize = dbPoolSize!!,
        ),
      backgroundTask =
        Conf.BackgroundTask(
          dashboardEnabled = dashboardEnabled!!,
          dashboardUserName = dashboardUser!!,
          dashboardPassword = dashboardPassword!!,
        ),
      firebaseAuthCredential = firebaseAuthCredential!!,
      forexApiKey = forexApiKey!!,
    )
  }
}
