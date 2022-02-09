package com.ixigo.sdk

import android.content.Context
import android.content.SharedPreferences
import java.util.*

private const val uuidKey = "uuid"

class UUIDFactory(private val context: Context) {
  val uuid: UUID by lazy {
    synchronized(this) {
      val uuid = readFromPreferences()
      uuid ?: createAndSave()
    }
  }

  private fun createAndSave(): UUID {
    val uuid = UUID.randomUUID()
    val editor = prefs.edit()
    editor.putString(uuidKey, uuid.toString())
    editor.apply()
    return uuid
  }

  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences("uuidfactory.xml", Context.MODE_PRIVATE)
  }

  private fun readFromPreferences(): UUID? {
    val uuidStr: String? = prefs.getString(uuidKey, null)
    return uuidStr?.let { UUID.fromString(it) }
  }
}
