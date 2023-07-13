package me.sujanpoudel.playdeals.exceptions

open class ClientErrorException(message: String, val statusCode: Int = 400) : Exception(message) {
  class InvalidValueException(field: String) : ClientErrorException(message = "Invalid value for $field")
}
