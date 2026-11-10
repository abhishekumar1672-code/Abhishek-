package com.procut.editor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.procut.editor.data.DefaultSettingsRepository
import com.procut.editor.data.InMemoryProjectRepository
import com.procut.editor.data.ProjectRepository
import com.procut.editor.data.SettingsRepository
import com.procut.editor.ui.editor.EditorScreen
import com.procut.editor.ui.home.HomeScreen
import com.procut.editor.ui.settings.SettingsScreen
import com.procut.editor.ui.viewmodels.EditorViewModel
import com.procut.editor.ui.viewmodels.HomeViewModel
import com.procut.editor.ui.viewmodels.SettingsViewModel

object ProCutDestinations {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val SETTINGS = "settings"
    const val ARG_PROJECT_ID = "projectId"

    fun editorRoute(projectId: String) = "$EDITOR/$projectId"
}

@Composable
fun ProCutNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    projectRepository: ProjectRepository = remember { InMemoryProjectRepository() },
    settingsRepository: SettingsRepository? = null
) {
    val context = LocalContext.current
    val resolvedSettingsRepository = settingsRepository ?: remember { DefaultSettingsRepository(context) }

    NavHost(
        navController = navController,
        startDestination = ProCutDestinations.HOME,
        modifier = modifier
    ) {
        composable(ProCutDestinations.HOME) {
            val homeViewModel = remember(projectRepository) {
                HomeViewModel(projectRepository)
            }
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToEditor = { projectId ->
                    navController.navigate(ProCutDestinations.editorRoute(projectId))
                },
                onNavigateToSettings = {
                    navController.navigate(ProCutDestinations.SETTINGS)
                }
            )
        }

        composable(
            route = "${ProCutDestinations.EDITOR}/{${ProCutDestinations.ARG_PROJECT_ID}}",
            arguments = listOf(
                navArgument(ProCutDestinations.ARG_PROJECT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString(ProCutDestinations.ARG_PROJECT_ID) ?: ""
            val editorViewModel = remember(projectId, projectRepository) {
                EditorViewModel(projectId, projectRepository)
            }
            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ProCutDestinations.SETTINGS) {
            val settingsViewModel = remember(resolvedSettingsRepository) {
                SettingsViewModel(resolvedSettingsRepository)
            }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
