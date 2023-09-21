package me.sujanpoudel.playdeals.domain.entities

import java.time.OffsetDateTime

enum class DealType {
  ANDROID_APP,
  IOS_APP,
  DESKTOP_APP,
  OTHER
}

data class DealEntity(
  val id: String,
  val name: String,
  val icon: String,
  val images: List<String>,
  val normalPrice: Float,
  val currentPrice: Float,
  val currency: String,
  val url: String,
  val category: String,
  val downloads: String,
  val rating: String,
  val offerExpiresIn: OffsetDateTime,
  val type: DealType,
  val source: String,
  val createdAt: OffsetDateTime,
  val updatedAt: OffsetDateTime
)
