package me.sujanpoudel.playdeals.repositories.caching

import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.logger
import me.sujanpoudel.playdeals.repositories.DealRepository

class CachingDealRepository(
  private val delegate: DealRepository
) : DealRepository by delegate {

  private val cache by lazy {
    HashMap<String, DealEntity>(0, 0.8f)
  }

  private var cacheInitialized = false

  private suspend fun initialize() {
    if (cacheInitialized) return

    try {
      cache.putAll(delegate.getAll(0, Int.MAX_VALUE).map { it.id to it })
      cacheInitialized = true
    } catch (e: Exception) {
      logger.error(e) {
        "Error while preloading cache"
      }
    }
  }

  override suspend fun getAll(skip: Int, take: Int): List<DealEntity> {
    initialize()
    return if (cacheInitialized) {
      cache.values.toList().drop(skip).take(take)
    } else {
      delegate.getAll(skip, take)
    }
  }

  override suspend fun upsert(appDeal: NewDeal): DealEntity {
    initialize()
    return delegate.upsert(appDeal).also { entity ->
      if (cacheInitialized) {
        cache[entity.id] = entity
      }
    }
  }

  override suspend fun delete(id: String): DealEntity? {
    initialize()
    return delegate.delete(id).also {
      if (cacheInitialized) {
        cache.remove(id)
      }
    }
  }
}
