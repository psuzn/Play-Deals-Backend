package me.sujanpoudel.playdeals.services

import com.google.api.core.ApiFuture
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import me.sujanpoudel.playdeals.Constants
import me.sujanpoudel.playdeals.Environment
import me.sujanpoudel.playdeals.domain.entities.DealEntity
import me.sujanpoudel.playdeals.domain.entities.formattedCurrentPrice
import me.sujanpoudel.playdeals.domain.entities.formattedNormalPrice
import me.sujanpoudel.playdeals.logger
import java.util.concurrent.TimeUnit

suspend fun <T> ApiFuture<T>.awaitIgnoring() {
  withContext(SupervisorJob()) {
    try {
      this@awaitIgnoring.get(30, TimeUnit.SECONDS)
    } catch (e: Exception) {
      logger.error(e) { "error while awaiting future" }
    }
  }
}

class MessagingService(
  private val firebaseMessaging: FirebaseMessaging,
  private val environment: Environment
) {

  private fun String.asTopic() = if (environment == Environment.PRODUCTION) this else "$this-dev"

  suspend fun sendMessageToTopic(
    topic: String,
    title: String,
    body: String,
    imageUrl: String? = null
  ) {
    val message = Message.builder()
      .setTopic(topic.asTopic())
      .setNotification(
        Notification.builder()
          .setTitle(title)
          .setBody(body)
          .setImage(imageUrl)
          .build()
      ).setAndroidConfig(
        AndroidConfig.builder()
          .setCollapseKey(topic)
          .setNotification(
            AndroidNotification.builder()
              .setPriority(AndroidNotification.Priority.HIGH)
              .setChannelId(topic)
              .build()
          )
          .build()
      ).build()

    firebaseMessaging.sendAsync(message)
      .awaitIgnoring()
  }
}

suspend inline fun MessagingService.sendMessageForNewDeal(deal: DealEntity) = sendMessageToTopic(
  topic = if (deal.currentPrice == 0f) {
    Constants.PushNotificationTopic.NEW_FREE_DEAL
  } else {
    Constants.PushNotificationTopic.NEW_DISCOUNT_DEAL
  },
  title = "New deal found",
  body = "${deal.name} was ${deal.formattedNormalPrice()} is now ${deal.formattedCurrentPrice()}",
  imageUrl = deal.icon
)

suspend inline fun MessagingService.sendMaintenanceLog(message: String) = sendMessageToTopic(
  topic = Constants.PushNotificationTopic.DEV_LOG,
  title = "Maintenance Log",
  body = message
)
