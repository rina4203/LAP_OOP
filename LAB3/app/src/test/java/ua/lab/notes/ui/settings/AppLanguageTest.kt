package ua.lab.notes.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun recognisesLanguageTags() {
        assertEquals(AppLanguage.UKRAINIAN, AppLanguage.fromTag("uk"))
        assertEquals(AppLanguage.UKRAINIAN, AppLanguage.fromTag("uk-UA"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US,uk"))
    }

    @Test
    fun unknownOrEmptyTagMeansSystemLanguage() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("pl"))
    }
}
