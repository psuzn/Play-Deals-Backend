package me.sujanpoudel.playdeals

import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MainTest {
  @Test
  fun `Should return a proper conf with all values from env`() {
    val env = mutableMapOf(
      "APP_PORT" to "123",
      "DB_HOST" to "localhost",
      "DB_USERNAME" to "u",
      "DB_PASSWORD" to "p",
      "APP_PORT" to "123",
      "DB_PORT" to "3333",
      "DB_NAME" to "db-name",
    )

    val conf = buildConf(env).unwrap()

    conf.api.port shouldBe 123
  }

  @Test
  fun `Should fail on first critical incorrect val from env`() {
    val env = mutableMapOf(
      "ENV" to "prod"
    )

    val err = buildConf(env).unwrapError()
    val violations = (err as BootstrapException).violations
    violations.shouldHaveSize(3) shouldContainExactlyInAnyOrder listOf(
      "Invalid ENV",
      "No DB_HOST env var defined!",
      "No DB_USERNAME env var defined!"
    )
  }

  @Test
  fun `Should return a proper conf with some defaults being taken`() {
    val env = mutableMapOf<String, String>(
      "DB_HOST" to "localhost",
      "DB_USERNAME" to "u",
      "DB_PASSWORD" to "p",
      "DB_PORT" to "3333",
      "DB_NAME" to "db-name",
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
      "No DB_USERNAME env var defined!"
    )
  }
}
