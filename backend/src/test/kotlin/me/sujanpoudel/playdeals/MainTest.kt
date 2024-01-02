package me.sujanpoudel.playdeals

import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import me.sujanpoudel.playdeals.common.BootstrapException
import me.sujanpoudel.playdeals.common.buildConf
import org.junit.jupiter.api.Test

class MainTest {
  @Test
  fun `Should return a proper conf with all values from env`() {
    val env = mutableMapOf(
      "ENV" to "DEVELOPMENT",
      "APP_PORT" to "123",
      "CORS" to "*.example.com",

      "DASHBOARD" to "true",
      "DASHBOARD_USER" to "user",
      "DASHBOARD_PASS" to "admin",

      "DB_HOST" to "localhost1",
      "DB_USERNAME" to "u",
      "DB_PASSWORD" to "p",
      "DB_POOL_SIZE" to "8",
      "DB_PORT" to "3333",
      "DB_NAME" to "db-name",

      "FIREBASE_ADMIN_AUTH_CREDENTIALS" to "dGVzdF9jcmVk",
      "FOREX_API_KEY" to "forex_key"
    )

    val conf = buildConf(env).unwrap()

    conf.environment shouldBe Environment.DEVELOPMENT
    conf.api.port shouldBe 123
    conf.api.cors shouldBe "*.example.com"

    conf.backgroundTask.dashboardEnabled shouldBe true
    conf.backgroundTask.dashboardUserName shouldBe "user"
    conf.backgroundTask.dashboardPassword shouldBe "admin"

    conf.db.name shouldBe "db-name"
    conf.db.host shouldBe "localhost1"
    conf.db.password shouldBe "p"
    conf.db.poolSize shouldBe 8
    conf.db.username shouldBe "u"
    conf.db.port shouldBe 3333

    conf.firebaseAuthCredential shouldBe "test_cred"
    conf.forexApiKey shouldBe "forex_key"
  }

  @Test
  fun `Should return a proper conf with some defaults being taken`() {
    val env = mutableMapOf(
      "DB_PORT" to "3333",
      "DB_HOST" to "localhost",
      "DB_USERNAME" to "u",
      "DB_PASSWORD" to "p",
      "FIREBASE_ADMIN_AUTH_CREDENTIALS" to "dGVzdF9jcmVk",
      "FOREX_API_KEY" to "forex_key"
    )

    val conf = buildConf(env).unwrap()

    conf.environment shouldBe Environment.PRODUCTION
    conf.api.port shouldBe 8888
    conf.api.cors shouldBe ".*."

    conf.backgroundTask.dashboardEnabled shouldBe true
    conf.backgroundTask.dashboardUserName shouldBe "admin"
    conf.backgroundTask.dashboardPassword shouldBe "admin"

    conf.db.name shouldBe "play_deals"
    conf.db.host shouldBe "localhost"
    conf.db.password shouldBe "p"
    conf.db.poolSize shouldBe 5
    conf.db.username shouldBe "u"
    conf.db.port shouldBe 3333

    conf.firebaseAuthCredential shouldBe "test_cred"
    conf.forexApiKey shouldBe "forex_key"
  }

  @Test
  fun `Should return all violations`() {
    val env = mutableMapOf(
      "APP_PORT" to "BAD_APP_PORT",
      "ENV" to "prod"
    )

    val violations = ((buildConf(env).unwrapError()) as BootstrapException).violations
    violations.shouldNotBeEmpty()
    violations shouldContainExactlyInAnyOrder listOf(
      "Invalid 'ENV'",
      "Invalid 'APP_PORT'",
      "No 'DB_HOST' env var defined!",
      "Invalid 'DB_HOST'",
      "No 'DB_USERNAME' env var defined!",
      "Invalid 'DB_USERNAME'",
      "No 'FIREBASE_ADMIN_AUTH_CREDENTIALS' env var defined!",
      "Invalid 'FIREBASE_ADMIN_AUTH_CREDENTIALS'",
      "No 'FOREX_API_KEY' env var defined!",
      "Invalid 'FOREX_API_KEY'"
    )
  }
}
