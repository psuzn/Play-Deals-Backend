package me.sujanpoudel.playdeals.api.health

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.get
import org.junit.jupiter.api.Test

class DBCleanupTest(vertx: Vertx) : IntegrationTest(vertx) {

  @Test
  fun `Does cleanup`() = runTest {
    val sqlClient = di.get<SqlClient>()

    sqlClient
      .query(CLEAN_UP_DB_QUERY).execute()
      .onFailure { it.printStackTrace() }

    val totalUsers = sqlClient.preparedQuery("""select count(*) from app_deal """)
      .execute()
      .await().first().getInteger(0)

    totalUsers shouldBe 0
  }
}
