package me.sujanpoudel.playdeals.jobs

import kotlinx.coroutines.runBlocking
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler

abstract class CoJobRequestHandler<T : JobRequest> : JobRequestHandler<T> {
  override fun run(jobRequest: T): Unit =
    runBlocking {
      handleRequest(jobRequest)
    }

  abstract suspend fun handleRequest(jobRequest: T)
}
