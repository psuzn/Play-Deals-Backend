package me.sujanpoudel.playdeals.api.appDeals

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.api.ApiResponse
import me.sujanpoudel.playdeals.domain.NewAppDeal
import me.sujanpoudel.playdeals.domain.entities.AppDeal
import me.sujanpoudel.playdeals.get
import me.sujanpoudel.playdeals.repositories.AppDealRepository
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

private val newAppDeal = NewAppDeal(
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
  offerExpiresIn = OffsetDateTime.now()
)

class GetAppDealsApiTest(vertx: Vertx) : IntegrationTest(vertx) {
  @Test
  fun `should send error if skip param is less than 0`() = runTest {
    val response = httpClient.get("/api/deals/?skip=-1")
      .send()
      .await()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "skip Can't be less than 0"
  }

  @Test
  fun `should send error if take param is less than 1`() = runTest {
    val response = httpClient.get("/api/deals/?take=0")
      .send()
      .await()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "take Can't be less than 1"
  }

  @Test
  fun `should return app deals`() = runTest {
    val repository = di.get<AppDealRepository>()

    val app0 = repository.upsert(newAppDeal)
    val app1 = repository.upsert(newAppDeal.copy(id = "id1"))

    val response = httpClient.get("/api/deals/")
      .send()
      .await()

    val deals: ApiResponse<List<AppDeal>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

    response.statusCode() shouldBe 200
    deals.data!!.size shouldBe 2
    deals.data.shouldContainAll(listOf(app0, app1))
  }

  @Test
  fun `should correctly handle skip parameter`() = runTest {
    val repository = di.get<AppDealRepository>()

    repository.upsert(newAppDeal)
    repository.upsert(newAppDeal.copy(id = "id1"))
    repository.upsert(newAppDeal.copy(id = "id2"))
    repository.upsert(newAppDeal.copy(id = "id3"))

    httpClient.get("/api/deals?skip=1")
      .send()
      .await().also { response ->
        val deals: ApiResponse<List<AppDeal>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 3
      }

    httpClient.get("/api/deals?skip=3")
      .send()
      .await().also { response ->
        val deals: ApiResponse<List<AppDeal>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 1
      }
  }

  @Test
  fun `should correctly handle take parameter`() = runTest {
    val repository = di.get<AppDealRepository>()

    repository.upsert(newAppDeal)
    repository.upsert(newAppDeal.copy(id = "id1"))
    repository.upsert(newAppDeal.copy(id = "id2"))
    repository.upsert(newAppDeal.copy(id = "id3"))

    httpClient.get("/api/deals?take=2")
      .send()
      .await().also { response ->
        val deals: ApiResponse<List<AppDeal>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 2
      }

    httpClient.get("/api/deals?take=1")
      .send()
      .await().also { response ->
        val deals: ApiResponse<List<AppDeal>> = di.get<ObjectMapper>().readValue(response.bodyAsString())

        response.statusCode() shouldBe 200
        deals.data!!.size shouldBe 1
      }
  }
}
