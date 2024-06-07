package me.sujanpoudel.playdeals.usecases

import me.sujanpoudel.playdeals.jobs.AppDetailScrapper
import me.sujanpoudel.playdeals.repositories.DealRepository
import org.jobrunr.scheduling.JobRequestScheduler
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.UUID

class NewDealUseCase(
  di: DI,
) : UseCase<String, Unit> {
  private val jobRequestScheduler by di.instance<JobRequestScheduler>()
  private val dealsRepository by di.instance<DealRepository>()

  override suspend fun doExecute(input: String) {
    val existingDeal = dealsRepository.getDealByPackageName(input)

    if (existingDeal != null) {
      return
    }

    val id = UUID.nameUUIDFromBytes(input.toByteArray())

    jobRequestScheduler.enqueue(id, AppDetailScrapper.Request(input))
  }
}
