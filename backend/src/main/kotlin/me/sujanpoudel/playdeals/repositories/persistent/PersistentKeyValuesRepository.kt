package me.sujanpoudel.playdeals.repositories.persistent

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.entities.value
import me.sujanpoudel.playdeals.domain.entities.valueOrNull
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository

class PersistentKeyValuesRepository(
  private val sqlClient: SqlClient,
) : KeyValuesRepository {
  override suspend fun set(key: String, value: String): String {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "key_value_store" VALUES ($1,$2)
          ON CONFLICT(key) DO UPDATE SET value = $2
      RETURNING *
      """.trimIndent(),
    ).exec(key, value)
      .coAwait()
      .first()
      .value()
  }

  override suspend fun get(key: String): String? {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "key_value_store" WHERE key = $1
      """.trimIndent(),
    ).exec(key)
      .coAwait()
      .firstOrNull()
      .valueOrNull()
  }

  override suspend fun delete(key: String) {
    sqlClient.preparedQuery(
      """
      DELETE FROM "key_value_store" WHERE key = $1
      """.trimIndent(),
    ).exec(key)
      .coAwait()
  }
}
