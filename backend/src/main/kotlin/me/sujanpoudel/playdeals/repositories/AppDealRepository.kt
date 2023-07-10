package me.sujanpoudel.playdeals.repositories

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.NewAppDeal
import me.sujanpoudel.playdeals.domain.entities.AppDeal
import me.sujanpoudel.playdeals.domain.entities.asAppDeal
import me.sujanpoudel.playdeals.domain.insertValues

class AppDealRepository(
  private val sqlClient: SqlClient
) {
  suspend fun getAll(skip: Int, take: Int): List<AppDeal> {

    return sqlClient.preparedQuery(
      """
      SELECT * FROM "app_deal" ORDER BY created_at DESC OFFSET $1 LIMIT $2
      """.trimIndent()
    ).exec(skip, take)
      .await()
      .map { it.asAppDeal() }
  }

  suspend fun upsert(appDeal: NewAppDeal): AppDeal {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "app_deal" (id, name, icon, images, normal_price, current_price, currency, store_url, category, downloads, rating, offer_expires_in)
      VALUES ( $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
      ON CONFLICT(id) DO UPDATE SET
        name             = $2,
        icon             = $3,
        images           = $4,
        normal_price     = $5,
        current_price    = $6,
        currency         = $7,
        store_url        = $8,
        category         = $9,
        downloads        = $10,
        rating           = $11,
        offer_expires_in = $12
      RETURNING *
      """.trimIndent()
    ).exec(*appDeal.insertValues)
      .await()
      .first()
      .asAppDeal()
  }

  suspend fun delete(id: String): AppDeal? {
    return sqlClient.preparedQuery(
      """
      DELETE from "app_deal" where id=$1 RETURNING *
      """.trimIndent()
    ).exec(id)
      .await()
      .firstOrNull()?.asAppDeal()
  }

  suspend fun getPotentiallyExpiredDeals(): List<AppDeal> {
    return sqlClient.preparedQuery(
      """
      SELECT * FROM "app_deal" where offer_expires_in < current_timestamp
      """.trimIndent()
    ).exec()
      .await()
      .map { it.asAppDeal() }
  }
}
