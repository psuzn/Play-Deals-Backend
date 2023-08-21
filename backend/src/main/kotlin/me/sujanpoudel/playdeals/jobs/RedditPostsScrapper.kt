package me.sujanpoudel.playdeals.jobs

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.coroutines.await
import me.sujanpoudel.playdeals.common.SIMPLE_NAME
import me.sujanpoudel.playdeals.common.loggingExecutionTime
import me.sujanpoudel.playdeals.log
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.scheduling.JobBuilder
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.storage.StorageProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Duration
import java.util.UUID

data class RedditPost(
  val id: String,
  val content: String
)

class RedditPostsScrapper(
  override val di: DI
) : CoJobRequestHandler<RedditPostsScrapper.Request>(), DIAware {

  private val verticle by instance<BackgroundJobsVerticle>()
  private val keyValueRepository by instance<KeyValuesRepository>()
  private val webClient by lazy {
    WebClient.create(
      verticle.vertx,
      WebClientOptions().setDefaultHost("www.reddit.com")
    )
  }
  private val jobRequestScheduler by instance<JobRequestScheduler>()
  private val storageProvider by instance<StorageProvider>()

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest"
  ) {
    val lastRedditPost = keyValueRepository.get<String>(LAST_REDDIT_POST)

    val posts = loggingExecutionTime(
      "$SIMPLE_NAME:: Fetched reddit post, last post id was : '$lastRedditPost'"
    ) {
      getLatestRedditPosts(lastRedditPost)
    }

    val appLinks = posts.flatMap { post ->
      PLAY_CONSOLE_REGX.matchEntire(post.content)?.groups.orEmpty()
        .drop(1)
        .filterNotNull()
        .map { it.value }
    }.distinct()

    log.info("$SIMPLE_NAME:: got ${posts.size} new posts (${appLinks.size} Links)")

    appLinks.forEach { packageName ->
      val id = UUID.nameUUIDFromBytes(packageName.toByteArray())
      jobRequestScheduler.enqueue(id, AppDetailScrapper.Request(packageName))
    }

    posts.firstOrNull()?.let {
      log.info("$SIMPLE_NAME:: Last reddit post id is ${it.content}")
      keyValueRepository.set(LAST_REDDIT_POST, it.id)
    }

    rescheduleNextTick()
  }

  private fun rescheduleNextTick() {
    verticle.vertx.setTimer(1000) {
      storageProvider.deletePermanently(JOB_ID)
      jobRequestScheduler.create(
        Request().asJob(Duration.ofHours(1))
      )
    }
  }

  private suspend fun getLatestRedditPosts(after: String?): List<RedditPost> {
    val path = "/r/googleplaydeals/new.json?limit=100&before=${after.orEmpty()}"

    return webClient.get(path)
      .send()
      .map {
        if (it.statusCode() == 200) {
          it.bodyAsJsonObject()
            .getJsonObject("data")
            .getJsonArray("children")
        } else {
          log.error("Error while getting reddit post : ${it.bodyAsString()}")
          jsonArrayOf()
        }
      }
      .map { posts ->
        posts.map { post ->
          val data = (post as JsonObject).getJsonObject("data")
          RedditPost(
            id = data.getString("name"),
            content = data.getString("selftext")
          )
        }
      }
      .await()
  }

  class Request : JobRequest {
    override fun getJobRequestHandler() = RedditPostsScrapper::class.java
  }

  companion object {
    const val LAST_REDDIT_POST = "LAST_REDDIT_POST"

    val JOB_ID: UUID = UUID.nameUUIDFromBytes("Reddit Posts".toByteArray())
    val PLAY_CONSOLE_REGX = Regex("https://play\\.google\\.com/store/apps/details\\?id=([(a-zA-Z-0-9.]+)")
  }
}

fun RedditPostsScrapper.Request.asJob(
  duration: Duration = Duration.ofHours(1)
): JobBuilder = JobBuilder.aJob()
  .withJobRequest(this)
  .withAmountOfRetries(2)
  .scheduleIn(duration)
  .withLabels("Reddit")
  .withName("Reddit Post Scrap")
  .withId(RedditPostsScrapper.JOB_ID)
