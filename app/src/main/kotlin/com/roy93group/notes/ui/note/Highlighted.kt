

package com.roy93group.notes.ui.note

/**
 * Text with a list of highlighted ranges.
 */
data class Highlighted(
    val content: String,
    val highlights: List<IntRange> = emptyList()
)
