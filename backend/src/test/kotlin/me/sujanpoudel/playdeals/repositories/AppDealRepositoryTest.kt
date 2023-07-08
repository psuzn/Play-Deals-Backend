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

class AppDealRepositoryTest(vertx: Vertx) : IntegrationTest(vertx) {

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
    category = "unknown"
  )

  @Test
  fun `should create new app deal in db`() = runTest {

    val appDeal = repository.createNew(newAppDeal)

    val appDealFromDb = sqlClient.preparedQuery(""" SELECT * from "app_deal" where id=$1""")
      .exec(newAppDeal.id)
      .await()
      .first()
      .asAppDeal()

    appDeal.shouldBeEqualToComparingFields(appDealFromDb)
  }

  @Test
  fun `should delete app deal in db`() = runTest {

    repository.createNew(newAppDeal)
    repository.delete(newAppDeal.id)

    sqlClient.preparedQuery("""SELECT * from "app_deal" where id=$1""")
      .exec(newAppDeal.id)
      .await()
      .rowCount() shouldBe 0
  }

  @Test
  fun `should be able to get all app deals from db`() = runTest {

    val deal0 = repository.createNew(newAppDeal)
    val deal1 = repository.createNew(newAppDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainAll listOf(deal0, deal1)
  }

  @Test
  fun `should be able to get all app deals from db in order`() = runTest {

    val deal0 = repository.createNew(newAppDeal)
    val deal1 = repository.createNew(newAppDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainInOrder listOf(deal1, deal0)
  }
}
