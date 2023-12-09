

package com.roy93group.notes.ui.edit.adapter

import android.text.Editable
import android.text.format.DateUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.roy93group.notes.R
import com.roy93group.notes.databinding.VItemEditContentBinding
import com.roy93group.notes.databinding.VItemEditDateBinding
import com.roy93group.notes.databinding.VItemEditHeaderBinding
import com.roy93group.notes.databinding.VItemEditItemAddBinding
import com.roy93group.notes.databinding.VItemEditItemBinding
import com.roy93group.notes.databinding.VItemEditLabelsBinding
import com.roy93group.notes.databinding.VItemEditTitleBinding
import com.roy93group.notes.hideKeyboard
import com.roy93group.notes.model.PrefsManager
import com.roy93group.notes.model.entity.Label
import com.roy93group.notes.model.entity.Reminder
import com.roy93group.notes.showKeyboard
import com.roy93group.notes.strikethroughText
import com.roy93group.notes.ui.edit.BulletTextWatcher
import com.roy93group.notes.utils.RelativeDateFormatter
import java.text.DateFormat

/**
 * Interface implemented by any item that can have its focus position changed.
 */
sealed interface EditFocusableViewHolder {
    fun setFocus(pos: Int)
}

class EditDateViewHolder(binding: VItemEditDateBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val dateEdt = binding.dateEdt

    private val dateFormatter = RelativeDateFormatter(dateEdt.resources) { date ->
        DateUtils.formatDateTime(dateEdt.context, date, DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_ALL)
    }

    fun bind(item: EditDateItem) {
        dateEdt.text = dateFormatter.format(item.date, System.currentTimeMillis(),
            PrefsManager.MAXIMUM_RELATIVE_DATE_DAYS)
    }
}

class EditTitleViewHolder(binding: VItemEditTitleBinding, callback: EditAdapter.Callback) :
    RecyclerView.ViewHolder(binding.root), EditFocusableViewHolder {

    private val titleEdt = binding.titleEdt
    private var item: EditTitleItem? = null

    init {
        titleEdt.setOnClickListener {
            callback.onNoteClickedToEdit()
        }
        titleEdt.doAfterTextChanged { editable ->
            if (editable != item?.title?.text) {
                item?.title = AndroidEditableText(editable ?: return@doAfterTextChanged)
            }
        }
        titleEdt.setHorizontallyScrolling(false)
        titleEdt.maxLines = Integer.MAX_VALUE
    }

    fun bind(item: EditTitleItem) {
        this.item = item
        titleEdt.isFocusable = item.editable
        titleEdt.isFocusableInTouchMode = item.editable
        titleEdt.setText(item.title.text)
    }

    override fun setFocus(pos: Int) {
        titleEdt.requestFocus()
        titleEdt.setSelection(pos)
        titleEdt.showKeyboard()
    }
}

class EditContentViewHolder(binding: VItemEditContentBinding, callback: EditAdapter.Callback) :
    RecyclerView.ViewHolder(binding.root), EditFocusableViewHolder {

    private val contentEdt = binding.contentEdt
    private var item: EditContentItem? = null

    init {
        contentEdt.addTextChangedListener(BulletTextWatcher())
        contentEdt.doAfterTextChanged { editable ->
            if (editable != null && editable != item?.content?.text) {
                item?.content = AndroidEditableText(editable)
            }
        }

        contentEdt.setOnClickListener {
            callback.onNoteClickedToEdit()
        }
        contentEdt.onLinkClickListener = callback::onLinkClickedInNote
    }

    fun bind(item: EditContentItem) {
        this.item = item
        contentEdt.isFocusable = item.editable
        contentEdt.isFocusableInTouchMode = item.editable
        contentEdt.setText(item.content.text)
    }

    override fun setFocus(pos: Int) {
        contentEdt.requestFocus()
        contentEdt.setSelection(pos)
        contentEdt.showKeyboard()
    }
}

class EditItemViewHolder(binding: VItemEditItemBinding, callback: EditAdapter.Callback) :
    RecyclerView.ViewHolder(binding.root), EditFocusableViewHolder {

    val dragImv = binding.dragImv
    private val itemCheck = binding.itemChk
    private val itemEdt = binding.contentEdt
    private val deleteImv = binding.deleteImv

    private var item: EditItemItem? = null

    val isChecked: Boolean
        get() = itemCheck.isChecked

    init {
        itemCheck.setOnCheckedChangeListener { _, isChecked ->
            itemEdt.clearFocus()
            itemEdt.hideKeyboard()
            itemEdt.strikethroughText = isChecked && callback.strikethroughCheckedItems
            itemEdt.isActivated = !isChecked // Controls text color selector.
            dragImv.isInvisible = isChecked && callback.moveCheckedToBottom

            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                callback.onNoteItemCheckChanged(pos, isChecked)
            }
        }

        itemEdt.doOnTextChanged { _, _, _, count ->
            if (itemEdt.text != item?.content?.text) {
                item?.content = AndroidEditableText(itemEdt.text!!)
            }

            // This is used to detect when user enters line breaks into the input, so the
            // item can be split into multiple items. When user enters a single line break,
            // selection is set at the beginning of new item. On paste, i.e. when more than one
            // character is entered, selection is set at the end of last new item.
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                callback.onNoteItemChanged(pos, count > 1)
            }
        }
        itemEdt.setOnFocusChangeListener { _, hasFocus ->
            // Only show delete icon for currently focused item.
            deleteImv.isInvisible = !hasFocus
        }
        itemEdt.setOnKeyListener { _, _, event ->
            val isCursorAtStart =
                itemEdt.selectionStart == 0 && itemEdt.selectionStart == itemEdt.selectionEnd
            if (isCursorAtStart && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                // If user presses backspace at the start of an item, current item
                // will be merged with previous.
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    callback.onNoteItemBackspacePressed(pos)
                }
            }
            false
        }
        itemEdt.setOnClickListener {
            callback.onNoteClickedToEdit()
        }
        itemEdt.onLinkClickListener = callback::onLinkClickedInNote

        deleteImv.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                callback.onNoteItemDeleteClicked(pos)
            }
        }
    }

    fun bind(item: EditItemItem) {
        this.item = item

        itemEdt.isFocusable = item.editable
        itemEdt.isFocusableInTouchMode = item.editable
        itemEdt.setText(item.content.text)
        itemEdt.isActivated = !item.checked

        itemCheck.isChecked = item.checked
        itemCheck.isEnabled = item.editable
    }

    override fun setFocus(pos: Int) {
        itemEdt.requestFocus()
        itemEdt.setSelection(pos)
        itemEdt.showKeyboard()
    }

    fun clearFocus() {
        itemEdt.clearFocus()
    }
}

class EditItemAddViewHolder(binding: VItemEditItemAddBinding, callback: EditAdapter.Callback) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.setOnClickListener {
            callback.onNoteItemAddClicked(bindingAdapterPosition)
        }
    }
}

class EditHeaderViewHolder(binding: VItemEditHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val titleTxv = binding.titleTxv

    fun bind(item: EditCheckedHeaderItem) {
        titleTxv.text = titleTxv.context.resources.getQuantityString(
            R.plurals.edit_checked_items, item.count, item.count)
    }
}

class EditItemLabelsViewHolder(binding: VItemEditLabelsBinding, callback: EditAdapter.Callback) :
    RecyclerView.ViewHolder(binding.root) {

    private val chipGroup = binding.chipGroup
    private val labelClickListener = View.OnClickListener {
        callback.onNoteLabelClicked()
    }
    private val reminderClickListener = View.OnClickListener {
        callback.onNoteReminderClicked()
    }

    private val reminderDateFormatter = RelativeDateFormatter(itemView.resources) { date ->
        DateFormat.getDateInstance(DateFormat.SHORT).format(date)
    }

    fun bind(item: EditChipsItem) {
        val layoutInflater = LayoutInflater.from(chipGroup.context)
        chipGroup.removeAllViews()
        for (chip in item.chips) {
            when (chip) {
                is Label -> {
                    val view = layoutInflater.inflate(R.layout.v_edit_chip_label,
                        chipGroup,
                        false) as Chip
                    chipGroup.addView(view)
                    view.text = chip.name
                    view.setOnClickListener(labelClickListener)
                }

                is Reminder -> {
                    val view = layoutInflater.inflate(R.layout.v_edit_chip_reminder,
                        chipGroup,
                        false) as Chip
                    chipGroup.addView(view)
                    view.text = reminderDateFormatter.format(chip.next.time,
                        System.currentTimeMillis(), PrefsManager.MAXIMUM_RELATIVE_DATE_DAYS)
                    view.strikethroughText = chip.done
                    view.isActivated = !chip.done
                    view.setChipIconResource(if (chip.recurrence != null) R.drawable.ic_repeat else R.drawable.ic_alarm)
                    view.setOnClickListener(reminderClickListener)
                }

                else -> error("Unknown chip type")
            }
        }
    }
}

// Wrapper around Editable to allow transparent access to text content from ViewModel.
// Editable items have a EditableText field which is set by a text watcher added to the
// EditText and called when text is set when item is bound.
// Note that the Editable instance can change during the EditText lifetime.
private class AndroidEditableText(override val text: Editable) : EditableText {

    override fun append(text: CharSequence) {
        this.text.append(text)
    }

    override fun replaceAll(text: CharSequence) {
        this.text.replace(0, this.text.length, text)
    }
}
