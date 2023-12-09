package com.roy93group.notes.ui

import androidx.annotation.Keep
import com.roy93group.notes.model.entity.Note
import com.roy93group.notes.model.entity.NoteStatus

/**
 * A class representing a change of status for one or many notes.
 * Status change from [oldStatus] to [newStatus].
 */
@Keep
data class StatusChange(
    val oldNotes: List<Note>,
    val oldStatus: NoteStatus,
    val newStatus: NoteStatus,
)
