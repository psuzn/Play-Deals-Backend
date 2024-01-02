package me.sujanpoudel.playdeals.jobs

import io.vertx.core.json.Json
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.Conf
import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.domain.ConversionRate
import me.sujanpoudel.playdeals.domain.ForexRate
import me.sujanpoudel.playdeals.logger
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.scheduling.RecurringJobBuilder
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ForexFetcher(
  override val di: DI,
  private val conf: Conf
) : CoJobRequestHandler<ForexFetcher.Request>(), DIAware {

  private val backgroundJobsVerticle by instance<BackgroundJobsVerticle>()

  private val webClient by lazy {
    WebClient.create(
      backgroundJobsVerticle.vertx,
      WebClientOptions().setSsl(false).setDefaultHost("api.exchangeratesapi.io")
    )
  }

  private val repository by instance<KeyValuesRepository>()

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest"
  ) {
    val rates = getForexRates()
    logger.info("got ${rates.rates.size} forex rate")
    repository.saveForexRate(rates)
  }

  private suspend fun getForexRates(): ForexRate {
    val response = webClient.get("/v1/latest?access_key=${conf.forexApiKey}&format=1&base=EUR")
      .send()
      .await()
      .bodyAsString()
      .let {
        Json.decodeValue(it) as io.vertx.core.json.JsonObject
      }

    val epochSeconds = response.getLong("timestamp")
    val usdRate = response.getJsonObject("rates").getNumber("USD").toFloat()

    return ForexRate(
      timestamp = OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC),
      rates = response.getJsonObject("rates").map {
        ConversionRate(it.key, (it.value as Number).toFloat() / usdRate)
      }
    )
  }

  class Request private constructor() : JobRequest {
    override fun getJobRequestHandler() = ForexFetcher::class.java

    companion object {
      private val JOB_ID: UUID = UUID.nameUUIDFromBytes("ForexFetch".toByteArray())
      operator fun invoke(): RecurringJobBuilder = RecurringJobBuilder.aRecurringJob()
        .withJobRequest(Request())
        .withName("ForexFetch")
        .withId(JOB_ID.toString())
        .withDuration(Duration.ofDays(1))
        .withAmountOfRetries(3)

      fun immediate(): JobRequest = Request()
    }
  }
}

private const val KEY_FOREX_RATE = "FOREX_RATE"

suspend fun KeyValuesRepository.getForexRate(): ForexRate? = get(KEY_FOREX_RATE)?.let {
  Json.decodeValue(it, ForexRate::class.java)
}

suspend fun KeyValuesRepository.saveForexRate(forexRate: ForexRate) = set(KEY_FOREX_RATE, Json.encode(forexRate))
