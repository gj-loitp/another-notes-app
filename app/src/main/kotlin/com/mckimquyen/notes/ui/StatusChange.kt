package com.mckimquyen.notes.ui

import androidx.annotation.Keep
import com.mckimquyen.notes.model.entity.Note
import com.mckimquyen.notes.model.entity.NoteStatus

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
