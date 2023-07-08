package me.sujanpoudel.playdeals.jobs

import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler

object AppDetailScrapJob : JobRequestHandler<AppDetailScrapJob.Request> {
  data class Request(val id: String) : JobRequest {
    override fun getJobRequestHandler() = AppDetailScrapJob::class.java
  }

  override fun run(jobRequest: Request) {
    println("AppScrapper Scrapping app details ${jobRequest.id}")
    Thread.sleep(1000)
    println("AppScrapper Scrapped app details ${jobRequest.id}")
  }
}
