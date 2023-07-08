package me.sujanpoudel.playdeals

enum class Environment {
  PRODUCTION,
  DEVELOPMENT,
  TEST
}

data class Conf(
  val db: DB,
  val app: App,
  val environment: Environment,
  val cors: String
) {

  data class DB(
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String,
    val poolSize: Int,
  )

  data class App(val port: Int)
}
