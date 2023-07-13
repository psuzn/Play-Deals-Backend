package me.sujanpoudel.playdeals.usecases

import me.sujanpoudel.playdeals.domain.entities.AppDeal
import me.sujanpoudel.playdeals.repositories.AppDealRepository
import org.kodein.di.DI
import org.kodein.di.instance

class GetAppDealsUseCase(
  di: DI
) : UseCase<GetAppDealsUseCase.Input, List<AppDeal>> {

  private val appDealsRepository by di.instance<AppDealRepository>()

  class Input(val skip: Int, val take: Int)

  override suspend fun doExecute(input: Input): List<AppDeal> {
    return appDealsRepository.getAll(input.skip, input.take)
  }

}

