package me.sujanpoudel.playdeals.api.deals

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.coAwait
import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealType
import me.sujanpoudel.playdeals.get
import me.sujanpoudel.playdeals.repositories.DealRepository
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class NewDealApiTest(vertx: Vertx) : IntegrationTest(vertx) {
  @Test
  fun `should send error response if packageName is null`() = runTest {
    val response =
      httpClient.post("/api/deals")
        .sendJson(jsonObjectOf())
        .coAwait()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "packageName is required"
  }

  @Test
  fun `should send error response if packageName is invalid`() = runTest {
    val response =
      httpClient.post("/api/deals")
        .sendJson(jsonObjectOf("packageName" to "11111"))
        .coAwait()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "Invalid value for packageName"
  }

  @Test
  fun `should enqueue a app detail scrap request on success`() = runTest {
    val storageProvider = di.get<StorageProvider>()

    val packageName = "com.example.app"

    val response =
      httpClient.post("/api/deals")
        .sendJson(jsonObjectOf("packageName" to packageName))
        .coAwait()

    val job = storageProvider.getJobById(UUID.nameUUIDFromBytes(packageName.encodeToByteArray()))

    job.state shouldBe StateName.ENQUEUED

    response.statusCode() shouldBe 200
  }

  @Test
  fun `should should 200 if the app already exists`() = runTest {
    di.get<StorageProvider>()
    val repository = di.get<DealRepository>()

    val packageName = "com.example.app"

    val newDeal =
      NewDeal(
        id = packageName,
        name = "name",
        icon = "icon",
        images = listOf("img0", "img1"),
        normalPrice = 12f,
        currentPrice = 12f,
        currency = "USD",
        storeUrl = "store_url",
        category = "unknown",
        downloads = "12+",
        rating = "12",
        offerExpiresIn = OffsetDateTime.now(),
        type = DealType.ANDROID_APP,
        source = Constants.DealSources.APP_DEAL_SUBREDDIT,
      )

    repository.upsert(newDeal)

    val response =
      httpClient.post("/api/deals")
        .sendJson(jsonObjectOf("packageName" to packageName))
        .coAwait()

    response.statusCode() shouldBe 200
  }
}
