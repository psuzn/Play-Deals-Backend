package me.sujanpoudel.playdeals.jobs

import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrConfiguration
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.storage.StorageProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

class BackgroundJobsVerticle(
  override val di: DI
) : CoroutineVerticle(), DIAware {

  private val storageProvider by instance<StorageProvider>()
  private val jobRequestScheduler by instance<JobRequestScheduler>()

  override suspend fun start() {
    direct.instance<JobRunrConfiguration.JobRunrConfigurationResult>()
    setupRecurringJobs()
  }

  private fun setupRecurringJobs() {
    jobRequestScheduler.createRecurrently(
      RedditPostsScrapper.Request().asRecurringRequest()
    )

    jobRequestScheduler.createRecurrently(
      AndroidAppExpiryCheckScheduler.Request().asRecurringRequest()
    )
  }

  override suspend fun stop() {
    super.stop()
    JobRunr.destroy()
  }
}
