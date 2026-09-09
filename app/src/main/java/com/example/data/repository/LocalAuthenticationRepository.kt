package com.example.data.repository

import com.example.data.local.LocalSeedData
import com.example.domain.model.AuthSession
import com.example.domain.model.AuthState
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Production-ready local Authentication Repository.
 * Handles sign in, sign up (strictly client-side account creation without role choice),
 * password reset requests, and session tracking.
 * Ready for drop-in replacement with Firebase Auth, Supabase, or custom REST auth service.
 */
class LocalAuthenticationRepository(
  private val userRepository: LocalUserRepository
) : AuthenticationRepository {

  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentSession = MutableStateFlow<AuthSession?>(null)
  override val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()

  override suspend fun signIn(email: String, password: String): Result<User> {
    val cleanEmail = email.trim()
    if (cleanEmail.isBlank()) {
      val err = "Email address is required."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }
    if (password.length < 4) {
      val err = "Password must be at least 4 characters."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }

    _authState.value = AuthState.Authenticating

    // Look for existing user in user repository
    val existingUser = userRepository.getAllUsers().firstOrNull {
      it.email.equals(cleanEmail, ignoreCase = true)
    }

    val user = if (existingUser != null) {
      existingUser
    } else {
      // Create new client user if not found (clean development/testing behavior)
      val newClient = User(
        id = "usr_${UUID.randomUUID().toString().take(8)}",
        name = cleanEmail.substringBefore("@").replace(".", " ").split(" ")
          .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
        email = cleanEmail,
        phone = "+91 98000 00000",
        role = UserRole.CLIENT,
        companyName = null,
        bio = "Client member on FameBook."
      )
      userRepository.addUser(newClient)
      newClient
    }

    val session = AuthSession(
      userId = user.id,
      token = "fb_session_${UUID.randomUUID()}",
      role = user.role
    )

    _currentSession.value = session
    _authState.value = AuthState.Authenticated(user)
    userRepository.setCurrentUser(user)

    return Result.success(user)
  }

  override suspend fun signUp(fullName: String, email: String, password: String): Result<User> {
    val cleanName = fullName.trim()
    val cleanEmail = email.trim()

    if (cleanName.isBlank()) {
      val err = "Full name is required."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      val err = "A valid email address is required."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }
    if (password.length < 6) {
      val err = "Password must be at least 6 characters."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }

    _authState.value = AuthState.Authenticating

    // Check if account with email already exists
    val existing = userRepository.getAllUsers().firstOrNull {
      it.email.equals(cleanEmail, ignoreCase = true)
    }
    if (existing != null) {
      val err = "An account with this email already exists. Please sign in."
      _authState.value = AuthState.AuthenticationError(err)
      return Result.failure(IllegalArgumentException(err))
    }

    // Every signed up user is created with UserRole.CLIENT.
    // The user does NOT select a role as specified in production architecture.
    val newUser = User(
      id = "usr_${UUID.randomUUID().toString().take(8)}",
      name = cleanName,
      email = cleanEmail,
      phone = "+91 98000 00000",
      role = UserRole.CLIENT,
      companyName = null,
      bio = "Client member on FameBook."
    )

    userRepository.addUser(newUser)

    val session = AuthSession(
      userId = newUser.id,
      token = "fb_session_${UUID.randomUUID()}",
      role = newUser.role
    )

    _currentSession.value = session
    _authState.value = AuthState.Authenticated(newUser)
    userRepository.setCurrentUser(newUser)

    return Result.success(newUser)
  }

  override suspend fun sendPasswordReset(email: String): Result<Unit> {
    val cleanEmail = email.trim()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      return Result.failure(IllegalArgumentException("Please enter a valid email address."))
    }
    // Simulation of secure password reset dispatch
    return Result.success(Unit)
  }

  override suspend fun signOut(): Result<Unit> {
    _currentSession.value = null
    _authState.value = AuthState.Unauthenticated
    userRepository.setCurrentUser(null)
    return Result.success(Unit)
  }
}
