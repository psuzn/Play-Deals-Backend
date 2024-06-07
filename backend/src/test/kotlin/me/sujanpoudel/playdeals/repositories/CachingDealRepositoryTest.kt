package me.sujanpoudel.playdeals.repositories

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.vertx.core.Vertx
import me.sujanpoudel.playdeals.IntegrationTest
import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.repositories.caching.CachingDealRepository
import me.sujanpoudel.playdeals.repositories.persistent.PersistentDealRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CachingDealRepositoryTest(vertx: Vertx) : IntegrationTest(vertx) {
  private lateinit var persistentDealRepository: PersistentDealRepository

  private lateinit var repository: CachingDealRepository

  @BeforeEach
  fun setup() {
    clearAllMocks()
    persistentDealRepository = mockk<PersistentDealRepository>()
    repository = CachingDealRepository(persistentDealRepository)
  }

  @Test
  fun `should delegate new deal call`() = runTest {
    val dealEntity = mockk<DealEntity>()
    val newDeal = mockk<NewDeal>()

    coEvery { persistentDealRepository.upsert(any()) } returns dealEntity

    repository.upsert(newDeal) shouldBe dealEntity

    coVerify(exactly = 1) {
      persistentDealRepository.upsert(newDeal)
    }
  }

  @Test
  fun `should cache newly added deal`() = runTest {
    val dealEntity = mockk<DealEntity>()
    val newDeal = mockk<NewDeal>()

    every { newDeal.id } returns "1"
    every { dealEntity.id } returns newDeal.id
    coEvery { persistentDealRepository.upsert(any()) } returns dealEntity
    coEvery { persistentDealRepository.getAll(any(), any()) } returns emptyList()

    repository.upsert(newDeal)

    repository.getAll(0, Int.MAX_VALUE).shouldContainExactly(dealEntity)

    clearAllMocks()

    coVerify(exactly = 0) {
      persistentDealRepository.getAll(any(), any())
    }
  }

  @Test
  fun `should remove entry when deal is deleted`() = runTest {
    val entity1 = mockk<DealEntity>()
    val entity2 = mockk<DealEntity>()

    val newDeal1 = mockk<NewDeal>()
    val newDeal2 = mockk<NewDeal>()

    every { newDeal1.id } returns "1"
    every { newDeal2.id } returns "2"

    every { entity1.id } returns newDeal1.id
    every { entity2.id } returns newDeal2.id

    coEvery { persistentDealRepository.upsert(newDeal1) } returns entity1
    coEvery { persistentDealRepository.upsert(newDeal2) } returns entity2

    coEvery { persistentDealRepository.delete("1") } returns entity1
    coEvery { persistentDealRepository.delete("2") } returns entity2

    coEvery { persistentDealRepository.getAll(any(), any()) } returns emptyList()

    repository.upsert(newDeal1)
    repository.upsert(newDeal2)

    repository.getAll(0, Int.MAX_VALUE).shouldContainExactly(entity1, entity2)

    repository.delete(entity2.id)

    repository.getAll(0, Int.MAX_VALUE).shouldContainExactly(entity1)
  }

  @Test
  fun `should respect skip and take`() = runTest {
    val entity1 = mockk<DealEntity>()
    val entity2 = mockk<DealEntity>()
    val entity3 = mockk<DealEntity>()

    val newDeal1 = mockk<NewDeal>()
    val newDeal2 = mockk<NewDeal>()
    val newDeal3 = mockk<NewDeal>()

    every { newDeal1.id } returns "1"
    every { newDeal2.id } returns "2"
    every { newDeal3.id } returns "3"

    every { entity1.id } returns newDeal1.id
    every { entity2.id } returns newDeal2.id
    every { entity3.id } returns newDeal3.id

    coEvery { persistentDealRepository.upsert(newDeal1) } returns entity1
    coEvery { persistentDealRepository.upsert(newDeal2) } returns entity2
    coEvery { persistentDealRepository.upsert(newDeal3) } returns entity3

    coEvery { persistentDealRepository.getAll(any(), any()) } returns emptyList()

    repository.upsert(newDeal1)
    repository.upsert(newDeal2)
    repository.upsert(newDeal3)

    println(repository.getAll(0, Int.MAX_VALUE))

    repository.getAll(0, 1).shouldContainExactly(entity1)
    repository.getAll(1, 1).shouldContainExactly(entity2)
    repository.getAll(2, 1).shouldContainExactly(entity3)
  }
}
