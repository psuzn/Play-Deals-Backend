package me.sujanpoudel.playdeals.domain.entities

import io.vertx.sqlclient.Row
import me.sujanpoudel.playdeals.common.get

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
    expired = get("expired"),
    category = get("category"),
    downloads = get("downloads"),
    rating = get("rating"),
    createdAt = get("created_at"),
    updatedAt = get("updated_at"),
  )
}

fun Row.asPotentialDeal(): PotentialDeal {
  return PotentialDeal(
    id = get("id"),
    scrapAttempts = get("scrap_attempts"),
    lastScrapAttemptAt = get("last_scrap_attempt_at"),
    source = get("source"),
    createdAt = get("created_at"),
    updatedAt = get("updated_at"),
  )
}
