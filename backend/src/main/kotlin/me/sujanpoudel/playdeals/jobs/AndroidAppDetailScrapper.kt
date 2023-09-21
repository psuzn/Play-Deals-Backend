package me.sujanpoudel.playdeals.jobs

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.domain.AndroidAppDetail
import me.sujanpoudel.playdeals.domain.asNewDeal
import me.sujanpoudel.playdeals.log
import me.sujanpoudel.playdeals.repositories.DealRepository
import org.jobrunr.jobs.lambdas.JobRequest
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.regex.Pattern

enum class Value(val root: String, vararg val path: Int) {
  TITLE("ds:5", 1, 2, 0, 0),
  ICON("ds:5", 1, 2, 95, 0, 3, 2),
  INSTALLS("ds:5", 1, 2, 13, 0),
  RATINGS("ds:5", 1, 2, 51, 0, 1),
  NORMAL_PRICE("ds:5", 1, 2, 57, 0, 0, 0, 0, 1, 1, 0),
  CURRENT_PRICE("ds:5", 1, 2, 57, 0, 0, 0, 0, 1, 0, 0),
  OFFER_END_TIME("ds:5", 1, 2, 57, 0, 0, 0, 0, 14, 0, 0),
  CURRENCY("ds:5", 1, 2, 57, 0, 0, 0, 0, 1, 0, 1),
  GENRE("ds:5", 1, 2, 79, 0, 0, 0),
  SCREENSHOTS_LIST("ds:5", 1, 2, 78, 0),
  SCREENSHOTS_URL("", 3, 2);
}

class AppDetailScrapper(
  override val di: DI
) : CoJobRequestHandler<AppDetailScrapper.Request>(), DIAware {

  private val jobsVerticle by instance<BackgroundJobsVerticle>()
  private val repository by instance<DealRepository>()
  private val webClient by lazy {
    WebClient.create(jobsVerticle.vertx, WebClientOptions().setDefaultHost("play.google.com"))
  }

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest ${jobRequest.packageName}"
  ) {
    val packageName = jobRequest.packageName

    val app = loggingExecutionTime("$SIMPLE_NAME:: scrapping app details $packageName") {
      getAppDetail(packageName)
    }

    when {
      app.normalPrice == 0f -> {
        log.info("App $packageName(${app.name}) doesn't have any price")
        repository.delete(packageName)
      }

      app.normalPrice == app.currentPrice -> {
        log.info("App $packageName(${app.name}) deals has been expired")
        repository.delete(packageName)
      }

      (app.currentPrice ?: 0f) < app.normalPrice -> {
        log.info("Found deal for $packageName(${app.name}) ${app.currentPrice} ${app.currency}(${app.normalPrice} ${app.currency})")
        repository.upsert(app.asNewDeal())
      }
    }
  }

  private suspend fun getAppDetail(packageName: String): AndroidAppDetail {
    val response = webClient.get("/store/apps/details?id=$packageName&hl=en&gl=us")
      .send()
      .await()

    val body = response.bodyAsString()

    val mapper = ObjectMapper().apply {
      configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }

    val matches = INIT_DATA_PATTERN.matcher(body).let {
      val snippets = mutableListOf<String>()
      while (it.find()) {
        snippets.add(it.group(1))
      }
      snippets
    }.map {
      io.vertx.core.json.Json.decodeValue(mapper.readTree(it).toPrettyString()) as JsonObject
    }

    val combined = jsonObjectOf()

    matches.forEach {
      combined.put(it.getString("key"), it.getValue("data"))
    }

    val currentPrice = combined.getValue<Int>(Value.CURRENT_PRICE) / PRICE_MULTIPLIER
    val normalPrice = combined.getValueOrNull<Int>(Value.NORMAL_PRICE)?.div(PRICE_MULTIPLIER) ?: currentPrice

    return AndroidAppDetail(
      id = packageName,
      name = combined.getValue(Value.TITLE),
      icon = combined.getValue(Value.ICON),
      images = (combined.getValue(Value.SCREENSHOTS_LIST) as JsonArray).mapNotNull {
        getValue(it as JsonArray, Value.SCREENSHOTS_URL.path.toTypedArray()) as? String
      },
      normalPrice = normalPrice,
      currency = combined.getValue(Value.CURRENCY) as String,
      currentPrice = currentPrice,
      rating = combined.getValue<Float>(Value.RATINGS).toString(),
      downloads = combined.getValue(Value.INSTALLS),
      storeUrl = "https://play.google.com/store/apps/details?id=$packageName",
      category = combined.getValue(Value.GENRE) as String,
      offerExpiresIn = combined.getValueOrNull<Int>(Value.OFFER_END_TIME)?.let {
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(it.toLong()), ZoneOffset.UTC)
      },
      source = Constants.DealSources.APP_DEAL_SUBREDDIT
    )
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> JsonObject.getValueOrNull(value: Value): T? {
    return try {
      return getValue(getJsonArray(value.root), value.path.toTypedArray()) as? T
    } catch (e: Exception) {
      null
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun <T> JsonObject.getValue(value: Value): T {
    return getValue(getJsonArray(value.root), value.path.toTypedArray()) as T
  }

  fun getValue(jsonObject: JsonArray, path: Array<Int>): Any {
    return if (path.size == 1) {
      jsonObject.getValue(path.first())
    } else {
      getValue(jsonObject.getValue(path.first()) as JsonArray, path.drop(1).toTypedArray())
    }
  }

  companion object {
    const val PRICE_MULTIPLIER = 1000_000f
    val INIT_DATA_PATTERN: Pattern =
      Pattern.compile("<script [A-Za-z-0-9=_\"]+>AF_initDataCallback\\((\\{.*?\\})\\);")
  }

  data class Request(val packageName: String) : JobRequest {
    override fun getJobRequestHandler() = AppDetailScrapper::class.java
  }
}
