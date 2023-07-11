package me.sujanpoudel.playdeals

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.api.ApiVerticle
import me.sujanpoudel.playdeals.jobs.BackgroundJobsVerticle

class MainVerticle(
  private val apiVerticle: ApiVerticle,
  private val flywayVerticle: FlywayVerticle,
  private val backgroundJobsVerticle: BackgroundJobsVerticle
) : CoroutineVerticle() {

  override suspend fun start() {
    vertx.deployVerticle(flywayVerticle).await()
    vertx.deployVerticle(backgroundJobsVerticle).await()
    vertx.deployVerticle(apiVerticle).await()
  }
}
