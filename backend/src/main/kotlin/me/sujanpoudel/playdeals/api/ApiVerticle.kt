package me.sujanpoudel.playdeals.api

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.api.health.healthApi
import me.sujanpoudel.playdeals.log
import org.kodein.di.DirectDI
import org.kodein.di.instance

class ApiVerticle(
  private val di: DirectDI
) : CoroutineVerticle() {

  override suspend fun start() {

    val config = di.instance<Conf>()
    val router = Router.router(vertx)
    router.route().handler(CorsHandler.create().addRelativeOrigin(config.api.cors))
    router.route("/health/*").subRouter(healthApi(di, vertx))

    router.route("/").handler {
      it.response().setStatusCode(200).send("OK")
    }

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config.api.port)
      .await()

    log.info("API server running at : ${config.api.port}")
  }
}
