package ua.lab.notes.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import timber.log.Timber
import ua.lab.notes.NotesApplication
import ua.lab.notes.data.sync.SyncResult

class SyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as NotesApplication).container.notesRepository
        val result = repository.sync()
        Timber.d("Background sync: %s", result)
        return if (result is SyncResult.Failure) Result.retry() else Result.success()
    }
}
