package me.sujanpoudel.playdeals

import java.util.regex.Pattern

object RegxPatterns {
  val playStorePackageName: Pattern = Pattern.compile("^([A-Za-z][A-Za-z\\d_]*\\.)*[A-Za-z][A-Za-z\\d_]*\$")
}
