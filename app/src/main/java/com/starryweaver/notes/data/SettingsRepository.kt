package com.starryweaver.notes.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ThemeColor { INDIGO, BLUE, TEAL, GREEN, AMBER, VIOLET, CUSTOM }

/** 笔记列表布局：单栏 / 双栏。storageValue 为持久化取值。 */
enum class NotesLayout(val storageValue: String) {
    SINGLE("single"),
    DOUBLE("double"),
    ;

    companion object {
        fun fromStorage(raw: String?): NotesLayout =
            entries.firstOrNull { it.storageValue == raw } ?: SINGLE
    }
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notes_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readEnum(prefs.getString(KEY_THEME, null), ThemeMode.SYSTEM))
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _sort = MutableStateFlow(readEnum(prefs.getString(KEY_SORT, null), NoteSort.UPDATED))
    val sort: StateFlow<NoteSort> = _sort.asStateFlow()

    private val _themeColor = MutableStateFlow(readEnum(prefs.getString(KEY_COLOR, null), ThemeColor.INDIGO))
    val themeColor: StateFlow<ThemeColor> = _themeColor.asStateFlow()

    private val _customThemeColor = MutableStateFlow(prefs.getInt(KEY_CUSTOM_COLOR, DEFAULT_CUSTOM_COLOR))
    val customThemeColor: StateFlow<Int> = _customThemeColor.asStateFlow()

    private val _privacyAccepted = MutableStateFlow(prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false))
    val privacyAccepted: StateFlow<Boolean> = _privacyAccepted.asStateFlow()

    private val _notesLayout = MutableStateFlow(NotesLayout.fromStorage(prefs.getString(KEY_NOTES_LAYOUT, null)))
    val notesLayout: StateFlow<NotesLayout> = _notesLayout.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
        prefs.edit().putBoolean(KEY_DYNAMIC, enabled).apply()
    }

    fun setSort(sort: NoteSort) {
        _sort.value = sort
        prefs.edit().putString(KEY_SORT, sort.name).apply()
    }

    fun setThemeColor(color: ThemeColor) {
        _themeColor.value = color
        prefs.edit().putString(KEY_COLOR, color.name).apply()
    }

    fun setCustomThemeColor(argb: Int) {
        _customThemeColor.value = argb
        prefs.edit().putInt(KEY_CUSTOM_COLOR, argb).apply()
    }

    fun setPrivacyAccepted(accepted: Boolean) {
        _privacyAccepted.value = accepted
        prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, accepted).apply()
    }

    fun setNotesLayout(layout: NotesLayout) {
        _notesLayout.value = layout
        prefs.edit().putString(KEY_NOTES_LAYOUT, layout.storageValue).apply()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    private inline fun <reified T : Enum<T>> readEnum(raw: String?, fallback: T): T =
        raw?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback

    companion object {
        /** 自定义主题色的初始种子色（青绿）。 */
        const val DEFAULT_CUSTOM_COLOR = 0xFF00897B.toInt()

        private const val KEY_THEME = "theme_mode"
        private const val KEY_DYNAMIC = "dynamic_color"
        private const val KEY_SORT = "sort"
        private const val KEY_COLOR = "theme_color"
        private const val KEY_CUSTOM_COLOR = "theme_custom_color"
        private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
        private const val KEY_NOTES_LAYOUT = "notes_layout_mode"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
