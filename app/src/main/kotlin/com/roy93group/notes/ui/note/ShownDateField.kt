package com.roy93group.notes.ui.note

import com.roy93group.notes.R
import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

/**
 * Enum for different date fields shown for notes.
 * [value] is from [R.array.pref_shown_date_values].
 */
enum class ShownDateField(override val value: String) : ValueEnum<String> {
    ADDED("added"),
    MODIFIED("modified"),
    NONE("none");

    companion object {
        fun fromValue(value: String): ShownDateField = findValueEnum(value)
    }
}
