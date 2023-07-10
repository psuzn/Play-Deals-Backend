package me.sujanpoudel.playdeals.repositories

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.entities.KeyValue
import me.sujanpoudel.playdeals.domain.entities.asKeyValue
import java.io.Serializable

class KeyValuesRepository(
  val sqlClient: SqlClient
) {

  suspend inline fun <reified T : Serializable> set(key: String, value: T): KeyValue<T> {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "key_value_store" VALUES ($1,$2)
          ON CONFLICT(key) DO UPDATE SET value = $2
      RETURNING *
    """.trimIndent()
    ).exec(key, value)
      .await()
      .first()
      .asKeyValue<T>()
  }

  suspend inline fun <reified T : Serializable> get(key: String): T? {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "key_value_store" WHERE key = $1
    """.trimIndent()
    ).exec(key)
      .await()
      .firstOrNull()?.asKeyValue<T>()?.value
  }

  suspend inline fun <reified T : Serializable> delete(key: String) {
    sqlClient.preparedQuery(
      """
     DELETE FROM "key_value_store" WHERE key = $1
    """.trimIndent()
    ).exec(key)
      .await()

  }

}
