package com.mckimquyen.notes.model.entity

import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

enum class NoteStatus(override val value: Int) : ValueEnum<Int> {
    ACTIVE(0),
    ARCHIVED(1),
    DELETED(2);

    companion object {
        fun fromValue(value: Int): NoteStatus = findValueEnum(value)
    }
}
