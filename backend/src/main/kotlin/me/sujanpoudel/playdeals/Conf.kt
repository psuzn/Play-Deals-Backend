package me.sujanpoudel.playdeals

enum class Environment {
  PRODUCTION,
  DEVELOPMENT,
  TEST
}

data class Conf(
  val db: DB,
  val api: Api,
  val environment: Environment,
  val backgroundTask: BackgroundTask
) {
  data class DB(
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String,
    val poolSize: Int
  )

  data class BackgroundTask(
    val dashboardEnabled: Boolean,
    val dashboardUserName: String,
    val dashboardPassword: String
  )

  data class Api(val port: Int, val cors: String)
}
