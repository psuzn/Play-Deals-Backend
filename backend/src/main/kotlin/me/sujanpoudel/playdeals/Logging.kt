package me.sujanpoudel.playdeals

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.sujanpoudel.playdeals.services.MessagingService
import me.sujanpoudel.playdeals.services.sendMaintenanceLog
import mu.KLogger
import mu.KotlinLogging

val logger: KLogger = KotlinLogging.logger { }

fun KLogger.infoNotify(message: String) {
  info(message)
  val messagingService = primaryDI.get<MessagingService>()
  CoroutineScope(Dispatchers.Default).launch {
    messagingService.sendMaintenanceLog(message)
  }
}
