package me.sujanpoudel.playdeals.repositories

import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import me.sujanpoudel.playdeals.common.exec
import me.sujanpoudel.playdeals.domain.NewPotentialDeal
import me.sujanpoudel.playdeals.domain.entities.PotentialDeal
import me.sujanpoudel.playdeals.domain.entities.asPotentialDeal
import me.sujanpoudel.playdeals.domain.insertValues

class PotentialDealRepository(
  private val sqlClient: SqlClient
) {
  suspend fun getAll(skip: Int, take: Int): List<PotentialDeal> {

    return sqlClient.preparedQuery(
      """
      SELECT * FROM "potential_deal" ORDER BY created_at DESC OFFSET $1 LIMIT $2
      """.trimIndent()
    ).exec(skip, take)
      .await()
      .map { it.asPotentialDeal() }
  }

  suspend fun createNew(appDeal: NewPotentialDeal): PotentialDeal {
    return sqlClient.preparedQuery(
      """
      INSERT INTO "potential_deal" values ($1,$2,$3,$4) RETURNING *
      """.trimIndent()
    ).exec(*appDeal.insertValues)
      .await()
      .first()
      .asPotentialDeal()
  }

  suspend fun delete(id: String): PotentialDeal {
    return sqlClient.preparedQuery(
      """
      DELETE from "potential_deal" where id=$1 RETURNING *
      """.trimIndent()
    ).exec(id)
      .await()
      .first()
      .asPotentialDeal()
  }
}
