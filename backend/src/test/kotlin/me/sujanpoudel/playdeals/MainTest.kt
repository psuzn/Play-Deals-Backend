package me.sujanpoudel.playdeals

import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import me.sujanpoudel.playdeals.common.BootstrapException
import me.sujanpoudel.playdeals.common.buildConf
import org.junit.jupiter.api.Test

class MainTest {
  @Test
  fun `Should return a proper conf with all values from env`() {
    val env = mutableMapOf(
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

      "APP_PORT" to "123",

      "FIREBASE_ADMIN_AUTH_CREDENTIALS" to "dGVzdF9jcmVk",
      "ENV" to "DEVELOPMENT"
    )

    val conf = buildConf(env).unwrap()

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

    conf.api.port shouldBe 123

    conf.environment shouldBe Environment.DEVELOPMENT
  }

  @Test
  fun `Should fail on first critical incorrect val from env`() {
    val env = mutableMapOf(
      "ENV" to "prod"
    )

    val err = buildConf(env).unwrapError()
    err.printStackTrace()
    val violations = (err as BootstrapException).violations
    violations.shouldHaveSize(4) shouldContainExactlyInAnyOrder listOf(
      "Invalid ENV",
      "No DB_HOST env var defined!",
      "No DB_USERNAME env var defined!",
      "No FIREBASE_ADMIN_AUTH_CREDENTIALS env var defined!"
    )
  }

  @Test
  fun `Should return a proper conf with some defaults being taken`() {
    val env = mutableMapOf(
      "DB_HOST" to "localhost",
      "DB_USERNAME" to "u",
      "DB_PASSWORD" to "p",
      "DB_PORT" to "3333",
      "DB_NAME" to "db-name",
      "FIREBASE_ADMIN_AUTH_CREDENTIALS" to "dGVzdF9jcmVk"
    )

    val conf = buildConf(env).unwrap()
    conf.api.port shouldBe 8888
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
      "Invalid APP_PORT",
      "Invalid ENV",
      "No DB_HOST env var defined!",
      "No DB_USERNAME env var defined!",
      "No FIREBASE_ADMIN_AUTH_CREDENTIALS env var defined!"
    )
  }
}
