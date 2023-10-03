package me.sujanpoudel.playdeals

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.vertx.core.Vertx
import me.sujanpoudel.playdeals.common.BootstrapException
import me.sujanpoudel.playdeals.common.buildConf
import mu.KLogger
import mu.KotlinLogging
import org.kodein.di.direct
import org.kodein.di.instance

val log: KLogger = KotlinLogging.logger {}

fun main() {
  val vertx = Vertx.vertx()
  val conf = when (val result = buildConf(System.getenv())) {
    is Ok -> result.value
    is Err -> {
      (result.error as BootstrapException).violations.forEach(::println)
      return
    }
  }

  val di = DIConfigurer.configure(vertx, conf)

  vertx.deployVerticle(di.direct.instance<MainVerticle>())
    .onSuccess { log.info("Deployed MainVerticle : $it") }
    .onFailure {
      log.error(it) { "Error deploying main verticle" }
      vertx.close()
    }

  di.direct.instance<ObjectMapper>()
}
