package me.sujanpoudel.playdeals.domain.entities

import io.vertx.core.json.Json
import io.vertx.sqlclient.Row
import me.sujanpoudel.playdeals.common.get
import java.io.Serializable

fun Row.asAppDeal(): AppDeal {
  return AppDeal(
    id = get("id"),
    name = get("name"),
    icon = get("icon"),
    images = get<Array<String>>("images").toList(),
    normalPrice = get("normal_price"),
    currentPrice = get("current_price"),
    currency = get("currency"),
    storeUrl = get("store_url"),
    category = get("category"),
    downloads = get("downloads"),
    rating = get("rating"),
    offerExpiresIn = get("offer_expires_in"),
    createdAt = get("created_at"),
    updatedAt = get("updated_at"),
  )
}

inline fun <reified T : Serializable> Row.asKeyValue(): KeyValue<T> {
  return KeyValue(
    get("key"),
    getString("value").let {
      Json.CODEC.fromValue(it, T::class.java)
    }
  )
}
