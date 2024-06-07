package me.sujanpoudel.playdeals.domain

import me.sujanpoudel.playdeals.domain.entities.DealType
import java.time.OffsetDateTime

data class NewDeal(
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
  val offerExpiresIn: OffsetDateTime,
  val type: DealType,
  val source: String,
)

val NewDeal.insertValues
  get() =
    arrayOf(
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
      offerExpiresIn,
      type.toString(),
      source,
    )
