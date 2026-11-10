package ua.lab.notes.logging

import android.util.Log
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseTreeTest {

    private val tree = ReleaseTree()

    @Test
    fun releaseBuildsLogOnlyWarningsAndErrors() {
        assertFalse(tree.isLoggable("Notes", Log.DEBUG))
        assertFalse(tree.isLoggable("Notes", Log.INFO))
        assertTrue(tree.isLoggable("Notes", Log.WARN))
        assertTrue(tree.isLoggable("Notes", Log.ERROR))
    }
}
