package me.sujanpoudel.playdeals.jobs

import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.jobrunr.jobs.lambdas.JobLambda
import org.jobrunr.scheduling.BackgroundJobRequest
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.scheduling.RecurringJobBuilder
import org.kodein.di.DirectDI
import org.kodein.di.instance
import java.time.Duration

interface RecurrentJob : JobLambda {
  val id: String
}

class RecurrentJobVerticle(
  private val di: DirectDI
) : CoroutineVerticle() {

  private val scheduledJobs = listOf(ScrapReddit.ID)

  override suspend fun start() {
    BackgroundJobRequest.createRecurrently(
      RecurringJobBuilder.aRecurringJob()
        .withJobRequest(AppDetailScrapJob.Request("dsds"))
        .withDuration(Duration.ofSeconds(10))
        .withAmountOfRetries(5)
        .withId(ScrapReddit.ID)
    )
  }

  override suspend fun stop() {
    val scheduler = di.instance<JobScheduler>()

    scheduledJobs.forEach {
      scheduler.delete(it)
    }
  }
}
