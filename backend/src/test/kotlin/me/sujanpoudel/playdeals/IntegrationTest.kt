package me.sujanpoudel.playdeals

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.sqlclient.SqlClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.kodein.di.direct
import org.kodein.di.instance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

private const val DB_NAME = "play-deals"
private const val DB_USERNAME = "play-deals"
private const val DB_PASSWORD = "play-deals"

@Testcontainers
@ExtendWith(VertxExtension::class)
abstract class IntegrationTest(private val vertx: Vertx) {
  private lateinit var deploymentId: String

  private val log = KotlinLogging.logger {}
  protected val httpClient: WebClient by lazy { WebClient.create(vertx) }

  private val conf = Conf(
    app = Conf.App(8888),
    environment = Environment.TEST,
    cors = ".*.",
    db = Conf.DB(
      host = postgresqlContainer.host,
      port = postgresqlContainer.firstMappedPort,
      name = DB_NAME,
      username = DB_USERNAME,
      password = DB_PASSWORD,
      3
    ),
  )

  var di = DIConfigurer.configure(vertx, conf)

  protected fun runTest(block: suspend () -> Unit): Unit = runBlocking(vertx.dispatcher()) {
    block()
  }

  private fun deployVerticle(): String = runBlocking(vertx.dispatcher()) {
    vertx.deployVerticle(di.direct.instance<MainVerticle>()).await()
  }

  @BeforeEach
  fun assignDeploymentId() {
    deploymentId = deployVerticle()
    log.info { "Created deployment id $deploymentId" }
    runBlocking(vertx.dispatcher()) { cleanupDB() }
  }

  private suspend fun cleanupDB() {
    val sqlClient = di.get<SqlClient>()
    sqlClient
      .query(CLEAN_UP_DB_QUERY).execute()
      .onSuccess { log.info { "Successfully cleaned up dh" } }
      .onFailure { log.error(it) { "Could not cleanup db" } }
      .await()
  }

  @AfterEach
  fun undeployVerticle() = runBlocking(vertx.dispatcher()) {
    vertx.undeploy(deploymentId).await()
    log.info { "un-deployed deployment id $deploymentId" }
  }

  companion object {

    @JvmStatic
    protected val CLEAN_UP_DB_QUERY = """
      DELETE FROM "app_deal" where True;
      DELETE FROM "potential_deal" where True;
    """.trimIndent()

    @Container
    @JvmStatic
    val postgresqlContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>(
      DockerImageName.parse(System.getenv("POSTGRES_IMAGE") ?: "postgres:14")
        .asCompatibleSubstituteFor("postgres")
    )
      .apply {
        withDatabaseName(DB_NAME)
        withUsername(DB_USERNAME)
        withPassword(DB_PASSWORD)
      }
  }
}
