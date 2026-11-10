package ua.lab.notes.data.sync

sealed interface SyncResult {
    data class Success(val uploaded: Int, val downloaded: Int) : SyncResult
    data object NotSignedIn : SyncResult
    data object Unavailable : SyncResult
    data class Failure(val cause: Throwable) : SyncResult
}
