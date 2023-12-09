

package com.roy93group.notes.ui.labels.adapter

import com.roy93group.notes.model.entity.Label

data class LabelListItem(
    val id: Long,
    val label: Label,
    val checked: Boolean
)
