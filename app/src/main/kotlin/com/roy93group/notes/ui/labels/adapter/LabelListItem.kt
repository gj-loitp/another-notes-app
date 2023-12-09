package com.roy93group.notes.ui.labels.adapter

import androidx.annotation.Keep
import com.roy93group.notes.model.entity.Label

@Keep
data class LabelListItem(
    val id: Long,
    val label: Label,
    val checked: Boolean,
)
