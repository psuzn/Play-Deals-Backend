package me.sujanpoudel.playdeals.domain.entities

import io.vertx.sqlclient.Row
import me.sujanpoudel.playdeals.common.asEnum
import me.sujanpoudel.playdeals.common.get

fun Row.asAppDeal(): DealEntity {
  return DealEntity(
    id = get("id"),
    name = get("name"),
    icon = get("icon"),
    images = get<Array<String>>("images").toList(),
    normalPrice = get("normal_price"),
    currentPrice = get("current_price"),
    currency = get("currency"),
    url = get("url"),
    category = get("category"),
    downloads = get("downloads"),
    rating = get("rating"),
    offerExpiresIn = get("offer_expires_in"),
    type = getString("type").asEnum(),
    source = get("source"),

    createdAt = get("created_at"),
    updatedAt = get("updated_at")
  )
}

fun Row?.valueOrNull() = this?.getString("value")
fun Row.value(): String = getString("value")
