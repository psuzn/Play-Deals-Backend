package me.sujanpoudel.playdeals.api.health

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.UseCase

class DBHealthUseCase(
  private val sqlClient: SqlClient
) : UseCase<Unit, Boolean> {
  override suspend fun doExecute(input: Unit): Boolean = runCatching {
    sqlClient.preparedQuery("""SELECT 1""")
      .execute()
      .await()
  }.map { rs ->
    rs.count() == 1
  }.getOrDefault(false)
}
