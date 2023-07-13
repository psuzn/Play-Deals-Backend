package me.sujanpoudel.playdeals.common

inline fun <reified T : Enum<T>> String.asEnum() = enumValueOf<T>(this)
inline fun <reified T : Enum<T>> String.asEnumOrNull() = try {
  asEnum<T>()
} catch (e: Exception) {
  null
}
