package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.SupabaseHttpClient
import com.example.data.remote.SupabaseSession
import android.net.Uri
import com.example.domain.model.AuthState
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SupabaseUserRepository(
  private val api: SupabaseHttpClient = SupabaseHttpClient(
    BuildConfig.SUPABASE_URL,
    BuildConfig.SUPABASE_PUBLISHABLE_KEY
  ),
  private val session: SupabaseSession = SupabaseSession()
) : UserRepository {
  private val _currentUser = MutableStateFlow<User?>(null)
  override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()
  private val accessToken: String?
    get() = session.accessToken

  override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
    _authState.value = AuthState.Authenticating
    val payload = JSONObject().put("email", email.trim()).put("password", password)
    requestUser("POST", "auth/v1/token?grant_type=password", payload.toString(), email)
  }

  override suspend fun register(
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    companyOrSpecialty: String?,
    password: String
  ): Result<User> = withContext(Dispatchers.IO) {
    if (role != UserRole.CLIENT) return@withContext failure("New accounts are created as clients.")
    val payload = JSONObject()
      .put("email", email.trim())
      .put("password", password)
      .put("data", JSONObject().put("full_name", name.trim()))
    runCatching {
      val redirect = Uri.encode("famebook://auth/callback")
      api.request("POST", "auth/v1/signup?redirect_to=$redirect", payload.toString()).use { response ->
        if (!response.isSuccessful) error(response.errorMessage())
        val json = JSONObject(response.body?.string().orEmpty())
        val session = json.optJSONObject("session")
        val user = json.optJSONObject("user") ?: json
        val token = session?.optString("access_token").orEmpty()
        if (token.isBlank()) error("Account created. Confirm your email before signing in.")
        this@SupabaseUserRepository.session.accessToken = token
        loadProfile(user.getString("id"), name, email, phone)
      }
    }.fold({ user -> setAuthenticated(user) }, { failure(it.message ?: "Account creation failed.") })
  }

  override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching { accessToken?.let { api.request("POST", "auth/v1/logout", accessToken = it).use { } } }
      .fold({
        session.accessToken = null
        session.currentUser = null
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        Result.success(Unit)
      }, { failure(it.message ?: "Sign out failed.") })
  }

  override suspend fun updateProfile(name: String, email: String, phone: String, bio: String, company: String?): Result<User> = withContext(Dispatchers.IO) {
    val current = _currentUser.value ?: return@withContext failure("No active session.")
    val payload = JSONObject().put("full_name", name.trim()).put("email", email.trim())
      .put("phone", phone.trim()).put("bio", bio.trim()).put("company_name", company)
    runCatching {
      api.request("PATCH", "rest/v1/profiles?id=eq.${current.id}", payload.toString(), accessToken, "return=representation").use { response ->
        if (!response.isSuccessful) error(response.errorMessage())
        val rows = JSONArray(response.body?.string().orEmpty())
        if (rows.length() == 0) error("Profile was not returned by Supabase.")
        profileFromJson(rows.getJSONObject(0))
      }
    }.fold({ setAuthenticated(it) }, { failure(it.message ?: "Profile update failed.") })
  }

  override suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
    runCatching {
      api.request("GET", "rest/v1/profiles?id=eq.$userId&select=*", accessToken = accessToken).use { response ->
        if (!response.isSuccessful) return@runCatching null
        val rows = JSONArray(response.body?.string().orEmpty())
        if (rows.length() == 0) null else profileFromJson(rows.getJSONObject(0))
      }
    }.getOrNull()
  }

  override suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
    runCatching {
      api.request("GET", "rest/v1/profiles?select=*&order=created_at.desc&limit=100", accessToken = accessToken).use { response ->
        if (!response.isSuccessful) return@runCatching emptyList()
        val rows = JSONArray(response.body?.string().orEmpty())
        List(rows.length()) { profileFromJson(rows.getJSONObject(it)) }
      }
    }.getOrDefault(emptyList())
  }

  override suspend fun updateUserRole(userId: String, role: UserRole): Result<User> = withContext(Dispatchers.IO) {
    runCatching {
      val payload = JSONObject().put("p_user_id", userId).put("p_role", role.name)
      api.request("POST", "rest/v1/rpc/assign_user_role", payload.toString(), accessToken).use { response ->
        if (!response.isSuccessful) error(response.errorMessage())
        val body = response.body?.string().orEmpty().trim()
        val json = if (body.startsWith("[")) JSONArray(body).getJSONObject(0) else JSONObject(body)
        profileFromJson(json)
      }
    }.fold(
      { updated ->
        if (_currentUser.value?.id == updated.id) setAuthenticated(updated) else Result.success(updated)
      },
      { Result.failure(it) }
    )
  }

  private fun requestUser(method: String, path: String, body: String, email: String): Result<User> = runCatching {
    api.request(method, path, body).use { response ->
      if (!response.isSuccessful) error(response.errorMessage())
      val json = JSONObject(response.body?.string().orEmpty())
      session.accessToken = json.getString("access_token")
      val authUser = json.getJSONObject("user")
      val metadata = authUser.optJSONObject("user_metadata")
      loadProfile(authUser.getString("id"), metadata?.optString("full_name").orEmpty(), email, "")
    }
  }.fold({ setAuthenticated(it) }, { failure(it.message ?: "Sign in failed.") })

  private fun loadProfile(id: String, fallbackName: String, email: String, phone: String): User {
    api.request("GET", "rest/v1/profiles?id=eq.$id&select=*", accessToken = accessToken).use { response ->
      if (!response.isSuccessful) error(response.errorMessage())
      val rows = JSONArray(response.body?.string().orEmpty())
      return if (rows.length() == 0) User(id, fallbackName.ifBlank { email.substringBefore('@') }, email, phone, UserRole.CLIENT)
      else profileFromJson(rows.getJSONObject(0))
    }
  }

  private fun profileFromJson(json: JSONObject) = User(
    id = json.getString("id"), name = json.optString("full_name", "FameBook User"),
    email = json.optString("email"), phone = json.optString("phone"),
    role = runCatching { UserRole.valueOf(json.optString("role", "CLIENT")) }.getOrDefault(UserRole.CLIENT),
    avatarUrl = json.optString("avatar_url").takeIf { it.isNotBlank() },
    companyName = json.optString("company_name").takeIf { it.isNotBlank() }, bio = json.optString("bio")
  )

  private fun setAuthenticated(user: User): Result<User> {
    session.currentUser = user
    _currentUser.value = user
    _authState.value = AuthState.Authenticated(user)
    return Result.success(user)
  }

  private fun failure(message: String): Result<Nothing> {
    _authState.value = AuthState.AuthenticationError(message)
    return Result.failure(IllegalArgumentException(message))
  }

  private fun okhttp3.Response.errorMessage(): String {
    val bodyText = body?.string().orEmpty()
    return runCatching {
      val json = JSONObject(bodyText)
      json.optString("msg").ifBlank { json.optString("message") }
    }.getOrDefault(bodyText).ifBlank { "Supabase request failed ($code)." }
  }
}
