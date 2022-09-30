/*
 * Copyright 2022 Nicolas Maltais
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

package com.maltaisn.notes.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.color.DynamicColors
//import com.github.venom.Venom
import com.maltaisn.notes.App
import com.maltaisn.notes.TAG
import com.maltaisn.notes.model.PrefsManager
import com.maltaisn.notes.model.converter.NoteTypeConverter
import com.maltaisn.notes.model.entity.Note
import com.maltaisn.notes.model.entity.NoteStatus
import com.maltaisn.notes.model.entity.NoteType
import com.maltaisn.notes.navigateSafe
import com.maltaisn.notes.receiver.AlarmReceiver
import com.maltaisn.notes.sync.NavGraphMainDirections
import com.maltaisn.notes.sync.R
import com.maltaisn.notes.sync.databinding.ActivityMainBinding
import com.maltaisn.notes.ui.*
import com.maltaisn.notes.ui.navigation.HomeDestination
import javax.inject.Inject
import javax.inject.Provider

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var sharedViewModelProvider: Provider<SharedViewModel>
    private val sharedViewModel by navGraphViewModel(R.id.nav_graph_main) { sharedViewModelProvider.get() }

    @Inject
    lateinit var viewModelFactory: MainViewModel.Factory
    private val viewModel by viewModel { viewModelFactory.create(it) }

    @Inject
    lateinit var prefs: PrefsManager

    lateinit var drawerLayout: DrawerLayout

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_DayNight)

        super.onCreate(savedInstanceState)
        (applicationContext as App).appComponent.inject(this)

        // Apply dynamic colors
        if (prefs.dynamicColors)
            DynamicColors.applyToActivityIfAvailable(this)

        // For triggering process death during debug
//        val venom = Venom.createInstance(this)
//        venom.initialize()
//        venom.start()

        binding = ActivityMainBinding.inflate(layoutInflater)
        drawerLayout = binding.drawerLayout
        setContentView(binding.root)

        // Allow for transparent status and navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(this)

        binding.navView.setNavigationItemSelectedListener { item ->
            viewModel.navigationItemSelected(
                item,
                binding.navView.menu.findItem(R.id.drawer_labels).subMenu!!
            )
            true
        }
        viewModel.startPopulatingDrawerWithLabels()

        setupViewModelObservers()
    }

    private fun setupViewModelObservers() {
        val menu = binding.navView.menu
        val labelSubmenu = menu.findItem(R.id.drawer_labels).subMenu!!
        var currentHomeDestination: HomeDestination = HomeDestination.Status(NoteStatus.ACTIVE)

        viewModel.currentHomeDestination.observe(this) { newHomeDestination ->
            sharedViewModel.changeHomeDestination(newHomeDestination)
            currentHomeDestination = newHomeDestination
        }

        viewModel.navDirectionsEvent.observeEvent(this) { navDirections ->
            navController.navigateSafe(navDirections)
        }

        viewModel.drawerCloseEvent.observeEvent(this) {
            drawerLayout.closeDrawers()
        }

        viewModel.clearLabelsEvent.observeEvent(this) {
            labelSubmenu.clear()
        }

        viewModel.labelsAddEvent.observeEvent(this) { labels ->
            if (labels != null) {
                for (label in labels) {
                    labelSubmenu.add(Menu.NONE, View.generateViewId(), Menu.NONE, label.name)
                        .setIcon(R.drawable.ic_label_outline).isCheckable = true
                }
            }

            // Select the current label in the navigation drawer, if it isn't already.
            if (currentHomeDestination is HomeDestination.Labels) {
                val currentLabelName = (currentHomeDestination as HomeDestination.Labels).label.name
                if (binding.navView.checkedItem != null && (
                        binding.navView.checkedItem!! !in labelSubmenu ||
                        binding.navView.checkedItem!!.title != currentLabelName)
                    || binding.navView.checkedItem == null
                ) {
                    labelSubmenu.forEach { item : MenuItem ->
                        if (item.title == currentLabelName) {
                            binding.navView.setCheckedItem(item)
                            return@forEach
                        }
                    }
                }
            }
        }

        viewModel.manageLabelsVisibility.observe(this) { isVisible ->
            menu.findItem(R.id.drawer_item_edit_labels).isVisible = isVisible
        }

        viewModel.editItemEvent.observeEvent(this) { noteId ->
            // Allow navigating to same destination, in case notification is clicked while already editing a note.
            // In this case the EditFragment will be opened multiple times.
            navController.navigateSafe(NavGraphMainDirections.actionEditNote(noteId), true)
        }

        viewModel.autoExportEvent.observeEvent(this) { uri ->
            viewModel.autoExport(try {
                // write and *truncate*. Otherwise the file is not overwritten!
                contentResolver.openOutputStream(Uri.parse(uri), "wt")
            } catch (e: Exception) {
                Log.i(TAG, "Auto data export failed", e)
                null
            })
        }

        viewModel.createNoteEvent.observeEvent(this) { newNoteData ->
            navController.navigateSafe(NavGraphMainDirections.actionEditNote(
                type = newNoteData.type.value,
                title = newNoteData.title,
                content = newNoteData.content
            ))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        drawerLayout.setDrawerLockMode(if (destination.id == R.id.fragment_home) {
            DrawerLayout.LOCK_MODE_UNLOCKED
        } else {
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        })
    }

    override fun onStart() {
        super.onStart()

        // Go to label, if it has been newly created
        sharedViewModel.labelAddEventNav.observeEvent(this) { label ->
            if (navController.previousBackStackEntry?.destination?.id == R.id.fragment_home) {
                viewModel.selectLabel(label)
            }
        }

        viewModel.onStart()
    }

    override fun onResume() {
        super.onResume()
        handleIntent()
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(this)
    }

    private fun handleIntent() {
        val intent = intent ?: return
        if (!intent.getBooleanExtra(KEY_INTENT_HANDLED, false)) {
            when (intent.action) {
                Intent.ACTION_SEND -> {
                    // Plain text was shared to app, create new note for it
                    if (intent.type == "text/plain") {
                        val title = intent.getStringExtra(Intent.EXTRA_TITLE)
                            ?: intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
                        val content = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                        viewModel.createNote(NoteType.TEXT, title, content)
                    }
                }
                INTENT_ACTION_CREATE -> {
                    // Intent to create a note of a certain type. Used by launcher shortcuts.
                    val type = NoteTypeConverter.toType(
                        intent.getIntExtra(EXTRA_NOTE_TYPE, 0))
                    viewModel.createNote(type)
                }
                INTENT_ACTION_EDIT -> {
                    // Intent to edit a specific note. This is used by reminder notification.
                    viewModel.editNote(intent.getLongExtra(AlarmReceiver.EXTRA_NOTE_ID, Note.NO_ID))
                }
                INTENT_ACTION_SHOW_REMINDERS -> {
                    // Show reminders screen in HomeFragment. Used by launcher shortcut.
                    binding.navView.menu.findItem(R.id.drawer_item_reminders).isChecked = true
                    sharedViewModel.changeHomeDestination(HomeDestination.Reminders)
                }
            }

            // Mark intent as handled or it will be handled again if activity is resumed again.
            intent.putExtra(KEY_INTENT_HANDLED, true)
        }
    }

    companion object {
        private const val KEY_INTENT_HANDLED = "com.maltaisn.notes.INTENT_HANDLED"

        const val EXTRA_NOTE_TYPE = "com.maltaisn.notes.NOTE_TYPE"

        const val INTENT_ACTION_CREATE = "com.maltaisn.notes.CREATE"
        const val INTENT_ACTION_EDIT = "com.maltaisn.notes.EDIT"
        const val INTENT_ACTION_SHOW_REMINDERS = "com.maltaisn.notes.SHOW_REMINDERS"
    }
}
