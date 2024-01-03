package me.sujanpoudel.playdeals.usecases

import me.sujanpoudel.playdeals.domain.ForexRate
import me.sujanpoudel.playdeals.jobs.getForexRate
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import org.kodein.di.DI
import org.kodein.di.instance

class GetForexUseCase(di: DI) : UseCase<Unit, ForexRate?> {

  private val appDealsRepository by di.instance<KeyValuesRepository>()

  override suspend fun doExecute(input: Unit): ForexRate? {
    return appDealsRepository.getForexRate()
  }
}
