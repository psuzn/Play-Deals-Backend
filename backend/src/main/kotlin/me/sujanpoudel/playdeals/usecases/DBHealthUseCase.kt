package me.sujanpoudel.playdeals.usecases

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient
import org.kodein.di.DI
import org.kodein.di.instance

class DBHealthUseCase(
  di: DI,
) : UseCase<Unit, Boolean> {
  private val sqlClient by di.instance<SqlClient>()

  override suspend fun doExecute(input: Unit): Boolean =
    runCatching {
      sqlClient.preparedQuery("""SELECT 1""")
        .execute()
        .coAwait()
    }.map { rs ->
      rs.count() == 1
    }.getOrDefault(false)
}
