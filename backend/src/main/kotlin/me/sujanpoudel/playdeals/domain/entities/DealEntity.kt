package me.sujanpoudel.playdeals.domain.entities

import java.time.OffsetDateTime
import kotlin.math.roundToInt

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

private fun String.asCurrencySymbol() = when (this) {
  "USD" -> "$"
  else -> this
}

private fun Float.formatAsPrice(): String {
  val int = toInt()
  val decimal = ((this - int) * 100).roundToInt()

  val formattedDecimal = if (decimal < 10) {
    "${decimal}0"
  } else {
    "$decimal"
  }

  return "$int.$formattedDecimal"
}

fun DealEntity.formattedCurrentPrice() = "${currency.asCurrencySymbol()}${normalPrice.formatAsPrice()}"

fun DealEntity.formattedNormalPrice() = "${currency.asCurrencySymbol()}${normalPrice.formatAsPrice()}"
