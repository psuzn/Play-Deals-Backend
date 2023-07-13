package me.sujanpoudel.playdeals.api.appDeals

import io.vertx.core.MultiMap
import me.sujanpoudel.playdeals.exceptions.ClientErrorException
import me.sujanpoudel.playdeals.usecases.Validated

class GetAppDealsContext(
  private val param: MultiMap
) : Validated {

  val skip by lazy { param.get("skip")?.toIntOrNull() ?: 0 }
  val take by lazy { param.get("take")?.toIntOrNull() ?: 10 }

  override suspend fun validate() {
    when {
      skip < 0 -> throw ClientErrorException("skip Can't be less than 0")
      take <= 0 -> throw ClientErrorException("take Can't be less than 1")
    }
  }
}
