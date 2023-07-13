package me.sujanpoudel.playdeals.api.appDeals

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.ContentTypes
import me.sujanpoudel.playdeals.common.coHandler
import me.sujanpoudel.playdeals.common.jsonResponse
import me.sujanpoudel.playdeals.usecases.GetAppDealsUseCase
import me.sujanpoudel.playdeals.usecases.NewAppDealUseCase
import me.sujanpoudel.playdeals.usecases.executeUseCase
import org.kodein.di.DirectDI
import org.kodein.di.instance

fun appDealsApi(
  di: DirectDI,
  vertx: Vertx
): Router = Router.router(vertx).apply {

  get()
    .coHandler { ctx ->
      ctx.executeUseCase(
        useCase = di.instance<GetAppDealsUseCase>(),
        toContext = { GetAppDealsContext(ctx.request().params()) },
        toInput = { GetAppDealsUseCase.Input(it.skip, it.take) }
      ) {
        ctx.json(jsonResponse(data = it))
      }
    }

  post()
    .consumes(ContentTypes.JSON)
    .coHandler { ctx ->
      ctx.executeUseCase(
        useCase = di.instance<NewAppDealUseCase>(),
        toContext = { NewAppDealContext(ctx.request().body().await().toJsonObject()) },
        toInput = { it.packageName },
      ) {
        ctx.json(jsonResponse<Any>("App added for queue"))
      }
    }
}
