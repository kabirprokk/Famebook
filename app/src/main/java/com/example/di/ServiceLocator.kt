package com.example.di

import com.example.data.repository.LocalAuthenticationRepository
import com.example.data.repository.LocalUserRepository
import android.content.Context
import com.example.data.remote.SupabaseHttpClient
import com.example.data.remote.SupabaseSession
import com.example.data.remote.SupabaseSessionStore
import com.example.BuildConfig
import com.example.data.repository.SupabaseBookingRepository
import com.example.data.repository.SupabaseCrewRepository
import com.example.data.repository.SupabaseMessageRepository
import com.example.data.repository.SupabaseNotificationRepository
import com.example.data.repository.SupabaseUserRepository
import com.example.domain.repository.AuthenticationRepository
import com.example.domain.repository.BookingRepository
import com.example.domain.repository.CrewRepository
import com.example.domain.repository.MessageRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.UserRepository

/**
 * ServiceLocator provides clean singleton repository instances.
 * In a future phase when connecting a remote cloud backend (REST / Supabase / Firebase),
 * these can be substituted with remote implementations without altering any domain or UI code.
 */
object ServiceLocator {
  private val supabaseSession = SupabaseSession()
  private var sessionStore: SupabaseSessionStore? = null
  private val supabaseApi by lazy { SupabaseHttpClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY) }

  fun init(context: Context) {
    if (sessionStore == null) {
      sessionStore = SupabaseSessionStore(context)
      supabaseSession.accessToken = sessionStore?.accessToken()
      supabaseSession.refreshToken = sessionStore?.refreshToken()
    }
  }
  private val _localUserRepository by lazy {
    LocalUserRepository()
  }

  val userRepository: UserRepository by lazy {
    SupabaseUserRepository(supabaseApi, supabaseSession, sessionStore)
  }

  val authenticationRepository: AuthenticationRepository by lazy {
    LocalAuthenticationRepository(_localUserRepository)
  }

  val crewRepository: CrewRepository by lazy {
    SupabaseCrewRepository(supabaseApi, supabaseSession)
  }

  val notificationRepository: NotificationRepository by lazy {
    SupabaseNotificationRepository(supabaseApi, supabaseSession)
  }

  val bookingRepository: BookingRepository by lazy {
    SupabaseBookingRepository(supabaseApi, supabaseSession)
  }

  val messageRepository: MessageRepository by lazy {
    SupabaseMessageRepository(supabaseApi, supabaseSession)
  }
}
