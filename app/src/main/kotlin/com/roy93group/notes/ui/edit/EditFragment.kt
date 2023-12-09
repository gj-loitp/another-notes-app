/*
 * Copyright 2023 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.roy93group.notes.ui.edit

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.OneShotPreDrawListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.roy93group.notes.App
import com.roy93group.notes.NavGraphMainDirections
import com.roy93group.notes.R
import com.roy93group.notes.databinding.FEditBinding
import com.roy93group.notes.hideKeyboard
import com.roy93group.notes.model.entity.Note
import com.roy93group.notes.model.entity.NoteStatus
import com.roy93group.notes.model.entity.NoteType
import com.roy93group.notes.model.entity.PinnedStatus
import com.roy93group.notes.model.entity.Reminder
import com.roy93group.notes.navigateSafe
import com.roy93group.notes.showKeyboard
import com.roy93group.notes.ui.SharedViewModel
import com.roy93group.notes.ui.common.ConfirmDialog
import com.roy93group.notes.ui.edit.adapter.EditAdapter
import com.roy93group.notes.ui.navGraphViewModel
import com.roy93group.notes.ui.observeEvent
import com.roy93group.notes.ui.startSharingData
import com.roy93group.notes.ui.viewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Provider
import com.google.android.material.R as RMaterial

class EditFragment : Fragment(), Toolbar.OnMenuItemClickListener, ConfirmDialog.Callback {

    @Inject
    lateinit var viewModelFactory: EditViewModel.Factory
    val viewModel by viewModel { viewModelFactory.create(it) }

    @Inject
    lateinit var sharedViewModelProvider: Provider<SharedViewModel>
    private val sharedViewModel by navGraphViewModel(R.id.nav_graph_main) { sharedViewModelProvider.get() }

    private val args: EditFragmentArgs by navArgs()

    private var _binding: FEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true).apply {
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            duration = resources.getInteger(RMaterial.integer.material_motion_duration_long_1).toLong()
        }

        sharedElementReturnTransition = MaterialContainerTransform(requireContext(), false).apply {
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            duration = resources.getInteger(RMaterial.integer.material_motion_duration_long_1).toLong()
        }

        // Send an event via the sharedViewModel when the transition has finished playing
        (sharedElementReturnTransition as MaterialContainerTransform).addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                sharedViewModel.sharedElementTransitionFinished()
            }
        })

        super.onCreate(savedInstanceState)
        (requireContext().applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FEditBinding.inflate(inflater, container, false)
        val noteId = args.noteId
        ViewCompat.setTransitionName(
            binding.fragmentEditLayout,
            "noteContainer$noteId"
        )
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.saveNote()
            viewModel.exit()
        }

        viewModel.start(
            args.noteId,
            args.labelId,
            args.changeReminder,
            NoteType.fromValue(args.type),
            args.title,
            args.content
        )

        // Toolbar
        binding.toolbar.apply {
            setOnMenuItemClickListener(this@EditFragment)
            setNavigationOnClickListener {
                view.hideKeyboard()
                viewModel.saveNote()
                viewModel.exit()
            }
            setTitle(if (args.noteId == Note.NO_ID) {
                view.showKeyboard()
                R.string.edit_add_title
            } else {
                R.string.edit_change_title
            })
        }

        // Recycler view
        val rcv = binding.recyclerView
        rcv.setHasFixedSize(true)
        val adapter = EditAdapter(context, viewModel)
        val layoutManager = LinearLayoutManager(context)
        rcv.adapter = adapter
        rcv.layoutManager = layoutManager
        rcv.itemAnimator = object : DefaultItemAnimator() {
            override fun animateAppearance(
                viewHolder: RecyclerView.ViewHolder,
                preLayoutInfo: ItemHolderInfo?,
                postLayoutInfo: ItemHolderInfo
            ): Boolean {
                return if (preLayoutInfo != null && (preLayoutInfo.left != postLayoutInfo.left
                            || preLayoutInfo.top != postLayoutInfo.top)
                ) {
                    // item move, handle normally
                    super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo)
                } else {
                    // do not animate new item appearance
                    // this is mainly to avoid animating the whole list when fragment view is recreated.
                    dispatchAddFinished(viewHolder)
                    false
                }
            }
        }
        rcv.setOnTouchListener { _, event ->
            // Special case to dispatch touch events to underlaying background view to focus content view.
            // This is only done if content view is not taller than RecyclerView to avoid scrolling issues (#63).
            val contentEdt = rcv.findViewById<View>(R.id.contentEdt)
            if (contentEdt != null && contentEdt.height < rcv.height) {
                binding.viewBackground.dispatchTouchEvent(event)
            }
            false
        }
        binding.viewBackground.setOnClickListener {
            // On background click, focus note content if text note.
            viewModel.focusNoteContent()
        }

        // Dynamically adjust the padding on the bottom of the RecyclerView.
        // This enables edge-to-edge functionality and also handles resizing
        // when the keyboard is opened / closed.
        val initialPadding = resources.getDimensionPixelSize(R.dimen.edit_recyclerview_bottom_padding)
        ViewCompat.setOnApplyWindowInsetsListener(rcv) { _, insets ->
            val sysWindow = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            rcv.updatePadding(bottom = sysWindow.bottom + initialPadding)
            insets
        }

        setupViewModelObservers(adapter)

        // Delay the shared element transition until the recyclerView is ready to be drawn
        OneShotPreDrawListener.add(binding.recyclerView) {
            // Start shared element transition
            startPostponedEnterTransition()
        }
        postponeEnterTransition()
    }

    @SuppressLint("WrongConstant")
    private fun setupViewModelObservers(adapter: EditAdapter) {
        val navController = findNavController()

        // Each observer must take care not to undo the work of another observer
        // in case an attribute of a menu item is dependant on more than one criterion.
        viewModel.noteStatus.observe(viewLifecycleOwner, ::updateItemsForNoteStatus)
        viewModel.notePinned.observe(viewLifecycleOwner, ::updateItemsForPinnedStatus)
        viewModel.noteReminder.observe(viewLifecycleOwner, ::updateItemsForStatusAndReminder)
        viewModel.noteType.observe(viewLifecycleOwner, ::updateItemsForNoteType)
        viewModel.noteType.asFlow().combine(viewModel.noteStatus.asFlow()) { type, status -> status to type }
            .asLiveData().observe(viewLifecycleOwner, ::updateItemsForStatusAndType)

        viewModel.editItems.observe(viewLifecycleOwner, adapter::submitList)

        viewModel.focusEvent.observeEvent(viewLifecycleOwner, adapter::setItemFocus)

        viewModel.noteCreateEvent.observeEvent(viewLifecycleOwner) { noteId ->
            sharedViewModel.noteCreated(noteId)
        }

        val restoreNoteSnackbar by lazy {
            Snackbar.make(requireView(), R.string.edit_in_trash_message,
                CANT_EDIT_SNACKBAR_DURATION)
                .setGestureInsetBottomIgnored(true)
                .setAction(R.string.action_restore) { viewModel.restoreNoteAndEdit() }
        }
        viewModel.messageEvent.observeEvent(viewLifecycleOwner) { message ->
            when (message) {
                EditMessage.BLANK_NOTE_DISCARDED -> sharedViewModel.onBlankNoteDiscarded()
                EditMessage.RESTORED_NOTE -> Snackbar.make(requireView(), resources.getQuantityText(
                    R.plurals.edit_message_move_restore, 1), Snackbar.LENGTH_SHORT)
                    .setGestureInsetBottomIgnored(true)
                    .show()

                EditMessage.CANT_EDIT_IN_TRASH -> restoreNoteSnackbar.show()
            }
        }

        viewModel.statusChangeEvent.observeEvent(viewLifecycleOwner,
            sharedViewModel::onStatusChange)

        viewModel.shareEvent.observeEvent(viewLifecycleOwner, ::startSharingData)

        viewModel.showDeleteConfirmEvent.observeEvent(viewLifecycleOwner) {
            ConfirmDialog.newInstance(
                title = R.string.action_delete_forever,
                message = R.string.trash_delete_message,
                btnPositive = R.string.action_delete
            ).show(childFragmentManager, DELETE_CONFIRM_DIALOG_TAG)
        }

        viewModel.showRemoveCheckedConfirmEvent.observeEvent(viewLifecycleOwner) {
            ConfirmDialog.newInstance(
                title = R.string.edit_convert_keep_checked,
                btnPositive = R.string.action_delete,
                btnNegative = R.string.action_keep
            ).show(childFragmentManager, REMOVE_CHECKED_CONFIRM_DIALOG_TAG)
        }

        viewModel.showReminderDialogEvent.observeEvent(viewLifecycleOwner) { noteId ->
            navController.navigateSafe(NavGraphMainDirections.actionReminder(longArrayOf(noteId)))
        }

        viewModel.showLabelsFragmentEvent.observeEvent(viewLifecycleOwner) { noteId ->
            navController.navigateSafe(NavGraphMainDirections.actionLabel(longArrayOf(noteId)))
        }

        viewModel.showLinkDialogEvent.observeEvent(viewLifecycleOwner) { linkText ->
            ConfirmDialog.newInstance(
                btnPositive = R.string.action_open,
                btnNegative = R.string.action_cancel,
                messageStr = linkText,
            ).show(childFragmentManager, OPEN_LINK_DIALOG_TAG)
        }

        viewModel.openLinkEvent.observeEvent(viewLifecycleOwner) { url ->
            val uri = Uri.parse(url)
            val context = requireContext()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // do nothing
            }
        }

        sharedViewModel.reminderChangeEvent.observeEvent(viewLifecycleOwner) { reminder ->
            viewModel.onReminderChange(reminder)
        }

        viewModel.exitEvent.observeEvent(viewLifecycleOwner) {
            navController.popBackStack()
        }
    }

    private fun updateItemsForNoteStatus(status: NoteStatus) {
        val menu = binding.toolbar.menu

        val moveItem = menu.findItem(R.id.itemMove)
        when (status) {
            NoteStatus.ACTIVE -> {
                moveItem.setIcon(R.drawable.ic_archive)
                moveItem.setTitle(R.string.action_archive)
            }

            NoteStatus.ARCHIVED -> {
                moveItem.setIcon(R.drawable.ic_unarchive)
                moveItem.setTitle(R.string.action_unarchive)
            }

            NoteStatus.DELETED -> {
                moveItem.setIcon(R.drawable.ic_restore)
                moveItem.setTitle(R.string.action_restore)
            }
        }

        val isTrash = status == NoteStatus.DELETED
        menu.findItem(R.id.itemShare).isVisible = !isTrash
        menu.findItem(R.id.itemCopy).isVisible = !isTrash
        menu.findItem(R.id.itemReminder).isVisible = !isTrash
        menu.findItem(R.id.itemDelete).setTitle(if (isTrash) {
            R.string.action_delete_forever
        } else {
            R.string.action_delete
        })
    }

    private fun updateItemsForPinnedStatus(pinned: PinnedStatus) {
        val item = binding.toolbar.menu.findItem(R.id.itemPin)
        when (pinned) {
            PinnedStatus.PINNED -> {
                item.isVisible = true
                item.setTitle(R.string.action_unpin)
                item.setIcon(R.drawable.ic_pin_outline)
            }

            PinnedStatus.UNPINNED -> {
                item.isVisible = true
                item.setTitle(R.string.action_pin)
                item.setIcon(R.drawable.ic_pin)
            }

            PinnedStatus.CANT_PIN -> {
                item.isVisible = false
            }
        }
    }

    private fun updateItemsForStatusAndReminder(reminder: Reminder?) {
        binding.toolbar.menu.findItem(R.id.itemReminder).setTitle(if (reminder != null) {
            R.string.action_reminder_edit
        } else {
            R.string.action_reminder_add
        })
    }

    private fun updateItemsForNoteType(type: NoteType) {
        val menu = binding.toolbar.menu

        val typeItem = menu.findItem(R.id.itemType)
        when (type) {
            NoteType.TEXT -> {
                typeItem.setIcon(R.drawable.ic_checkbox)
                typeItem.setTitle(R.string.action_convert_to_list)
            }

            NoteType.LIST -> {
                typeItem.setIcon(R.drawable.ic_text)
                typeItem.setTitle(R.string.action_convert_to_text)
            }
        }
    }

    private fun updateItemsForStatusAndType(state: Pair<NoteStatus, NoteType>) {
        val menu = binding.toolbar.menu
        val isEditableList = state.first != NoteStatus.DELETED && state.second == NoteType.LIST
        menu.findItem(R.id.itemUncheckAll).isVisible = isEditableList
        menu.findItem(R.id.itemDeleteChecked).isVisible = isEditableList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveNote()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemType -> viewModel.toggleNoteType()
            R.id.itemMove -> viewModel.moveNoteAndExit()
            R.id.itemPin -> viewModel.togglePin()
            R.id.itemReminder -> viewModel.changeReminder()
            R.id.itemLabels -> viewModel.changeLabels()
            R.id.itemShare -> viewModel.shareNote()
            R.id.itemUncheckAll -> {
                viewModel.uncheckAllItems()
            }

            R.id.itemDeleteChecked -> viewModel.deleteCheckedItems()
            R.id.itemCopy -> viewModel.copyNote(getString(R.string.edit_copy_untitled_name),
                getString(R.string.edit_copy_suffix))

            R.id.itemDelete -> viewModel.deleteNote()
            else -> return false
        }
        return true
    }

    override fun onDialogPositiveButtonClicked(tag: String?) {
        when (tag) {
            DELETE_CONFIRM_DIALOG_TAG -> viewModel.deleteNoteForeverAndExit()
            REMOVE_CHECKED_CONFIRM_DIALOG_TAG -> viewModel.convertToText(false)
            OPEN_LINK_DIALOG_TAG -> viewModel.openClickedLink()
        }
    }

    override fun onDialogNegativeButtonClicked(tag: String?) {
        if (tag == REMOVE_CHECKED_CONFIRM_DIALOG_TAG) {
            viewModel.convertToText(true)
        }
    }

    companion object {
        private const val DELETE_CONFIRM_DIALOG_TAG = "delete_confirm_dialog"
        private const val REMOVE_CHECKED_CONFIRM_DIALOG_TAG = "remove_checked_confirm_dialog"
        private const val OPEN_LINK_DIALOG_TAG = "open_link_confirm_dialog"

        private const val CANT_EDIT_SNACKBAR_DURATION = 5000
    }
}
