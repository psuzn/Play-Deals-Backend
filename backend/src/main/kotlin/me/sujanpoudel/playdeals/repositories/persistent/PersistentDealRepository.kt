package me.sujanpoudel.playdeals.repositories.persistent

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.NewDeal
import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.domain.entities.asAppDeal
import me.sujanpoudel.playdeals.domain.insertValues
import me.sujanpoudel.playdeals.repositories.DealRepository
import java.time.OffsetDateTime

class PersistentDealRepository(
  private val sqlClient: SqlClient
) : DealRepository {
  override suspend fun getAll(skip: Int, take: Int): List<DealEntity> {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "deal" ORDER BY created_at DESC OFFSET $1 LIMIT $2
      """.trimIndent()
    ).exec(skip, take)
      .await()
      .map { it.asAppDeal() }
  }

  override suspend fun upsert(appDeal: NewDeal): DealEntity {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "deal" (id, name, icon, images, normal_price, current_price, currency, url, category, downloads, rating, offer_expires_in, type, source)
      VALUES ( $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
      ON CONFLICT(id) DO UPDATE SET
        name             = $2,
        icon             = $3,
        images           = $4,
        normal_price     = $5,
        current_price    = $6,
        currency         = $7,
        url              = $8,
        category         = $9,
        downloads        = $10,
        rating           = $11,
        offer_expires_in = $12,
        type             = $13,
        source           = $14
      RETURNING *
      """.trimIndent()
    ).exec(*appDeal.insertValues)
      .await()
      .first()
      .asAppDeal()
  }

  override suspend fun delete(id: String): DealEntity? {
    return sqlClient.preparedQuery(
      """
      DELETE from "deal" where id=$1 RETURNING *
      """.trimIndent()
    ).exec(id)
      .await()
      .firstOrNull()?.asAppDeal()
  }

  override suspend fun getPotentiallyExpiredDeals(): List<DealEntity> {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "deal" where offer_expires_in < current_timestamp
      """.trimIndent()
    ).exec()
      .await()
      .map { it.asAppDeal() }
  }

  override suspend fun getNewDeals(since: OffsetDateTime): List<DealEntity> {
    return sqlClient.preparedQuery(
      """
      SELECT *  FROM "deal" where created_at > $1
      """.trimIndent()
    ).exec(since)
      .await()
      .map { it.asAppDeal() }
  }

  override suspend fun getDealByPackageName(packageName: String): DealEntity? {
    return sqlClient.preparedQuery(
      """
      SELECT *  FROM "deal" where id = $1
      """.trimIndent()
    ).exec(packageName)
      .await()
      .firstOrNull()?.asAppDeal()
  }
}
