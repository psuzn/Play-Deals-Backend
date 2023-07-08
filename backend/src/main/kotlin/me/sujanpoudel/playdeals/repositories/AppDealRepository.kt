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

  suspend fun createNew(appDeal: NewAppDeal): AppDeal {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "app_deal" values ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12) RETURNING *
      """.trimIndent()
    ).exec(*appDeal.insertValues)
      .await()
      .first()
      .asAppDeal()
  }

  suspend fun delete(id: String): AppDeal {
    return sqlClient.preparedQuery(
      """
      DELETE from "app_deal" where id=$1 RETURNING *
      """.trimIndent()
    ).exec(id)
      .await()
      .first()
      .asAppDeal()
  }
}
