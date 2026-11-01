package com.starryweaver.notes

import android.app.Application
import com.starryweaver.notes.data.NoteRepository
import com.starryweaver.notes.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NotesApplication : Application() {

    lateinit var noteRepository: NoteRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    /** 生命周期长于 ViewModel 的作用域，用于编辑器在退出时冲刷未保存的改动。 */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        noteRepository = NoteRepository(this)
        settingsRepository = SettingsRepository(this)
    }
}
