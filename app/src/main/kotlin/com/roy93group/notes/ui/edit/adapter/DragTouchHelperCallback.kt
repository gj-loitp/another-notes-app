package com.roy93group.notes.ui.edit.adapter

import android.content.Context
import android.graphics.Canvas
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.roy93group.notes.R

/**
 * Item touch helper callback for dragging items. Dragged item elevation is changed.
 * [onMove] is called when a list note item is moved.
 */
class DragTouchHelperCallback(
    context: Context,
    private val moveCheckedToBottom: Boolean,
    private val onMove: (from: Int, to: Int) -> Unit,
) : ItemTouchHelper.Callback() {

    private val dragElevation = context.resources.getDimensionPixelSize(
        R.dimen.edit_dragged_item_elevation
    ).toFloat()

    override fun isLongPressDragEnabled() = false
    override fun isItemViewSwipeEnabled() = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
        makeFlag(
            /* actionState = */ ItemTouchHelper.ACTION_STATE_DRAG,
            /* directions = */ ItemTouchHelper.UP or ItemTouchHelper.DOWN
        )

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return if (moveCheckedToBottom) {
            // Only unchecked items are moveable, and can't move into checked group.
            current is EditItemViewHolder && target is EditItemViewHolder &&
                    !current.isChecked && !target.isChecked
        } else {
            target is EditItemViewHolder
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        val view = viewHolder.itemView
        view.translationX = dX
        view.translationY = dY
        if (isCurrentlyActive) {
            ViewCompat.setElevation(/* view = */ view, /* elevation = */ dragElevation)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val view = viewHolder.itemView
        view.translationX = 0f
        view.translationY = 0f
        ViewCompat.setElevation(view, 0f)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        onMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
}
