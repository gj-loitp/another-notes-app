package com.roy93group.notes.ui.navigation

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.navigation.NavDirections
import com.roy93group.notes.model.entity.Label
import com.roy93group.notes.model.entity.NoteStatus
import com.roy93group.notes.ui.home.HomeFragment
import kotlinx.parcelize.Parcelize

/**
 * Different destinations accessible from the navigation drawer.
 */
sealed interface NavigationDestination {
    /**
     * Destination to navigate to another fragment in the navigation graph.
     */
    data class NavGraph(val directions: NavDirections) : NavigationDestination
}

/**
 * A destination accessible only by changing the content of the [HomeFragment].
 */
sealed interface HomeDestination : NavigationDestination, Parcelable {
    /**
     * Destination to view all notes of with a specific [status].
     */
    @Parcelize
    @Keep
    data class Status(val status: NoteStatus) : HomeDestination

    /**
     * Destination to view all notes with a [label].
     */
    @Parcelize
    @Keep
    data class Labels(val label: Label) : HomeDestination

    /**
     * Destination to view all notes with a reminder.
     */
    @Parcelize
    object Reminders : HomeDestination
}
