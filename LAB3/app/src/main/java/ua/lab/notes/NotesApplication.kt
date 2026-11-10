package ua.lab.notes

import android.app.Application
import timber.log.Timber
import ua.lab.notes.logging.ReleaseTree
import ua.lab.notes.sync.SyncScheduler

class NotesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else ReleaseTree())
        container = AppContainer(this)
        SyncScheduler.schedulePeriodic(this)
        Timber.i("Application started")
    }
}
