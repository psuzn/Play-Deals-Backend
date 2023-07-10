package me.sujanpoudel.playdeals.api

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.api.health.healthApi
import me.sujanpoudel.playdeals.jobs.AppDetailScrapper
import me.sujanpoudel.playdeals.log
import org.jobrunr.scheduling.BackgroundJobRequest
import org.jobrunr.scheduling.JobScheduler
import org.kodein.di.DirectDI
import org.kodein.di.instance
import java.util.UUID

class ApiVerticle(
  private val di: DirectDI
) : CoroutineVerticle() {

  var id = 0

  override suspend fun start() {

    val config = di.instance<Conf>()
    val router = Router.router(vertx)
    val jobScheduler = di.instance<JobScheduler>()
    router.route().handler(CorsHandler.create().addRelativeOrigin(config.api.cors))
    router.route("/health/*").subRouter(healthApi(di, vertx))

    router.get("/").handler {

      id++

      val uuid = UUID.nameUUIDFromBytes(it.toString().encodeToByteArray())

      BackgroundJobRequest.enqueue(uuid, AppDetailScrapper.Request("$id"))

      it.response().setStatusCode(200).send("job $id scheduled")
    }

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config.api.port)
      .await()

    log.info("API server running at : ${config.api.port}")
  }
}
