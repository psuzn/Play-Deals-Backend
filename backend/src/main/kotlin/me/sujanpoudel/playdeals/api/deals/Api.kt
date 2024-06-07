package me.sujanpoudel.playdeals.api.deals

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.coAwait
import me.sujanpoudel.playdeals.ContentTypes
import me.sujanpoudel.playdeals.common.coHandler
import me.sujanpoudel.playdeals.common.jsonResponse
import me.sujanpoudel.playdeals.usecases.GetDealsUseCase
import me.sujanpoudel.playdeals.usecases.NewDealUseCase
import me.sujanpoudel.playdeals.usecases.executeUseCase
import org.kodein.di.DirectDI
import org.kodein.di.instance

fun appDealsApi(
  di: DirectDI,
  vertx: Vertx,
): Router =
  Router.router(vertx).apply {
    get()
      .coHandler { ctx ->
        ctx.executeUseCase(
          useCase = di.instance<GetDealsUseCase>(),
          toContext = { GetDealsContext(ctx.request().params()) },
          toInput = { GetDealsUseCase.Input(it.skip, it.take) },
        ) {
          ctx.json(jsonResponse(data = it))
        }
      }

    post()
      .consumes(ContentTypes.JSON)
      .coHandler { ctx ->
        ctx.executeUseCase(
          useCase = di.instance<NewDealUseCase>(),
          toContext = { NewDealContext(ctx.request().body().coAwait().toJsonObject()) },
          toInput = { it.packageName },
        ) {
          ctx.json(jsonResponse<Any>("App added for queue"))
        }
      }
  }
