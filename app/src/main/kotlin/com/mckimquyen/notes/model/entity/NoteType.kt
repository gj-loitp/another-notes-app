package com.mckimquyen.notes.model.entity

import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

enum class NoteType(override val value: Int) : ValueEnum<Int> {
    TEXT(0),
    LIST(1);

    companion object {
        fun fromValue(value: Int): NoteType = findValueEnum(value)
    }
}
