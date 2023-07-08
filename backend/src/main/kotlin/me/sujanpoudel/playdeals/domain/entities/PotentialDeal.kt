package me.sujanpoudel.playdeals.domain.entities

import java.time.Instant

data class PotentialDeal(
  val id: String,
  val scrapAttempts: Int,
  val lastScrapAttemptAt: Instant,
  val source: String,

  val createdAt: Instant,
  val updatedAt: Instant
)
