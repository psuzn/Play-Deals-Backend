package me.sujanpoudel.playdeals.common

import me.sujanpoudel.playdeals.log

inline fun <T> loggingExecutionTime(message: String, action: () -> T): T {
  val before = System.currentTimeMillis()
  return action.invoke().also {
    log.info("$message (took ${System.currentTimeMillis() - before}ms)")
  }

}

val Any.SIMPLE_NAME
  get() = this::class.simpleName
