/*
 * Copyright 2020 Nicolas Maltais
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

package com.maltaisn.notes.ui.note

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.maltaisn.notes.App
import com.maltaisn.notes.R
import com.maltaisn.notes.model.entity.NoteStatus
import com.maltaisn.notes.ui.EventObserver
import com.maltaisn.notes.ui.MessageEvent
import com.maltaisn.notes.ui.SharedViewModel
import com.maltaisn.notes.ui.note.adapter.NoteAdapter
import com.maltaisn.notes.ui.note.adapter.NoteListLayoutMode
import kotlinx.serialization.json.Json
import java.text.NumberFormat
import javax.inject.Inject


abstract class NoteFragment : Fragment(), ActionMode.Callback {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val sharedViewModel: SharedViewModel by activityViewModels { viewModelFactory }
    protected abstract val viewModel: NoteViewModel

    @Inject lateinit var json: Json

    protected lateinit var toolbar: Toolbar
    protected lateinit var drawerLayout: DrawerLayout
    protected var actionMode: ActionMode? = null


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        (requireContext().applicationContext as App).appComponent.inject(this)
    }

    abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                       savedInstanceState: Bundle?): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        val activity = requireActivity()
        val navController = findNavController()

        // Setup toolbar with drawer
        toolbar = view.findViewById(R.id.toolbar)
        drawerLayout = activity.findViewById(R.id.drawer_layout)

        // Recycler view
        val rcv: RecyclerView = view.findViewById(R.id.rcv_notes)
        rcv.setHasFixedSize(true)
        val adapter = NoteAdapter(context, json, viewModel)
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        rcv.adapter = adapter
        rcv.layoutManager = layoutManager

        // Observers
        viewModel.noteItems.observe(viewLifecycleOwner, Observer { items ->
            adapter.submitList(items)
        })

        viewModel.listLayoutMode.observe(viewLifecycleOwner, Observer { mode ->
            layoutManager.spanCount =  when (mode!!) {
                NoteListLayoutMode.LIST -> 1
                NoteListLayoutMode.GRID -> 2
            }
            adapter.listLayoutMode = mode
        })

        viewModel.messageEvent.observe(viewLifecycleOwner, EventObserver { message ->
            sharedViewModel.onMessageEvent(message)
        })

        viewModel.currentSelection.observe(viewLifecycleOwner, Observer { selection ->
            if (selection.count != 0 && actionMode == null) {
                actionMode = toolbar.startActionMode(this)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)

            } else if (selection.count == 0 && actionMode != null) {
                actionMode?.finish()
                actionMode = null
            }

            actionMode?.let {
                it.title = NUMBER_FORMAT.format(selection.count)

                // Share and copy are only visible if there is a single note selected.
                val menu = it.menu
                val singleSelection = selection.count == 1
                menu.findItem(R.id.item_share).isVisible = singleSelection
                menu.findItem(R.id.item_copy).isVisible = singleSelection

                // Update move items depending on status
                val moveItem = menu.findItem(R.id.item_move)
                val deleteItem = menu.findItem(R.id.item_delete)
                when (selection.status!!) {
                    NoteStatus.ACTIVE -> {
                        moveItem.setIcon(R.drawable.ic_archive)
                        moveItem.setTitle(R.string.action_archive)
                        deleteItem.setTitle(R.string.action_delete)
                    }
                    NoteStatus.ARCHIVED -> {
                        moveItem.setIcon(R.drawable.ic_unarchive)
                        moveItem.setTitle(R.string.action_unarchive)
                        deleteItem.setTitle(R.string.action_delete)
                    }
                    NoteStatus.TRASHED -> {
                        moveItem.setIcon(R.drawable.ic_restore)
                        moveItem.setTitle(R.string.action_restore)
                        deleteItem.setTitle(R.string.action_delete_forever)
                    }
                }
            }
        })

        sharedViewModel.messageEvent.observe(viewLifecycleOwner, EventObserver { message ->
            when (message) {
                is MessageEvent.BlankNoteDiscardEvent -> {
                    Snackbar.make(view, R.string.message_blank_note_discarded, Snackbar.LENGTH_SHORT)
                            .show()
                }
                is MessageEvent.StatusChangeEvent -> {
                    val count = message.statusChange.oldNotes.size
                    Snackbar.make(view, context.resources.getQuantityString(
                            message.messageId, count, count), Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_undo) {
                                sharedViewModel.undoStatusChange()
                            }.show()
                }
            }
        })
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_move -> viewModel.moveSelectedNotes()
            R.id.item_select_all -> viewModel.selectAll()
            R.id.item_share -> Unit
            R.id.item_copy -> viewModel.copySelectedNote(
                    getString(R.string.copy_untitled_name), getString(R.string.copy_suffix))
            R.id.item_delete -> viewModel.deleteSelectedNotes()
            else -> return false
        }
        return true
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.selection_cab, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        viewModel.clearSelection()
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START)
    }

    companion object {
        private val NUMBER_FORMAT = NumberFormat.getInstance()
    }

}
