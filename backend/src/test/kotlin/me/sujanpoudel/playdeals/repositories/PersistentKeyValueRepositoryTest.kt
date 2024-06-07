package me.sujanpoudel.playdeals.repositories

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.entities.value
import me.sujanpoudel.playdeals.get
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class PersistentKeyValueRepositoryTest(vertx: Vertx) : IntegrationTest(vertx) {
  private val repository by lazy { di.get<KeyValuesRepository>() }
  private val sqlClient by lazy { di.get<SqlClient>() }

  @Test
  fun `should create new entry on db`() = runTest {
    val value = repository.set(KEY, "test")

    val valueFromDb =
      sqlClient.preparedQuery(""" SELECT * from "key_value_store" where key=$1""")
        .exec(KEY)
        .coAwait()
        .first()
        .getString("value")

    value shouldBe valueFromDb
  }

  @Test
  fun `should perform update if item with id already exists`() = runTest {
    repository.set(KEY, "test")

    val updated = repository.set(KEY, "test1")

    val fromDb =
      sqlClient.preparedQuery(""" SELECT * from "key_value_store" where key=$1""")
        .exec(KEY)
        .coAwait()
        .first()
        .value()

    fromDb shouldBe updated
  }

  @Test
  fun `should be able to serialize unknown types`() = runTest {
    val value = OffsetDateTime.now()

    repository.set(KEY, value.toString())

    val fromDb = repository.get(KEY).let(OffsetDateTime::parse)

    fromDb shouldBe value
  }

  companion object {
    const val KEY = "test_key"
  }
}
