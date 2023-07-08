package me.sujanpoudel.playdeals.jobs

import me.sujanpoudel.playdeals.log
import org.kodein.di.DirectDI

class ScrapReddit(private val di: DirectDI) {

  companion object {
    const val ID = "scrapRedditForNewDeals"
  }

  fun action() {
    log.info("scrapping deals from reddit")
    log.info("scrapping deals from reddit")
    log.info("scrapping deals from reddit")
    log.info("scrapping deals from reddit")
    log.info("scrapping deals from reddit")
  }
}
