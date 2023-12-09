

package com.roy93group.notes.model.entity

import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

enum class NoteStatus(override val value: Int) : ValueEnum<Int> {
    ACTIVE(0),
    ARCHIVED(1),
    DELETED(2);

    companion object {
        fun fromValue(value: Int): NoteStatus = findValueEnum(value)
    }
}
