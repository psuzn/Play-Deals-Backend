package me.sujanpoudel.playdeals.domain.entities

import java.time.OffsetDateTime

data class AppDeal(
  val id: String,
  val name: String,
  val icon: String,
  val images: List<String>,
  val normalPrice: Float,
  val currentPrice: Float,
  val currency: String,
  val storeUrl: String,
  val category: String,
  val downloads: String,
  val rating: String,
  val offerExpiresIn: OffsetDateTime,
  val createdAt: OffsetDateTime,
  val updatedAt: OffsetDateTime
)
