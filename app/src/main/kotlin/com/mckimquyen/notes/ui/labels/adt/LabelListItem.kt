package com.mckimquyen.notes.ui.labels.adt

import androidx.annotation.Keep
import com.mckimquyen.notes.model.entity.Label

@Keep
data class LabelListItem(
    val id: Long,
    val label: Label,
    val checked: Boolean,
)
