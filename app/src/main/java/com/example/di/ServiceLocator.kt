package com.example.di

import com.example.data.repository.LocalAuthenticationRepository
import com.example.data.repository.LocalBookingRepository
import com.example.data.repository.LocalCrewRepository
import com.example.data.repository.LocalMessageRepository
import com.example.data.repository.LocalNotificationRepository
import com.example.data.repository.LocalUserRepository
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
  private val _localUserRepository by lazy {
    LocalUserRepository()
  }

  val userRepository: UserRepository by lazy {
    SupabaseUserRepository()
  }

  val authenticationRepository: AuthenticationRepository by lazy {
    LocalAuthenticationRepository(_localUserRepository)
  }

  val crewRepository: CrewRepository by lazy {
    LocalCrewRepository()
  }

  val notificationRepository: NotificationRepository by lazy {
    LocalNotificationRepository()
  }

  val bookingRepository: BookingRepository by lazy {
    LocalBookingRepository(
      crewRepository = crewRepository,
      notificationRepository = notificationRepository
    )
  }

  val messageRepository: MessageRepository by lazy {
    LocalMessageRepository()
  }
}
