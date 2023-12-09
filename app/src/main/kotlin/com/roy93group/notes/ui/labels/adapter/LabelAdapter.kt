package com.roy93group.notes.ui.labels.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.roy93group.notes.databinding.VItemLabelBinding

class LabelAdapter(
    val context: Context,
    val callback: Callback,
) : ListAdapter<LabelListItem, LabelListViewHolder>(LabelListDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LabelListViewHolder(VItemLabelBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: LabelListViewHolder, position: Int) {
        holder.bind(item = getItem(position), adapter = this)
    }

    override fun getItemId(position: Int) = getItem(position).id

    interface Callback {
        val shouldHighlightCheckedItems: Boolean

        /** Called when a label [item] at [pos] is clicked. */
        fun onLabelItemClicked(item: LabelListItem, pos: Int)

        /** Called when a label [item] at [pos] is long-clicked. */
        fun onLabelItemLongClicked(item: LabelListItem, pos: Int)

        /** Called when the icon of a label [item] at [pos] is clicked. */
        fun onLabelItemIconClicked(item: LabelListItem, pos: Int)
    }
}
