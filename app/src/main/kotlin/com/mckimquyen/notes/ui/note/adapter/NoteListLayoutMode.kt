package com.mckimquyen.notes.ui.note.adapter

import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

/**
 * A note list layout mode.
 */
enum class NoteListLayoutMode(override val value: Int) : ValueEnum<Int> {
    LIST(0),
    GRID(1);

    companion object {
        fun fromValue(value: Int): NoteListLayoutMode = findValueEnum(value)
    }
}
