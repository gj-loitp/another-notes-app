

package com.roy93group.notes.ui.note

import com.roy93group.notes.R
import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

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
