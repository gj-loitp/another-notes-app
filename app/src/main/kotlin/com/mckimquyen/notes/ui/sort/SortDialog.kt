package com.mckimquyen.notes.ui.sort

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mckimquyen.notes.App
import com.mckimquyen.notes.R
import com.mckimquyen.notes.databinding.DlgSortBinding
import com.mckimquyen.notes.model.SortDirection
import com.mckimquyen.notes.model.SortField
import com.mckimquyen.notes.model.SortSettings
import com.mckimquyen.notes.ui.SharedViewModel
import com.mckimquyen.notes.ui.navGraphViewModel
import com.mckimquyen.notes.ui.observeEvent
import com.mckimquyen.notes.ui.viewModel
import debugCheck
import javax.inject.Inject
import javax.inject.Provider

class SortDialog : DialogFragment() {

    @Inject
    lateinit var sharedViewModelProvider: Provider<SharedViewModel>
    private val sharedViewModel by navGraphViewModel(R.id.nav_graph_main) {
        sharedViewModelProvider.get()
    }

    @Inject
    lateinit var viewModelProvider: Provider<SortViewModel>
    private val viewModel by viewModel {
        viewModelProvider.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireContext().applicationContext as App?)?.appComponent?.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val binding = DlgSortBinding.inflate(layoutInflater, null, false)

        // Create dialog
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle(R.string.sort_title)
            .setPositiveButton(R.string.action_ok) { _, _ ->
                val field = when (binding.sortFieldRadioGroup.checkedRadioButtonId) {
                    R.id.sortFieldAddedRadio -> SortField.ADDED_DATE
                    R.id.sortFieldModifiedRadio -> SortField.MODIFIED_DATE
                    R.id.sortFieldTitleRadio -> SortField.TITLE
                    else -> SortField.MODIFIED_DATE  // should not happen
                }
                val direction = when (binding.sortDirectionRadioGroup.checkedRadioButtonId) {
                    R.id.sortDirectionAscRadio -> SortDirection.ASCENDING
                    R.id.sortDirectionDescRadio -> SortDirection.DESCENDING
                    else -> SortDirection.DESCENDING  // should not happen
                }
                sharedViewModel.changeSortSettings(SortSettings(field, direction))
            }
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        setupViewModelObservers(binding)

        if (savedInstanceState == null) {
            viewModel.start()
        }

        return dialog
    }

    private fun setupViewModelObservers(binding: DlgSortBinding) {
        // Using `this` as lifecycle owner, cannot show dialog twice with same instance to avoid double observation.
        debugCheck(!viewModel.sortField.hasObservers()) { "Dialog was shown twice with same instance." }

        viewModel.sortField.observeEvent(this) { field ->
            when (field) {
                SortField.ADDED_DATE -> binding.sortFieldAddedRadio
                SortField.MODIFIED_DATE -> binding.sortFieldModifiedRadio
                SortField.TITLE -> binding.sortFieldTitleRadio
            }.isChecked = true
        }

        viewModel.sortDirection.observeEvent(this) { direction ->
            when (direction) {
                SortDirection.ASCENDING -> binding.sortDirectionAscRadio
                SortDirection.DESCENDING -> binding.sortDirectionDescRadio
            }.isChecked = true
        }
    }
}
