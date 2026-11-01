package com.starryweaver.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.starryweaver.notes.data.ImportExport
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.NoteSort
import com.starryweaver.notes.data.SettingsRepository
import com.starryweaver.notes.data.ThemeColor
import com.starryweaver.notes.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val repository: NoteRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
    val dynamicColor: StateFlow<Boolean> = settings.dynamicColor
    val themeColor: StateFlow<ThemeColor> = settings.themeColor
    val customThemeColor: StateFlow<Int> = settings.customThemeColor
    val sort: StateFlow<NoteSort> = settings.sort

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    fun setDynamicColor(enabled: Boolean) = settings.setDynamicColor(enabled)

    fun setThemeColor(color: ThemeColor) = settings.setThemeColor(color)

    fun setCustomThemeColor(argb: Int) = settings.setCustomThemeColor(argb)

    fun setSort(sort: NoteSort) = settings.setSort(sort)

    suspend fun exportJson(): String {
        repository.ensureLoaded()
        return ImportExport.exportJson(repository.notes.value)
    }

    suspend fun importJson(text: String): Int = repository.importMerge(ImportExport.importJson(text))

    companion object {
        fun factory(repository: NoteRepository, settings: SettingsRepository) = viewModelFactory {
            initializer { SettingsViewModel(repository, settings) }
        }
    }
}
