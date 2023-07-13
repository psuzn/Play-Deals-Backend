package me.sujanpoudel.playdeals.usecases

import me.sujanpoudel.playdeals.jobs.AppDetailScrapper
import org.jobrunr.scheduling.JobRequestScheduler
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.UUID

class NewAppDealUseCase(
  di: DI
) : UseCase<String, Unit> {

  private val jobRequestScheduler by di.instance<JobRequestScheduler>()

  override suspend fun doExecute(input: String) {
    val id = UUID.nameUUIDFromBytes(input.toByteArray())
    jobRequestScheduler.enqueue(id, AppDetailScrapper.Request(input))
  }

}

