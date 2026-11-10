package ua.lab.notes.data

fun interface Clock {
    fun now(): Long

    companion object {
        val SYSTEM = Clock { System.currentTimeMillis() }
    }
}
