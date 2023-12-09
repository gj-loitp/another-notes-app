

package com.roy93group.notes.ui.labels.adapter

import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.roy93group.notes.BuildConfig
import com.roy93group.notes.R
import com.roy93group.notes.databinding.VItemLabelBinding

class LabelListViewHolder(val binding: VItemLabelBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LabelListItem, adapter: LabelAdapter) {
        var name = item.label.name
        if (com.roy93group.notes.BuildConfig.ENABLE_DEBUG_FEATURES) {
            name += " (${item.label.id})"
        }

        binding.labelTxv.text = name
        binding.hiddenImv.isInvisible = !item.label.hidden

        val view = binding.root

        if (adapter.callback.shouldHighlightCheckedItems) {
            view.isActivated = item.checked
        } else {
            binding.labelImv.setImageResource(if (item.checked) {
                R.drawable.ic_label
            } else {
                R.drawable.ic_label_outline
            })
        }

        binding.labelImv.setOnClickListener {
            adapter.callback.onLabelItemIconClicked(item, bindingAdapterPosition)
        }
        view.setOnClickListener {
            adapter.callback.onLabelItemClicked(item, bindingAdapterPosition)
        }
        view.setOnLongClickListener {
            adapter.callback.onLabelItemLongClicked(item, bindingAdapterPosition)
            true
        }
    }
}
