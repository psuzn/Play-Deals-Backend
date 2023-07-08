package me.sujanpoudel.playdeals

import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.flywaydb.core.Flyway

class FlywayVerticle(private val flyway: Flyway) : CoroutineVerticle() {

  override suspend fun start() {
    flyway.migrate()
  }
}
