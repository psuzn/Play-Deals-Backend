package me.sujanpoudel.playdeals.domain

data class NewAppDeal(
  val id: String,
  val name: String,
  val icon: String,
  val images: List<String> = emptyList(),
  val normalPrice: Float,
  val currentPrice: Float,
  val currency: String,
  val storeUrl: String,
  val expired: Boolean = false,
  val category: String,
  val downloads: String = "0",
  val rating: String = "unknown",
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
    expired,
    category,
    downloads,
    rating,
  )
