package me.sujanpoudel.playdeals.api.deals

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.api.ApiResponse
import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.domain.entities.DealType
import me.sujanpoudel.playdeals.get
import me.sujanpoudel.playdeals.repositories.DealRepository
import me.sujanpoudel.playdeals.repositories.persistent.PersistentDealRepository
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

private val newDeal =
  NewDeal(
    id = "id",
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

class GetDealsApiTest(vertx: Vertx) : IntegrationTest(vertx) {
  @Test
  fun `should send error if skip param is less than 0`() = runTest {
    val response =
      httpClient.get("/api/deals/?skip=-1")
        .send()
        .coAwait()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "skip Can't be less than 0"
  }

  @Test
  fun `should send error if take param is less than 1`() = runTest {
    val response =
      httpClient.get("/api/deals/?take=0")
        .send()
        .coAwait()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "take Can't be less than 1"
  }

  @Test
  fun `should return app deals`() = runTest {
    val repository = di.get<DealRepository>()

    val app0 = repository.upsert(newDeal)
    val app1 = repository.upsert(newDeal.copy(id = "id1"))

    val response =
      httpClient.get("/api/deals/")
        .send()
        .coAwait()

    val deals: ApiResponse<List<DealEntity>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

    response.statusCode() shouldBe 200
    deals.data!!.size shouldBe 2
    deals.data.shouldContainAll(listOf(app0, app1))
  }

  @Test
  fun `should correctly handle skip parameter`() = runTest {
    val repository = di.get<DealRepository>()

    repository.upsert(newDeal)
    repository.upsert(newDeal.copy(id = "id1"))
    repository.upsert(newDeal.copy(id = "id2"))
    repository.upsert(newDeal.copy(id = "id3"))

    httpClient.get("/api/deals?skip=1")
      .send()
      .coAwait().also { response ->
        val deals: ApiResponse<List<DealEntity>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 3
      }

    httpClient.get("/api/deals?skip=3")
      .send()
      .coAwait().also { response ->
        val deals: ApiResponse<List<DealEntity>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 1
      }
  }

  @Test
  fun `should correctly handle take parameter`() = runTest {
    val repository = di.get<PersistentDealRepository>()

    repository.upsert(newDeal)
    repository.upsert(newDeal.copy(id = "id1"))
    repository.upsert(newDeal.copy(id = "id2"))
    repository.upsert(newDeal.copy(id = "id3"))

    httpClient.get("/api/deals?take=2")
      .send()
      .coAwait().also { response ->
        val deals: ApiResponse<List<DealEntity>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 2
      }

    httpClient.get("/api/deals?take=1")
      .send()
      .coAwait().also { response ->
        val deals: ApiResponse<List<DealEntity>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 1
      }
  }
}
