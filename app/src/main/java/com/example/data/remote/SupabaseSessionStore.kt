package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Persists Supabase auth tokens locally so sign-in survives app restarts. */
class SupabaseSessionStore(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences("famebook_session", Context.MODE_PRIVATE)

  fun save(accessToken: String?, refreshToken: String?) {
    prefs.edit {
      putString(KEY_ACCESS, accessToken)
      putString(KEY_REFRESH, refreshToken)
    }
  }

  fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)

  fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)

  fun clear() {
    prefs.edit { clear() }
  }

  companion object {
    private const val KEY_ACCESS = "supabase_access_token"
    private const val KEY_REFRESH = "supabase_refresh_token"
  }
}
