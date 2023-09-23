package me.sujanpoudel.playdeals.repositories

import me.sujanpoudel.playdeals.domain.entities.KeyValueEntity
import kotlin.reflect.KClass

interface KeyValuesRepository {
  suspend fun <T : Any> set(key: String, value: T, clazz: KClass<out T> = value::class): KeyValueEntity<T>

  suspend fun <T : Any> get(key: String, clazz: KClass<T>): T?

  suspend fun delete(key: String)
}

suspend inline fun <reified T : Any> KeyValuesRepository.set(key: String, value: T): KeyValueEntity<T> {
  return set(key, value, T::class)
}

suspend inline fun <reified T : Any> KeyValuesRepository.get(key: String) = get(key, T::class)
