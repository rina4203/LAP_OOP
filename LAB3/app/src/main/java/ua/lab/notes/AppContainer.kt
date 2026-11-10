package ua.lab.notes

import android.content.Context
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.data.auth.AuthService
import ua.lab.notes.data.auth.FirebaseAuthService
import ua.lab.notes.data.auth.UnavailableAuthService
import ua.lab.notes.data.local.NotesDatabase
import ua.lab.notes.data.remote.FirestoreNotesRemoteDataSource

class AppContainer(context: Context) {

    private val database = Room.databaseBuilder(
        context, NotesDatabase::class.java, "notes.db",
    ).build()

    private val firebaseConfigured = FirebaseApp.getApps(context).isNotEmpty()

    val authService: AuthService =
        if (firebaseConfigured) FirebaseAuthService(FirebaseAuth.getInstance())
        else UnavailableAuthService

    val notesRepository = NotesRepository(
        dao = database.noteDao(),
        remote = if (firebaseConfigured) {
            FirestoreNotesRemoteDataSource(FirebaseFirestore.getInstance())
        } else {
            null
        },
        auth = authService,
    )

    init {
        if (!firebaseConfigured) {
            Timber.w("google-services.json is missing, cloud sync is disabled")
        }
    }
}
