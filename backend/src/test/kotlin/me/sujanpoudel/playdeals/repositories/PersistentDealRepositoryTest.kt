package me.sujanpoudel.playdeals.repositories

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealType
import me.sujanpoudel.playdeals.domain.entities.asAppDeal
import me.sujanpoudel.playdeals.get
import me.sujanpoudel.playdeals.repositories.persistent.PersistentDealRepository
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class PersistentDealRepositoryTest(vertx: Vertx) : IntegrationTest(vertx) {

  private val repository by lazy { di.get<PersistentDealRepository>() }
  private val sqlClient by lazy { di.get<SqlClient>() }

  private val newDeal = NewDeal(
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
    offerExpiresIn = OffsetDateTime.now(),
    type = DealType.ANDROID_APP,
    source = Constants.DealSources.APP_DEAL_SUBREDDIT
  )

  @Test
  fun `should create new app deal in db`() = runTest {
    val appDeal = repository.upsert(newDeal)

    val appDealFromDb = sqlClient.preparedQuery(""" SELECT * from "deal" where id=$1""")
      .exec(newDeal.id)
      .await()
      .first()
      .asAppDeal()

    appDeal.shouldBeEqualToComparingFields(appDealFromDb)
  }

  @Test
  fun `should perform update if item with id already exists`() = runTest {
    repository.upsert(newDeal)

    repository.upsert(newDeal.copy(name = "Updated Name"))

    val appDealFromDb = sqlClient.preparedQuery(""" SELECT * from "deal" where id=$1""")
      .exec(newDeal.id)
      .await()
      .first()
      .asAppDeal()

    appDealFromDb.name.shouldBe("Updated Name")
  }

  @Test
  fun `should delete app deal in db`() = runTest {
    repository.upsert(newDeal)
    repository.delete(newDeal.id)

    sqlClient.preparedQuery("""SELECT * from "deal" where id=$1""")
      .exec(newDeal.id)
      .await()
      .rowCount() shouldBe 0
  }

  @Test
  fun `should be able to get all app deals from db`() = runTest {
    val deal0 = repository.upsert(newDeal)
    val deal1 = repository.upsert(newDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainAll listOf(deal0, deal1)
  }

  @Test
  fun `should be able to get all app deals from db in order`() = runTest {
    val deal0 = repository.upsert(newDeal)
    val deal1 = repository.upsert(newDeal.copy(id = "id_1"))

    val appDeal = repository.getAll(0, 100)

    appDeal shouldContainInOrder listOf(deal1, deal0)
  }
}