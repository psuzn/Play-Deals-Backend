package me.sujanpoudel.playdeals.domain.entities

import java.io.Serializable

data class KeyValueEntity<T : Serializable>(val key: String, val value: T)

val KeyValueEntity<*>.insertValues
  get() = arrayOf(key, value)
