

package com.roy93group.notes.model.entity

import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

/**
 * Describes how a note or a group of notes are pinned.
 */
enum class PinnedStatus(override val value: Int) : ValueEnum<Int> {
    CANT_PIN(0),
    UNPINNED(1),
    PINNED(2);

    companion object {
        fun fromValue(value: Int): PinnedStatus = findValueEnum(value)
    }
}
