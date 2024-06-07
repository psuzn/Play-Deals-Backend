package me.sujanpoudel.playdeals.common

import me.sujanpoudel.playdeals.jobs.info
import org.jobrunr.jobs.lambdas.JobRequestHandler
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

inline fun <T> JobRequestHandler<*>.loggingExecutionTime(message: String, action: () -> T): T {
  val timedValue = measureTimedValue { action.invoke() }
  info("$message took ${timedValue.duration.toString(DurationUnit.MILLISECONDS)}ms")
  return timedValue.value
}

val Any.SIMPLE_NAME
  get() = this::class.simpleName
