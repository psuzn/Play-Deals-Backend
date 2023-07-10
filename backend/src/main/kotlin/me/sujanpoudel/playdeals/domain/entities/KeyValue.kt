package me.sujanpoudel.playdeals.domain.entities

import java.io.Serializable

data class KeyValue<T : Serializable>(val key: String, val value: T)

val KeyValue<*>.insertValues
  get() = arrayOf(key, value)
