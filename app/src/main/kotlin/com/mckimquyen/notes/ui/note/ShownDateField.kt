package com.mckimquyen.notes.ui.note

import com.mckimquyen.notes.R
import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

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
