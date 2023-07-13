package me.sujanpoudel.playdeals.repositories

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.NewAppDeal
import me.sujanpoudel.playdeals.domain.entities.asAppDeal
import me.sujanpoudel.playdeals.get
import org.junit.jupiter.api.Test
import java.time.Instant

class ApiDealRepositoryTest(vertx: Vertx) : IntegrationTest(vertx) {

  private val repository by lazy { di.get<AppDealRepository>() }
  private val sqlClient by lazy { di.get<SqlClient>() }

  private val newAppDeal = NewAppDeal(
    id = "id",
    name = "name",
    icon = "icon",
    images = listOf("img0", "img1"),
    normalPrice = 12f,
    currentPrice = 12f,
    currency = "USD",
    storeUrl = "store_url",
    category = "unknown",
    downloads = "12+",
    rating = "12",
    offerExpiresIn = Instant.now()
  )

  @Test
  fun `should create new app deal in db`() = runTest {
    val appDeal = repository.upsert(newAppDeal)

    val appDealFromDb = sqlClient.preparedQuery(""" SELECT * from "app_deal" where id=$1""")
      .exec(newAppDeal.id)
      .await()
      .first()
      .asAppDeal()

    appDeal.shouldBeEqualToComparingFields(appDealFromDb)
  }

  @Test
  fun `should perform update if item with id already exists`() = runTest {
    repository.upsert(newAppDeal)

    repository.upsert(newAppDeal.copy(name = "Updated Name"))

    val appDealFromDb = sqlClient.preparedQuery(""" SELECT * from "app_deal" where id=$1""")
      .exec(newAppDeal.id)
      .await()
      .first()
      .asAppDeal()

    appDealFromDb.name.shouldBe("Updated Name")
  }

  @Test
  fun `should delete app deal in db`() = runTest {
    repository.upsert(newAppDeal)
    repository.delete(newAppDeal.id)

    sqlClient.preparedQuery("""SELECT * from "app_deal" where id=$1""")
      .exec(newAppDeal.id)
      .await()
      .rowCount() shouldBe 0
  }

  @Test
  fun `should be able to get all app deals from db`() = runTest {
    val deal0 = repository.upsert(newAppDeal)
    val deal1 = repository.upsert(newAppDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainAll listOf(deal0, deal1)
  }

  @Test
  fun `should be able to get all app deals from db in order`() = runTest {
    val deal0 = repository.upsert(newAppDeal)
    val deal1 = repository.upsert(newAppDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainInOrder listOf(deal1, deal0)
  }
}
