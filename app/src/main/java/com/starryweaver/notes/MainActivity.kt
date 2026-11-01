package com.starryweaver.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.starryweaver.notes.ui.edit.SharedNote
import com.starryweaver.notes.ui.navigation.NotesNavHost
import com.starryweaver.notes.ui.onboarding.OnboardingScreen
import com.starryweaver.notes.ui.privacy.PrivacyPolicyGateDialog
import com.starryweaver.notes.ui.theme.LocalThemeReveal
import com.starryweaver.notes.ui.theme.RevealTheme
import com.starryweaver.notes.ui.theme.ThemeRevealState

class MainActivity : ComponentActivity() {

    private var sharedNote by mutableStateOf<SharedNote?>(null)
    private val revealState = ThemeRevealState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedNote = extractSharedNote(intent)

        val app = application as NotesApplication
        setContent {
            val themeMode by app.settingsRepository.themeMode.collectAsState()
            val dynamicColor by app.settingsRepository.dynamicColor.collectAsState()
            val themeColor by app.settingsRepository.themeColor.collectAsState()
            val customColor by app.settingsRepository.customThemeColor.collectAsState()
            val privacyAccepted by app.settingsRepository.privacyAccepted.collectAsState()
            val onboardingCompleted by app.settingsRepository.onboardingCompleted.collectAsState()

            CompositionLocalProvider(LocalThemeReveal provides revealState) {
                val navController = rememberNavController()
                val mainContent: @Composable () -> Unit = {
                    NotesNavHost(
                        navController = navController,
                        repository = app.noteRepository,
                        settings = app.settingsRepository,
                        saveScope = app.applicationScope,
                        sharedNote = sharedNote,
                        onSharedConsumed = { sharedNote = null },
                    )
                }

                RevealTheme(
                    themeMode = themeMode,
                    dynamicColor = dynamicColor,
                    themeColor = themeColor,
                    customColor = Color(customColor),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        when {
                            !privacyAccepted -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    mainContent()
                                    PrivacyPolicyGateDialog(
                                        onAgree = { app.settingsRepository.setPrivacyAccepted(true) },
                                    )
                                }
                            }

                            !onboardingCompleted -> {
                                OnboardingScreen(
                                    onFinish = { app.settingsRepository.setOnboardingCompleted(true) },
                                )
                            }

                            else -> mainContent()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractSharedNote(intent)?.let { sharedNote = it }
    }

    private fun extractSharedNote(intent: Intent?): SharedNote? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_PROCESS_TEXT) return null

        val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
            ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            ?: return null
        if (text.isBlank()) return null

        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty().trim()
        return SharedNote(title = subject, text = text)
    }
}
