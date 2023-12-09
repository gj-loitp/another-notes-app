package com.roy93group.notes.ui.note

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

/**
 * Icon and message shown in the placeholder view when note list is empty.
 */
@Keep
data class PlaceholderData(
    @DrawableRes val iconId: Int,
    @StringRes val messageId: Int,
)
