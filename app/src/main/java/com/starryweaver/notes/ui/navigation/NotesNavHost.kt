package com.starryweaver.notes.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.SettingsRepository
import com.starryweaver.notes.ui.about.AboutScreen
import com.starryweaver.notes.ui.edit.NoteEditScreen
import com.starryweaver.notes.ui.edit.SharedNote
import com.starryweaver.notes.ui.notes.NoteListScreen
import com.starryweaver.notes.ui.settings.AppearanceScreen
import com.starryweaver.notes.ui.settings.SettingsScreen
import com.starryweaver.notes.ui.trash.TrashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object Routes {
    const val MAIN = "main"
    const val TRASH = "trash"
    const val ABOUT = "about"
    const val APPEARANCE = "appearance"
    const val EDIT = "edit/{id}?fresh={fresh}"
    const val NEW_EDIT = "edit/new"

    fun edit(id: String, fresh: Boolean = false) = "edit/$id?fresh=$fresh"
}

@Composable
fun NotesNavHost(
    navController: NavHostController,
    repository: NoteRepository,
    settings: SettingsRepository,
    saveScope: CoroutineScope,
    sharedNote: SharedNote?,
    onSharedConsumed: () -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.MAIN
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sharedNote) {
        if (sharedNote != null) {
            navController.navigate(Routes.NEW_EDIT)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(pagerState = pagerState, scope = scope)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MAIN,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { popEnter() },
            popExitTransition = { popExit() },
        ) {
            composable(Routes.MAIN) {
                MainTabs(
                    pagerState = pagerState,
                    repository = repository,
                    settings = settings,
                    onOpenNote = { id -> navController.navigate(Routes.edit(id)) },
                    onCreateNote = { id -> navController.navigate(Routes.edit(id, fresh = true)) },
                    onOpenTrash = { navController.navigate(Routes.TRASH) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenAppearance = { navController.navigate(Routes.APPEARANCE) },
                )
            }

            composable(
                route = Routes.EDIT,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("fresh") { type = NavType.BoolType; defaultValue = false },
                ),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: "new"
                val fresh = entry.arguments?.getBoolean("fresh") ?: false
                NoteEditScreen(
                    repository = repository,
                    saveScope = saveScope,
                    noteId = id,
                    seed = if (id == "new") sharedNote else null,
                    fresh = fresh,
                    onBack = { navController.popBackStack() },
                    onSharedConsumed = onSharedConsumed,
                )
            }

            composable(Routes.TRASH) {
                TrashScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.APPEARANCE) {
                AppearanceScreen(
                    repository = repository,
                    settings = settings,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private const val TRANSITION_MILLIS = 450

private fun AnimatedContentTransitionScope<*>.pushEnter(): EnterTransition =
    slideInHorizontally(animationSpec = tween(TRANSITION_MILLIS, easing = FastOutSlowInEasing)) { fullWidth -> fullWidth }

private fun AnimatedContentTransitionScope<*>.pushExit(): ExitTransition =
    slideOutHorizontally(animationSpec = tween(TRANSITION_MILLIS, easing = FastOutSlowInEasing)) { fullWidth -> -fullWidth / 4 }

private fun AnimatedContentTransitionScope<*>.popEnter(): EnterTransition =
    slideInHorizontally(animationSpec = tween(TRANSITION_MILLIS, easing = FastOutSlowInEasing)) { fullWidth -> -fullWidth / 4 }

private fun AnimatedContentTransitionScope<*>.popExit(): ExitTransition =
    slideOutHorizontally(animationSpec = tween(TRANSITION_MILLIS, easing = FastOutSlowInEasing)) { fullWidth -> fullWidth }

@Composable
private fun MainTabs(
    pagerState: PagerState,
    repository: NoteRepository,
    settings: SettingsRepository,
    onOpenNote: (String) -> Unit,
    onCreateNote: (String) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAppearance: () -> Unit,
) {
    HorizontalPager(state = pagerState, beyondViewportPageCount = 1) { page ->
        when (page) {
            0 -> NoteListScreen(
                repository = repository,
                settings = settings,
                onOpenNote = onOpenNote,
                onCreateNote = onCreateNote,
                onOpenTrash = onOpenTrash,
            )
            else -> SettingsScreen(
                repository = repository,
                settings = settings,
                onOpenTrash = onOpenTrash,
                onOpenAbout = onOpenAbout,
                onOpenAppearance = onOpenAppearance,
            )
        }
    }
}

@Composable
private fun MainBottomBar(pagerState: PagerState, scope: CoroutineScope) {
    NavigationBar {
        NavigationBarItem(
            selected = pagerState.currentPage == 0,
            onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("笔记") },
        )
        NavigationBarItem(
            selected = pagerState.currentPage == 1,
            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("设置") },
        )
    }
}
