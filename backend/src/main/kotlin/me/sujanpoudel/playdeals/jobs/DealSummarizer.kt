package me.sujanpoudel.playdeals.jobs

import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.domain.entities.formattedCurrentPrice
import me.sujanpoudel.playdeals.domain.entities.formattedNormalPrice
import me.sujanpoudel.playdeals.infoNotify
import me.sujanpoudel.playdeals.logger
import me.sujanpoudel.playdeals.repositories.DealRepository
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import me.sujanpoudel.playdeals.services.MessagingService
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.scheduling.RecurringJobBuilder
import org.jobrunr.scheduling.cron.Cron
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.OffsetDateTime
import java.util.UUID

class DealSummarizer(
  override val di: DI,
) : CoJobRequestHandler<DealSummarizer.Request>(), DIAware {
  private val dealRepository by instance<DealRepository>()
  private val keyValueRepository by instance<KeyValuesRepository>()
  private val messagingService by instance<MessagingService>()

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest",
  ) {
    val lastTimestamp =
      keyValueRepository.get(LAST_SUMMARY_TIMESTAMP)?.let(OffsetDateTime::parse)
        ?: OffsetDateTime.now()

    val deals = dealRepository.getNewDeals(lastTimestamp)

    if (deals.isNotEmpty()) {
      val maxCount = 6
      val dealsDescription =
        deals
          .take(maxCount)
          .mapIndexed { index, deal ->
            "${index + 1}. ${deal.name} was ${deal.formattedNormalPrice()} is now ${deal.formattedCurrentPrice()}"
          }.joinToString("\n")

      messagingService.sendMessageToTopic(
        topic = Constants.PushNotificationTopic.DEALS_SUMMARY,
        title = "New ${deals.size} app deals are found since yesterday",
        body =
          if (deals.size > maxCount) {
            "$dealsDescription\n\n +${deals.size - maxCount} more..."
          } else {
            dealsDescription
          },
      )
    } else {
      logger.infoNotify("$SIMPLE_NAME:: haven't got any deals since $lastTimestamp")
    }

    keyValueRepository.set(LAST_SUMMARY_TIMESTAMP, OffsetDateTime.now().toString())
  }

  class Request private constructor() : JobRequest {
    override fun getJobRequestHandler() = DealSummarizer::class.java

    companion object {
      private val JOB_ID: UUID = UUID.nameUUIDFromBytes("deal-summarizer".toByteArray())

      operator fun invoke(): RecurringJobBuilder = RecurringJobBuilder.aRecurringJob()
        .withJobRequest(Request())
        .withCron(Cron.daily(16))
        .withAmountOfRetries(2)
        .withLabels("Deal Summarizer")
        .withName("Deal Summarizer")
        .withId(JOB_ID.toString())
    }
  }

  companion object {
    private const val LAST_SUMMARY_TIMESTAMP = "LAST_SUMMARY_TIME"
  }
}
