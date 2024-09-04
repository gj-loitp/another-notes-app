package com.mckimquyen.notes.ui.note

import com.mckimquyen.notes.R
import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

/**
 * Enum for different swipe action.
 * [value] is from [R.array.pref_swipe_action_values].
 */
enum class SwipeAction(override val value: String) : ValueEnum<String> {
    ARCHIVE("archive"),
    DELETE("delete"),
    NONE("none");

    companion object {
        fun fromValue(value: String): SwipeAction = findValueEnum(value)
    }
}
