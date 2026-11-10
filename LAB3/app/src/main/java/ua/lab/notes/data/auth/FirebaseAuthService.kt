package ua.lab.notes.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseAuthService(private val auth: FirebaseAuth) : AuthService {

    private val user = MutableStateFlow(auth.currentUser?.toAccount())

    init {
        auth.addAuthStateListener { user.value = it.currentUser?.toAccount() }
    }

    override val isAvailable = true
    override val currentUser: StateFlow<UserAccount?> = user.asStateFlow()

    override suspend fun signIn(email: String, password: String) {
        call { auth.signInWithEmailAndPassword(email, password).await() }
        Timber.i("Signed in")
    }

    override suspend fun register(email: String, password: String) {
        call { auth.createUserWithEmailAndPassword(email, password).await() }
        Timber.i("Account created")
    }

    override fun signOut() {
        auth.signOut()
        Timber.i("Signed out")
    }

    private suspend fun <T> call(block: suspend () -> T): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Authentication failed")
            throw AuthException(e.toAuthError(), e)
        }
}

private fun FirebaseUser.toAccount() = UserAccount(uid, email)

private fun Exception.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == "ERROR_INVALID_EMAIL") AuthError.INVALID_EMAIL
        else AuthError.INVALID_CREDENTIALS
    is FirebaseNetworkException -> AuthError.NETWORK
    else -> AuthError.UNKNOWN
}
