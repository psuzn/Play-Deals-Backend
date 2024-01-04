package me.sujanpoudel.playdeals.domain

import java.time.OffsetDateTime

// Rates are USD based
data class ForexRate(
  val timestamp: OffsetDateTime,
  val rates: List<ConversionRate>
)

data class ConversionRate(
  val currency: String,
  val symbol: String,
  val name: String,
  val flag: String,
  val rate: Float
)
