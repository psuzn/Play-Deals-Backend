package me.sujanpoudel.playdeals.repositories

import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealEntity

interface DealRepository {
  suspend fun getAll(skip: Int, take: Int): List<DealEntity>

  suspend fun upsert(appDeal: NewDeal): DealEntity

  suspend fun delete(id: String): DealEntity?

  suspend fun getPotentiallyExpiredDeals(): List<DealEntity>
}
