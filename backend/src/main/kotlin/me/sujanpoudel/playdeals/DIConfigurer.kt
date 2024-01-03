package me.sujanpoudel.playdeals

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import me.sujanpoudel.playdeals.api.ApiVerticle
import me.sujanpoudel.playdeals.jobs.AndroidAppExpiryCheckScheduler
import me.sujanpoudel.playdeals.jobs.AppDetailScrapper
import me.sujanpoudel.playdeals.jobs.BackgroundJobsVerticle
import me.sujanpoudel.playdeals.jobs.DealSummarizer
import me.sujanpoudel.playdeals.jobs.ForexFetcher
import me.sujanpoudel.playdeals.jobs.RedditPostsScrapper
import me.sujanpoudel.playdeals.repositories.DealRepository
import me.sujanpoudel.playdeals.repositories.KeyValuesRepository
import me.sujanpoudel.playdeals.repositories.caching.CachingDealRepository
import me.sujanpoudel.playdeals.repositories.persistent.PersistentDealRepository
import me.sujanpoudel.playdeals.repositories.persistent.PersistentKeyValuesRepository
import me.sujanpoudel.playdeals.services.MessagingService
import me.sujanpoudel.playdeals.usecases.DBHealthUseCase
import me.sujanpoudel.playdeals.usecases.GetDealsUseCase
import me.sujanpoudel.playdeals.usecases.GetForexUseCase
import me.sujanpoudel.playdeals.usecases.NewDealUseCase
import org.flywaydb.core.Flyway
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrConfiguration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.BackgroundJobServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.type.erased
import org.postgresql.ds.PGSimpleDataSource
import java.time.Duration

inline fun <reified T : Any> DI.get(tag: String? = null) = direct.instance<T>(tag)

fun configureDI(
  vertx: Vertx,
  conf: Conf
) = DI {
  bindSingleton { conf }

  bindSingleton { ApiVerticle(di = this) }
  bindSingleton { BackgroundJobsVerticle(di = di) }

  bindSingleton { DeliveryOptions().setSendTimeout(5000) }

  bindSingleton { configureObjectMapper() }

  bindSingleton {
    MainVerticle(
      apiVerticle = instance(),
      backgroundJobsVerticle = instance(),
      flywayVerticle = instance()
    )
  }

  bindSingleton {
    FlywayVerticle(instance())
  }

  bindSingleton {
    val url = "jdbc:postgresql://${conf.db.host}:${conf.db.port}/${conf.db.name}"
    Flyway.configure()
      .dataSource(url, conf.db.username, conf.db.password)
      .baselineOnMigrate(true)
      .load()
  }

  bindSingleton {
    PgConnectOptions()
      .setHost(conf.db.host)
      .setDatabase(conf.db.name)
      .setPort(conf.db.port)
      .setUser(conf.db.username)
      .setPassword(conf.db.password)
  }

  bindSingleton {
    PgPool.client(vertx, instance<PgConnectOptions>(), PoolOptions().setMaxSize(conf.db.poolSize))
  }

  bindSingleton {
    PgPool.pool(vertx, instance<PgConnectOptions>(), PoolOptions())
  }

  bindSingleton<JobActivator> {
    object : JobActivator {
      override fun <T : Any> activateJob(type: Class<T>): T {
        return directDI.Instance(erased(type))
      }
    }
  }

  bindSingleton<StorageProvider> {
    SqlStorageProviderFactory.using(
      PGSimpleDataSource().apply {
        setURL("jdbc:postgresql://${conf.db.host}:${conf.db.port}/${conf.db.name}?currentSchema=job_runr")
        user = conf.db.username
        password = conf.db.password
      }
    )
  }

  bindSingleton {
    JobRunr.configure()
      .useStorageProvider(instance())
      .useDashboardIf(
        conf.backgroundTask.dashboardEnabled,
        JobRunrDashboardWebServerConfiguration
          .usingStandardDashboardConfiguration()
          .andBasicAuthentication(conf.backgroundTask.dashboardUserName, conf.backgroundTask.dashboardPassword)
      )
      .useJobActivator(instance())
      .useBackgroundJobServer(
        BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration()
          .andDeleteSucceededJobsAfter(Duration.ofHours(6))
          .andWorkerCount(1)
          .andPollIntervalInSeconds(10)
      )
      .initialize()
  }

  bindSingleton {
    instance<JobRunrConfiguration.JobRunrConfigurationResult>().jobScheduler
  }

  bindSingleton {
    instance<JobRunrConfiguration.JobRunrConfigurationResult>().jobRequestScheduler
  }

  bindSingleton<PersistentDealRepository> { PersistentDealRepository(sqlClient = instance()) }
  bindSingleton<DealRepository> { CachingDealRepository(instance<PersistentDealRepository>()) }
  bindSingleton<KeyValuesRepository> { PersistentKeyValuesRepository(sqlClient = instance()) }

  bindSingleton { RedditPostsScrapper(di) }
  bindSingleton { AppDetailScrapper(di) }
  bindSingleton { DealSummarizer(di) }
  bindSingleton {
    AndroidAppExpiryCheckScheduler(
      repository = instance(),
      requestScheduler = instance()
    )
  }
  bindSingleton {
    ForexFetcher(
      di = di,
      conf = instance()
    )
  }

  bindSingleton { DBHealthUseCase(di) }
  bindSingleton { GetDealsUseCase(di) }
  bindSingleton { NewDealUseCase(di) }
  bindSingleton { GetForexUseCase(di) }

  bindSingleton<FirebaseOptions> {
    FirebaseOptions.builder()
      .setCredentials(GoogleCredentials.fromStream(conf.firebaseAuthCredential.byteInputStream()))
      .build()
  }

  bindSingleton<FirebaseApp> {
    FirebaseApp.initializeApp(instance<FirebaseOptions>())
  }

  bindSingleton<FirebaseMessaging> {
    FirebaseMessaging.getInstance(instance<FirebaseApp>())
  }

  bindSingleton {
    MessagingService(firebaseMessaging = instance(), environment = conf.environment)
  }
}

private fun configureObjectMapper(): ObjectMapper {
  return DatabindCodec.mapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
  }
}
