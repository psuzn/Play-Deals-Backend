package me.sujanpoudel.playdeals.domain

import java.time.Instant

data class NewPotentialDeal(
  val id: String,
  val scrapAttempts: Int = 0,
  val lastScrapAttemptAt: Instant? = null,
  val source: String,
)

val NewPotentialDeal.insertValues: Array<out Any?>
  get() = arrayOf(
    id,
    scrapAttempts,
    lastScrapAttemptAt,
    source,
  )
