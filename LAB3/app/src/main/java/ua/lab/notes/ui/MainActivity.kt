package ua.lab.notes.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ua.lab.notes.ui.edit.NoteEditScreen
import ua.lab.notes.ui.edit.NoteEditViewModel.Companion.NOTE_ID_ARG
import ua.lab.notes.ui.list.NotesListScreen
import ua.lab.notes.ui.settings.SettingsScreen
import ua.lab.notes.ui.theme.NotesTheme

// AppCompatActivity потрібна для перемикання мови через AppCompatDelegate
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                NotesNavHost()
            }
        }
    }
}

private object Routes {
    const val NOTES = "notes"
    const val SETTINGS = "settings"
    const val EDIT = "edit?$NOTE_ID_ARG={$NOTE_ID_ARG}"

    fun edit(noteId: String?) = if (noteId == null) "edit" else "edit?$NOTE_ID_ARG=$noteId"
}

@Composable
private fun NotesNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.NOTES) {
        composable(Routes.NOTES) {
            NotesListScreen(
                onOpenNote = { navController.navigate(Routes.edit(it)) },
                onCreateNote = { navController.navigate(Routes.edit(null)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument(NOTE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            NoteEditScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
