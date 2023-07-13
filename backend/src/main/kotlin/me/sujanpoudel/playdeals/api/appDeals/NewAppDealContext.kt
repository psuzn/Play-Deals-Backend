package me.sujanpoudel.playdeals.api.appDeals

import io.vertx.core.json.JsonObject
import me.sujanpoudel.playdeals.RegxPatterns
import me.sujanpoudel.playdeals.exceptions.ClientErrorException
import me.sujanpoudel.playdeals.usecases.Validated

class NewAppDealContext(
  private val request: JsonObject
) : Validated {

  private val packageNameField = "packageName"

  val packageName: String by lazy { request.getString(packageNameField) }

  override suspend fun validate() {
    val packageName: String? = request.getString(packageNameField)
    when {
      packageName == null -> throw ClientErrorException("$packageNameField is required")
      !RegxPatterns.playStorePackageName.asMatchPredicate().test(packageName) ->
        throw ClientErrorException.InvalidValueException(packageNameField)
    }
  }
}
