package me.sujanpoudel.playdeals.domain

import java.time.OffsetDateTime

data class AppDetail(
  val id: String,
  val name: String,
  val icon: String,
  val images: List<String> = emptyList(),
  val normalPrice: Float,
  val currentPrice: Float?,
  val currency: String,
  val storeUrl: String,
  val category: String,
  val downloads: String,
  val rating: String,
  val offerExpiresIn: OffsetDateTime?
)

fun AppDetail.asNewAppDeal() = NewAppDeal(
  id = id,
  name = name,
  icon = icon,
  images = images,
  normalPrice = normalPrice,
  currentPrice = currentPrice!!,
  currency = currency,
  storeUrl = storeUrl,
  category = category,
  downloads = downloads,
  rating = rating,
  offerExpiresIn = offerExpiresIn!!
)
