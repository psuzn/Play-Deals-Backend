package me.sujanpoudel.playdeals.jobs

import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.logger
import me.sujanpoudel.playdeals.repositories.DealRepository
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.jobs.states.StateName
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder
import org.jobrunr.storage.StorageProvider
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AndroidAppExpiryCheckScheduler(
  private val repository: DealRepository,
  private val requestScheduler: JobRequestScheduler,
  private val storageProvider: StorageProvider,
) : CoJobRequestHandler<AndroidAppExpiryCheckScheduler.Request>() {
  override suspend fun handleRequest(jobRequest: Request): Unit =
    loggingExecutionTime(
      "$SIMPLE_NAME:: handleRequest",
    ) {
      val apps =
        repository.getPotentiallyExpiredDeals().stream()
          .map { AppDetailScrapper.Request(it.id) }

      requestScheduler.enqueue(apps)

      val lastUpdatedTime = Instant.now().minus(1, ChronoUnit.HOURS)
      val jobs = storageProvider.deleteJobsPermanently(StateName.FAILED, lastUpdatedTime)
      logger.info("deleted FAILED `$jobs`")
    }

  class Request private constructor() : JobRequest {
    override fun getJobRequestHandler() = AndroidAppExpiryCheckScheduler::class.java

    companion object {
      private val JOB_ID: UUID = UUID.nameUUIDFromBytes("AppExpiryCheckScheduler".toByteArray())

      operator fun invoke(): RecurringJobBuilder =
        RecurringJobBuilder.aRecurringJob()
          .withJobRequest(Request())
          .withName("App Expiry Checker")
          .withId(JOB_ID.toString())
          .withDuration(Duration.ofHours(6))
    }
  }
}
