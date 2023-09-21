package me.sujanpoudel.playdeals.domain

import me.sujanpoudel.playdeals.domain.entities.DealType
import java.time.OffsetDateTime

data class AndroidAppDetail(
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
  val offerExpiresIn: OffsetDateTime?,
  val source: String
)

fun AndroidAppDetail.asNewDeal() = NewDeal(
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
  offerExpiresIn = offerExpiresIn!!,
  type = DealType.ANDROID_APP,
  source = source
)
