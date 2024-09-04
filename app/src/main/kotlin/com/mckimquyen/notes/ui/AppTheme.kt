package com.mckimquyen.notes.ui

import com.mckimquyen.notes.R
import com.mckimquyen.notes.model.ValueEnum
import com.mckimquyen.notes.model.findValueEnum

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
