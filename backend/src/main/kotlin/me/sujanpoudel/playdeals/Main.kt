package me.sujanpoudel.playdeals

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.getOrThrow
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.runBlocking
import me.sujanpoudel.playdeals.common.BootstrapException
import me.sujanpoudel.playdeals.common.buildConf
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.system.exitProcess

private val vertx = Vertx.vertx()
val configuration = buildConf(System.getenv()).getOrThrow {
  (it as BootstrapException).violations.forEach(::println)
  exitProcess(-1)
}

val primaryDI = configureDI(vertx, configuration)

fun main(): Unit = runBlocking {
  primaryDI.direct.instance<ObjectMapper>()

  vertx.deployVerticle(primaryDI.direct.instance<MainVerticle>())
    .onSuccess { logger.infoNotify("Deployed MainVerticle : $it") }
    .onFailure {
      logger.error(it) { "Error deploying main verticle" }
      vertx.close()
    }.coAwait()
}
