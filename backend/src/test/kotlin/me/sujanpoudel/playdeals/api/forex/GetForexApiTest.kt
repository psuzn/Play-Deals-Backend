package me.sujanpoudel.playdeals.api.forex

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.domain.ConversionRate
import me.sujanpoudel.playdeals.domain.ForexRate
import me.sujanpoudel.playdeals.get
import me.sujanpoudel.playdeals.jobs.getForexRate
import me.sujanpoudel.playdeals.jobs.saveForexRate
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GetForexApiTest(vertx: Vertx) : IntegrationTest(vertx) {

  @Test
  fun `Key value repo should properly store the forex rate`() = runTest {
    val repository = di.get<KeyValuesRepository>()

    val forexRate = ForexRate(
      timestamp = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC),
      rates = listOf(ConversionRate("USD", "$", "US Dollar", 1.1f))
    )
    repository.saveForexRate(forexRate)

    val savedForexRate = repository.getForexRate()

    savedForexRate shouldBe forexRate
  }

  @Test
  fun `should return forex if there is data`() = runTest {
    val repository = di.get<KeyValuesRepository>()

    val forexRate = ForexRate(
      timestamp = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC),
      rates = listOf(ConversionRate("USD", "$", "US Dollar", 1.1f))
    )

    repository.saveForexRate(forexRate)

    val response = httpClient.get("/api/forex")
      .send()
      .await()
      .bodyAsJsonObject()

    response.getJsonObject("data").also { data ->
      OffsetDateTime.parse(data.getString("timestamp")) shouldBe forexRate.timestamp
      data.getJsonArray("rates").also { rates ->
        rates.size() shouldBe 1
        (rates.first() as JsonObject).also { rate ->
          rate.getString("currency") shouldBe "USD"
          rate.getString("symbol") shouldBe "$"
          rate.getString("name") shouldBe "US Dollar"
          rate.getFloat("rate") shouldBe 1.1f
        }
      }
    }
  }

  @Test
  fun `should return null if there is no data`() = runTest {
    val response = httpClient.get("/api/forex")
      .send()
      .await()
      .bodyAsJsonObject()

    response.getJsonObject("data") shouldBe null
  }
}
