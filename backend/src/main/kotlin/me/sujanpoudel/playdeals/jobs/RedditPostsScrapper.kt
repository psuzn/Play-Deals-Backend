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
import me.sujanpoudel.playdeals.repositories.get
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class RedditPost(
  val id: String,
  val content: String,
  val createdAt: OffsetDateTime
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

  override suspend fun handleRequest(jobRequest: Request): Unit = loggingExecutionTime(
    "$SIMPLE_NAME:: handleRequest"
  ) {
    val lastPostTime = keyValueRepository.get<String>(LAST_REDDIT_POST_TIME)?.let(OffsetDateTime::parse)

    val posts = loggingExecutionTime(
      "$SIMPLE_NAME:: Fetched reddit post, last created post was at : '$lastPostTime'"
    ) {
      getLatestRedditPosts(lastPostTime ?: OffsetDateTime.MIN)
    }

    val appIds = posts.flatMap { post ->
      PLAY_CONSOLE_REGX.findAll(post.content).toList().mapNotNull {
        it.groupValues.lastOrNull()
      }
    }.distinct()

    log.info("$SIMPLE_NAME:: got ${posts.size} new posts (${appIds.size} Links)")

    appIds.forEach { packageName ->
      val id = UUID.nameUUIDFromBytes(packageName.toByteArray())
      jobRequestScheduler.enqueue(id, AppDetailScrapper.Request(packageName))
    }

    posts.firstOrNull()?.let {
      log.info("$SIMPLE_NAME:: Last reddit post was at ${it.createdAt} with id ${it.id}")
      keyValueRepository.set(LAST_REDDIT_POST_TIME, it.createdAt.toString())
    }
  }

  private suspend fun getLatestRedditPosts(lastPostTime: OffsetDateTime): List<RedditPost> {
    val path = "/r/googleplaydeals/new.json?limit=100"

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
            content = data.getString("selftext").trim().ifBlank { data.getString("url") },
            createdAt = data.getDouble("created").toLong().let {
              OffsetDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC)
            }
          )
        }
      }
      .await()
      .filter {
        it.createdAt > lastPostTime
      }
  }

  class Request : JobRequest {
    override fun getJobRequestHandler() = RedditPostsScrapper::class.java
  }

  companion object {
    const val LAST_REDDIT_POST_TIME = "LAST_REDDIT_POST_TIME"

    val JOB_ID: UUID = UUID.nameUUIDFromBytes("Reddit Posts".toByteArray())
    val PLAY_CONSOLE_REGX = Regex("https://play\\.google\\.com/store/apps/details\\?id=([(a-z_A-Z-0-9.]+)")
  }
}

fun RedditPostsScrapper.Request.asRecurringRequest(): RecurringJobBuilder = RecurringJobBuilder.aRecurringJob()
  .withJobRequest(this)
  .withAmountOfRetries(2)
  .withLabels("Reddit")
  .withName("Reddit Post Scrap")
  .withId(RedditPostsScrapper.JOB_ID.toString())
  .withDuration(Duration.ofHours(1))
