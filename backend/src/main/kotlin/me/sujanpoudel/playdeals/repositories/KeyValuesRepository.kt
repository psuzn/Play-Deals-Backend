package me.sujanpoudel.playdeals.repositories

interface KeyValuesRepository {
  suspend fun set(key: String, value: String): String

  suspend fun get(key: String): String?

  suspend fun delete(key: String)
}
