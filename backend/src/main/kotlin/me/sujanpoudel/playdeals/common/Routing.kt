package me.sujanpoudel.playdeals.common

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sujanpoudel.playdeals.ContentTypes
import me.sujanpoudel.playdeals.exceptions.ClientErrorException
import me.sujanpoudel.playdeals.logger

fun Route.coHandler(fn: suspend (RoutingContext) -> Unit): Route {
  return handler { ctx ->
    CoroutineScope(Dispatchers.IO).launch(ctx.vertx().dispatcher()) {
      try {
        fn(ctx)
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
}

fun HttpServerResponse.contentType(value: String): HttpServerResponse = putHeader("Content-Type", value)

fun <T> jsonResponse(
  message: String = "Success",
  data: T? = null,
): JsonObject =
  jsonObjectOf(
    "message" to message,
    "data" to data,
  )

const val UNKNOWN_ERROR_MESSAGE = "Something went wrong"

fun RoutingContext.handleExceptions(exception: Throwable) {
  val (message, statusCode) =
    when (exception) {
      is ClientErrorException -> exception.message to exception.statusCode
      else -> UNKNOWN_ERROR_MESSAGE to 500
    }

  this.response()
    .setStatusCode(statusCode)
    .contentType(ContentTypes.JSON)
    .end(jsonResponse<Any>(message ?: UNKNOWN_ERROR_MESSAGE).encode())

  if (statusCode in 500..599) {
    logger.error(exception) {
      "Error while handing request at: ${request().path()}"
    }
  }
}
