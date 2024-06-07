package me.sujanpoudel.playdeals.jobs

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.vertx.core.json.Json
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.coAwait
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
import java.util.Locale
import java.util.UUID

data class Currency(
  val name: String,
  val symbol: String,
)

fun loadCurrencies(): HashMap<String, Currency> {
  return Gson().fromJson(
    Thread.currentThread().contextClassLoader.getResource("currencies.json")?.readText() ?: "{}",
    object : TypeToken<HashMap<String, Currency>>() {},
  )
}

class ForexFetcher(
  override val di: DI,
  private val conf: Conf,
) : CoJobRequestHandler<ForexFetcher.Request>(), DIAware {
  private val backgroundJobsVerticle by instance<BackgroundJobsVerticle>()

  private val webClient by lazy {
    WebClient.create(
      backgroundJobsVerticle.vertx,
      WebClientOptions().setSsl(false).setDefaultHost("api.exchangeratesapi.io"),
    )
  }

  private val repository by instance<KeyValuesRepository>()

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest",
  ) {
    val rates = getForexRates()
    logger.info("got ${rates.rates.size} forex rate")
    repository.saveForexRate(rates)
  }

  private suspend fun getForexRates(): ForexRate {
    val currencies = loadCurrencies()

    val response = webClient.get("/v1/latest?access_key=${conf.forexApiKey}&format=1&base=EUR")
      .send()
      .coAwait()
      .bodyAsString()
      .let {
        Json.decodeValue(it) as io.vertx.core.json.JsonObject
      }

    val epochSeconds = response.getLong("timestamp")
    val usdRate = response.getJsonObject("rates").getNumber("USD").toFloat()

    return ForexRate(
      timestamp = OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC),
      rates = response.getJsonObject("rates").map {
        val currency = currencies[it.key]
        ConversionRate(
          currency = it.key,
          symbol = currency?.symbol ?: "$",
          name = currency?.name ?: it.key,
          rate = (it.value as Number).toFloat() / usdRate,
        )
      },
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

private val Locale.flagEmoji: String
  get() {
    val firstLetter = Character.codePointAt(country, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(country, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
  }
