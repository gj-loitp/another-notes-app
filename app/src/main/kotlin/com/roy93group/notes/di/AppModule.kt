

package com.roy93group.notes.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.roy93group.notes.model.DefaultJsonManager
import com.roy93group.notes.model.DefaultLabelsRepository
import com.roy93group.notes.model.DefaultNotesRepository
import com.roy93group.notes.model.JsonManager
import com.roy93group.notes.model.LabelsRepository
import com.roy93group.notes.model.NotesRepository
import com.roy93group.notes.model.ReminderAlarmCallback
import com.roy93group.notes.receiver.ReceiverAlarmCallback
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module(includes = [
    DatabaseModule::class,
    BuildTypeModule::class,
])
abstract class AppModule {

    @get:Binds
    abstract val DefaultNotesRepository.bindNotesRepository: NotesRepository

    @get:Binds
    abstract val DefaultLabelsRepository.bindLabelsRepository: LabelsRepository

    @get:Binds
    abstract val DefaultJsonManager.bindJsonManager: JsonManager

    @get:Binds
    abstract val ReceiverAlarmCallback.bindAlarmCallback: ReminderAlarmCallback

    companion object {
        @Provides
        fun providesSharedPreferences(context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        @get:Provides
        val json
            get() = Json {
                encodeDefaults = false
                ignoreUnknownKeys = true
            }
    }
}
