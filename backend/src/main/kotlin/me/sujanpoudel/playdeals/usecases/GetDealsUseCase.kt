package me.sujanpoudel.playdeals.usecases

import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.repositories.DealRepository
import org.kodein.di.DI
import org.kodein.di.instance

class GetDealsUseCase(
  di: DI
) : UseCase<GetDealsUseCase.Input, List<DealEntity>> {

  private val appDealsRepository by di.instance<DealRepository>()

  class Input(val skip: Int, val take: Int)

  override suspend fun doExecute(input: Input): List<DealEntity> {
    return appDealsRepository.getAll(input.skip, input.take)
  }
}
