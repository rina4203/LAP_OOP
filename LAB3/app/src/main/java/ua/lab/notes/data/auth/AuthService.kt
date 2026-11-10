package ua.lab.notes.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserAccount(val id: String, val email: String?)

enum class AuthError {
    INVALID_CREDENTIALS,
    INVALID_EMAIL,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    UNKNOWN,
}

class AuthException(val error: AuthError, cause: Throwable? = null) :
    Exception(error.name, cause)

interface AuthService {
    val isAvailable: Boolean
    val currentUser: StateFlow<UserAccount?>

    suspend fun signIn(email: String, password: String)

    suspend fun register(email: String, password: String)

    fun signOut()
}

object UnavailableAuthService : AuthService {
    override val isAvailable = false
    override val currentUser: StateFlow<UserAccount?> = MutableStateFlow(null)

    override suspend fun signIn(email: String, password: String) =
        throw AuthException(AuthError.UNKNOWN)

    override suspend fun register(email: String, password: String) =
        throw AuthException(AuthError.UNKNOWN)

    override fun signOut() = Unit
}
