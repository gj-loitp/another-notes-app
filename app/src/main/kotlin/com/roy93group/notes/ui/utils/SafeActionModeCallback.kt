

package com.roy93group.notes.ui.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View

/**
 * Can't remove action mode callback after adding it, and setting the action mode to `null`
 * doesn't allow it to be garbage collected for some reason.
 * This class prevents the memory leak.
 * See [https://stackoverflow.com/questions/53261839/trying-to-detect-actionmode-memory-leak].
 */
private class SafeActionModeCallback(
    private var callback: ActionMode.Callback?
) : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        return callback!!.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return callback!!.onPrepareActionMode(mode, menu)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return callback!!.onActionItemClicked(mode, item)
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        callback!!.onDestroyActionMode(mode)
        callback = null
    }
}

fun View.startSafeActionMode(callback: ActionMode.Callback): ActionMode {
    return this.startActionMode(SafeActionModeCallback(callback))
}
