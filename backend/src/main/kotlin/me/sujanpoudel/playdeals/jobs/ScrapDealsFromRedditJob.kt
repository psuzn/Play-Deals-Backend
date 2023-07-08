package me.sujanpoudel.playdeals.jobs

import me.sujanpoudel.playdeals.log

object ScrapDealsFromRedditJob {
  const val ID = "ScrapDealsFromRedditJob"

  @JvmStatic
  fun run() {
    log.info("scrapping deals from reddit $this")
  }
}
