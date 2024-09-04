package com.mckimquyen.notes.di

import android.content.Context
import androidx.room.Room
import com.mckimquyen.notes.model.NotesDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(context: Context) = Room.databaseBuilder(
        context,
        NotesDb::class.java, "notes_db"
    )
        .addMigrations(*NotesDb.ALL_MIGRATIONS)
        .build()

    @Provides
    fun providesNotesDao(database: NotesDb) = database.notesDao()

    @Provides
    fun providesLabelsDao(database: NotesDb) = database.labelsDao()
}
