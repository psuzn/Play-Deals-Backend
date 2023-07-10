package me.sujanpoudel.playdeals.domain

import java.time.Instant
import java.time.ZoneOffset

data class NewAppDeal(
  val id: String,
  val name: String,
  val icon: String,
  val images: List<String> = emptyList(),
  val normalPrice: Float,
  val currentPrice: Float,
  val currency: String,
  val storeUrl: String,
  val category: String,
  val downloads: String,
  val rating: String,
  val offerExpiresIn: Instant
)

val NewAppDeal.insertValues
  get() = arrayOf(
    id,
    name,
    icon,
    images.toTypedArray(),
    normalPrice,
    currentPrice,
    currency,
    storeUrl,
    category,
    downloads,
    rating,
    offerExpiresIn.atOffset(ZoneOffset.UTC).toLocalDateTime()
  )