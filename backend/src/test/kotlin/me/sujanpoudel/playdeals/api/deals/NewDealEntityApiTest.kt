package me.sujanpoudel.playdeals.api.deals

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.get
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider
import org.junit.jupiter.api.Test
import java.util.UUID

class NewDealEntityApiTest(vertx: Vertx) : IntegrationTest(vertx) {

  @Test
  fun `should send error response if packageName is null`() = runTest {
    val response = httpClient.post("/api/deals")
      .sendJson(jsonObjectOf())
      .await()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "packageName is required"
  }

  @Test
  fun `should send error response if packageName is invalid`() = runTest {
    val response = httpClient.post("/api/deals")
      .sendJson(jsonObjectOf("packageName" to "11111"))
      .await()

    val responseBody = response.bodyAsJsonObject()

    response.statusCode() shouldBe 400
    responseBody.getString("message") shouldBe "Invalid value for packageName"
  }

  @Test
  fun `should enqueue a app detail scrap request on success`() = runTest {
    val storageProvider = di.get<StorageProvider>()

    val packageName = "com.example.app"

    val response = httpClient.post("/api/deals")
      .sendJson(jsonObjectOf("packageName" to packageName))
      .await()

    val job = storageProvider.getJobById(UUID.nameUUIDFromBytes(packageName.encodeToByteArray()))

    job.state shouldBe StateName.ENQUEUED

    response.statusCode() shouldBe 200
  }
}
