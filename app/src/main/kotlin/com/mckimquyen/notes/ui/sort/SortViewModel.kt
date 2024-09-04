package com.mckimquyen.notes.ui.sort

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mckimquyen.notes.model.PrefsManager
import com.mckimquyen.notes.model.SortDirection
import com.mckimquyen.notes.model.SortField
import com.mckimquyen.notes.ui.Event
import com.mckimquyen.notes.ui.send
import javax.inject.Inject

class SortViewModel @Inject constructor(
    private val prefs: PrefsManager,
) : ViewModel() {

    private val _sortField = MutableLiveData<Event<SortField>>()
    val sortField: LiveData<Event<SortField>>
        get() = _sortField

    private val _sortDirection = MutableLiveData<Event<SortDirection>>()
    val sortDirection: LiveData<Event<SortDirection>>
        get() = _sortDirection

    fun start() {
        _sortField.send(prefs.sortField)
        _sortDirection.send(prefs.sortDirection)
    }
}
