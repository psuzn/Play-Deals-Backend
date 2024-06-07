package me.sujanpoudel.playdeals.api.health

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.get
import org.junit.jupiter.api.Test

class DBCleanupTest(vertx: Vertx) : IntegrationTest(vertx) {
  @Test
  fun `Does cleanup`() =
    runTest {
      val sqlClient = di.get<SqlClient>()

      sqlClient
        .query(CLEAN_UP_DB_QUERY).execute()
        .onFailure { it.printStackTrace() }

      val totalDeals =
        sqlClient.preparedQuery("""select count(*) from deal """)
          .execute()
          .coAwait().first().getInteger(0)

      totalDeals shouldBe 0
    }
}
