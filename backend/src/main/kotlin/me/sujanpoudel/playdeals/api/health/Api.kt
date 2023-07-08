package me.sujanpoudel.playdeals.api.health

import io.vertx.core.Vertx
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DirectDI
import org.kodein.di.instance

fun healthApi(
  di: DirectDI,
  vertx: Vertx
): Router {
  val healthApi = Router.router(vertx)
  val dbHealthChecker = di.instance<DBHealthUseCase>()

  val livenessHandler = HealthCheckHandler.create(vertx)
  val readinessHandler = HealthCheckHandler.create(vertx)

  livenessHandler.register("status") { promise ->
    promise.complete(Status.OK())
  }

  readinessHandler.register("status") { promise ->
    promise.complete(Status.OK())
  }

  readinessHandler.register("postgres") { promise ->
    CoroutineScope(Dispatchers.IO).launch(vertx.dispatcher()) {
      if (dbHealthChecker.execute(Unit))
        promise.complete(Status.OK())
      else
        promise.complete(Status.KO())
    }
  }

  healthApi.get("/liveness").handler(livenessHandler)
  healthApi.get("/readiness").handler(readinessHandler)

  return healthApi
}
