package me.sujanpoudel.playdeals.domain.entities

data class KeyValueEntity<T>(val key: String, val value: T)

val KeyValueEntity<*>.insertValues
  get() = arrayOf(key, value)
