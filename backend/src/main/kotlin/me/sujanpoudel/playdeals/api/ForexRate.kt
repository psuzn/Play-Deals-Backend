package me.sujanpoudel.playdeals.api

import io.vertx.ext.web.Router
import me.sujanpoudel.playdeals.common.coHandler
import me.sujanpoudel.playdeals.common.jsonResponse
import me.sujanpoudel.playdeals.usecases.GetForexUseCase
import me.sujanpoudel.playdeals.usecases.executeUseCase
import org.kodein.di.DirectDI
import org.kodein.di.instance

fun forexRateApi(di: DirectDI, vertx: io.vertx.core.Vertx): Router = Router.router(vertx).apply {
  get()
    .coHandler { ctx ->
      ctx.executeUseCase(
        useCase = di.instance<GetForexUseCase>(),
        toContext = { },
        toInput = { },
      ) {
        ctx.json(jsonResponse(data = it))
      }
    }
}
