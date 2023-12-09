package com.roy93group.notes.ui

import com.roy93group.notes.R
import com.roy93group.notes.model.ValueEnum
import com.roy93group.notes.model.findValueEnum

/**
 * Enum for different app themes.
 * [value] is from [R.array.pref_theme_values].
 */
enum class AppTheme(override val value: String) : ValueEnum<String> {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String): AppTheme = findValueEnum(value)
    }
}
