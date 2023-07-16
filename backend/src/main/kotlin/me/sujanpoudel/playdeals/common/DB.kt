package me.sujanpoudel.playdeals.common

import io.vertx.core.Future
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple

fun <T> PreparedQuery<T>.exec(vararg args: Any?): Future<T> {
  return this.execute(Tuple.from(args.toList()))
}

inline fun <reified T> Row.get(column: String): T = get(T::class.java, column)
