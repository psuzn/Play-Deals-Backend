package me.sujanpoudel.playdeals.jobs

import io.vertx.kotlin.coroutines.CoroutineVerticle
import me.sujanpoudel.playdeals.get
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.storage.StorageProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Duration


class BackgroundJobsVerticle(
  override val di: DI,
) : CoroutineVerticle(), DIAware {

  private val storageProvider by instance<StorageProvider>()

  override suspend fun start() {

    di.get<JobRequestScheduler>().also { jobRequestScheduler ->
      storageProvider.deletePermanently(RedditPostsScrapper.JOB_ID)
      storageProvider.deletePermanently(AppExpiryCheckScheduler.JOB_ID)

      jobRequestScheduler.create(
        RedditPostsScrapper.Request().asJob(Duration.ofMinutes(1))
      )

      jobRequestScheduler.createRecurrently(
        AppExpiryCheckScheduler.Request().asRecurringRequest()
      )

    }
  }

}
