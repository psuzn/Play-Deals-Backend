package me.sujanpoudel.playdeals.api

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.api.deals.appDealsApi
import me.sujanpoudel.playdeals.logger
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
    router.route("/api/deals/*").subRouter(appDealsApi(di, vertx))

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config.api.port)
      .await()

    logger.info("API server running at : ${config.api.port}")
  }
}
