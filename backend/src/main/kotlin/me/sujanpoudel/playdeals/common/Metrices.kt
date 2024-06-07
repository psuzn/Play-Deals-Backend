package me.sujanpoudel.playdeals.common

import me.sujanpoudel.playdeals.logger
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

inline fun <T> loggingExecutionTime(message: String, action: () -> T): T {
  val timedValue =
    measureTimedValue {
      action.invoke()
    }
  logger.info("$message (took ${timedValue.duration.toString(DurationUnit.MILLISECONDS)})")
  return timedValue.value
}

val Any.SIMPLE_NAME
  get() = this::class.simpleName
