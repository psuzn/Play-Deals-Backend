package me.sujanpoudel.playdeals.repositories.persistent

import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.entities.KeyValueEntity
import me.sujanpoudel.playdeals.domain.entities.asKeyValue
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import kotlin.reflect.KClass

private fun Any.serializeToString(): String = Json.CODEC.toString(this)

class PersistentKeyValuesRepository(
  private val sqlClient: SqlClient
) : KeyValuesRepository {

  override suspend fun <T : Any> set(key: String, value: T, clazz: KClass<out T>): KeyValueEntity<T> {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "key_value_store" VALUES ($1,$2)
          ON CONFLICT(key) DO UPDATE SET value = $2
      RETURNING *
      """.trimIndent()
    ).exec(key, value.serializeToString())
      .await()
      .first()
      .asKeyValue<T>(clazz)
  }

  override suspend fun <T : Any> get(key: String, clazz: KClass<T>): T? {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "key_value_store" WHERE key = $1
      """.trimIndent()
    ).exec(key)
      .await()
      .firstOrNull()?.asKeyValue(clazz)?.value
  }

  override suspend fun delete(key: String) {
    sqlClient.preparedQuery(
      """
     DELETE FROM "key_value_store" WHERE key = $1
      """.trimIndent()
    ).exec(key)
      .await()
  }
}
