package ua.lab.notes.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ENGLISH("en"),
    UKRAINIAN("uk");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            val language = tag.substringBefore('-').substringBefore(',')
            return entries.firstOrNull { it.tag == language && it != SYSTEM } ?: SYSTEM
        }
    }
}

interface LanguageController {
    fun current(): AppLanguage

    fun apply(language: AppLanguage)
}

class AppCompatLanguageController : LanguageController {

    override fun current(): AppLanguage =
        AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags())

    override fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            if (language == AppLanguage.SYSTEM) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(language.tag),
        )
    }
}
