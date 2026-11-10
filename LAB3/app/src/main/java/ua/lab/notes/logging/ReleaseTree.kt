package ua.lab.notes.logging

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {

    public override fun isLoggable(tag: String?, priority: Int): Boolean =
        priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Log.println(priority, tag ?: DEFAULT_TAG, message)
    }

    private companion object {
        const val DEFAULT_TAG = "Notes"
    }
}
