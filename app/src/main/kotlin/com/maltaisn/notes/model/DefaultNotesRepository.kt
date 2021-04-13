/*
 * Copyright 2021 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.notes.model

import com.maltaisn.notes.model.entity.Label
import com.maltaisn.notes.model.entity.LabelRef
import com.maltaisn.notes.model.entity.Note
import com.maltaisn.notes.model.entity.NoteStatus
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import java.util.Date
import javax.inject.Inject

class DefaultNotesRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val labelsDao: LabelsDao,
    private val json: Json
) : NotesRepository {

    // Data modification methods are wrapped in non-cancellable context
    // so that calling them in onPause for example won't cancel the transaction on the
    // subsequent onDestroy call, which cancels the coroutine scope.

    override suspend fun insertNote(note: Note): Long = withContext(NonCancellable) {
        notesDao.insert(note)
    }

    override suspend fun updateNote(note: Note) = withContext(NonCancellable) {
        notesDao.update(note)
    }

    override suspend fun updateNotes(notes: List<Note>) = withContext(NonCancellable) {
        notesDao.updateAll(notes)
    }

    override suspend fun deleteNote(note: Note) = withContext(NonCancellable) {
        notesDao.delete(note)
    }

    override suspend fun deleteNotes(notes: List<Note>) = withContext(NonCancellable) {
        notesDao.deleteAll(notes)
    }

    override suspend fun getNoteById(id: Long) = notesDao.getById(id)


    override suspend fun insertLabel(label: Label) = withContext(NonCancellable) {
        labelsDao.insert(label)
    }

    override suspend fun updateLabel(label: Label) = withContext(NonCancellable) {
        labelsDao.update(label)
    }

    override suspend fun deleteLabel(label: Label) {
        labelsDao.delete(label)
    }

    override suspend fun getLabelById(id: Long) = labelsDao.getById(id)

    override suspend fun getLabelByName(name: String) = labelsDao.getLabelByName(name)

    override suspend fun insertLabelRefs(refs: List<LabelRef>) = labelsDao.insertRefs(refs)

    override suspend fun deleteLabelRefs(refs: List<LabelRef>) = labelsDao.removeRefs(refs)

    override fun getNotesByStatus(status: NoteStatus) = notesDao.getByStatus(status)

    override fun getNotesWithReminder() = notesDao.getAllWithReminder()

    override fun getAllLabels() = labelsDao.getAll()

    override fun searchNotes(query: String) = notesDao.search(query)

    override suspend fun emptyTrash() {
        deleteNotes(getNotesByStatus(NoteStatus.DELETED).first())
    }

    override suspend fun deleteOldNotesInTrash() {
        val delay = PrefsManager.TRASH_AUTO_DELETE_DELAY.toLongMilliseconds()
        val minDate = Date(System.currentTimeMillis() - delay)
        deleteNotes(notesDao.getByStatusAndDate(NoteStatus.DELETED, minDate))
    }

    override suspend fun getJsonData(): String {
        val notes = notesDao.getAll()
        val notesJson = buildJsonObject {
            for (note in notes) {
                put(note.id.toString(), json.encodeToJsonElement(Note.serializer(), note))
            }
        }
        return json.encodeToString(JsonObject.serializer(), notesJson)
    }

    override suspend fun clearAllData() {
        notesDao.clear()
    }
}
