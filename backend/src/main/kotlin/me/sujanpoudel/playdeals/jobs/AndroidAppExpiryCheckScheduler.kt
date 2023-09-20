package me.sujanpoudel.playdeals.jobs

import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.repositories.AppDealRepository
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Duration
import java.util.UUID

class AndroidAppExpiryCheckScheduler(
  override val di: DI
) : CoJobRequestHandler<AndroidAppExpiryCheckScheduler.Request>(), DIAware {

  private val repository by instance<AppDealRepository>()
  private val requestScheduler by instance<JobRequestScheduler>()

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest"
  ) {
    val apps = repository.getPotentiallyExpiredDeals().stream()
      .map { AppDetailScrapper.Request(it.id) }

    requestScheduler.enqueue(apps)
  }

  class Request : JobRequest {
    override fun getJobRequestHandler() = AndroidAppExpiryCheckScheduler::class.java
  }

  companion object {
    val JOB_ID: UUID = UUID.nameUUIDFromBytes("AppExpiryCheckScheduler".toByteArray())
  }
}

fun AndroidAppExpiryCheckScheduler.Request.asRecurringRequest(): RecurringJobBuilder = RecurringJobBuilder.aRecurringJob()
  .withJobRequest(this)
  .withName("App Expiry Checker")
  .withId(AndroidAppExpiryCheckScheduler.JOB_ID.toString())
  .withDuration(Duration.ofHours(6))
