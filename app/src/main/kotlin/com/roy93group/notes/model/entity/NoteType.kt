

package com.roy93group.notes.model.entity

import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

enum class NoteType(override val value: Int) : ValueEnum<Int> {
    TEXT(0),
    LIST(1);

    companion object {
        fun fromValue(value: Int): NoteType = findValueEnum(value)
    }
}
