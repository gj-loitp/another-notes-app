package com.mckimquyen.notes.di

import android.content.Context
import androidx.room.Room
import com.mckimquyen.notes.model.NotesDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(context: Context) = Room.databaseBuilder(
        context,
        NotesDatabase::class.java, "notes_db"
    )
        .addMigrations(*NotesDatabase.ALL_MIGRATIONS)
        .build()

    @Provides
    fun providesNotesDao(database: NotesDatabase) = database.notesDao()

    @Provides
    fun providesLabelsDao(database: NotesDatabase) = database.labelsDao()
}
