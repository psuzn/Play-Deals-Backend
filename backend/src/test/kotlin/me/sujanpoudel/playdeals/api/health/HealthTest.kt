package me.sujanpoudel.playdeals.api.health

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import me.sujanpoudel.playdeals.IntegrationTest
import org.junit.jupiter.api.Test

class HealthTest(vertx: Vertx) : IntegrationTest(vertx) {
  @Test
  fun `GET liveness should return 200`() =
    runTest {
      val response = httpClient.get(8888, "localhost", "/health/liveness").send().coAwait()

      response.statusCode() shouldBe 200
      val responseJson = response.bodyAsJsonObject()
      responseJson.getString("status") shouldBe "UP"
    }

  @Test
  fun `GET readiness should return 200`() =
    runTest {
      val response = httpClient.get(8888, "localhost", "/health/readiness").send().coAwait()

      val responseJson = response.bodyAsJsonObject()
      response.statusCode() shouldBe 200
      responseJson.getString("status") shouldBe "UP"
      responseJson.getString("outcome") shouldBe "UP"
    }
}
