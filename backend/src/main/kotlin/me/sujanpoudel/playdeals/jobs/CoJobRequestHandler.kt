package me.sujanpoudel.playdeals.jobs

import kotlinx.coroutines.runBlocking
import me.sujanpoudel.playdeals.infoNotify
import me.sujanpoudel.playdeals.logger
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler

abstract class CoJobRequestHandler<T : JobRequest> : JobRequestHandler<T> {
  override fun run(jobRequest: T): Unit = runBlocking {
    handleRequest(jobRequest)
  }

  abstract suspend fun handleRequest(jobRequest: T)
}

fun JobRequestHandler<*>.info(message: String) {
  jobContext().logger().info(message)
  logger.info(message)
}

fun JobRequestHandler<*>.infoNotify(message: String) {
  jobContext().logger().info(message)
  logger.infoNotify(message)
}
