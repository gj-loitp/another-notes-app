

package com.roy93group.notes.ui.home

/**
 * Used to provide built-type dependent behavior.
 */
interface BuildTypeBehavior {

    /**
     * Called by [HomeViewModel] to do something when the extra menu item is clicked.
     * This is only called in debug builds.
     */
    suspend fun doExtraAction(viewModel: HomeViewModel)
}
