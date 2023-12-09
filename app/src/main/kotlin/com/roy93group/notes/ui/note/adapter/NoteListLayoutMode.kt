

package com.roy93group.notes.ui.note.adapter

import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

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
