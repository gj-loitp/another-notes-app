package com.mckimquyen.notes.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.mckimquyen.notes.App
import com.mckimquyen.notes.R
import com.mckimquyen.notes.databinding.FSettingsBinding
import com.mckimquyen.notes.ext.TAG
import com.mckimquyen.notes.ext.moreApp
import com.mckimquyen.notes.ext.navigateSafe
import com.mckimquyen.notes.ext.openBrowserPolicy
import com.mckimquyen.notes.ext.rateApp
import com.mckimquyen.notes.ext.shareApp
import com.mckimquyen.notes.model.PrefsManager
import com.mckimquyen.notes.ui.AppTheme
import com.mckimquyen.notes.ui.common.ConfirmDialog
import com.mckimquyen.notes.ui.main.MainActivity
import com.mckimquyen.notes.ui.observeEvent
import com.mckimquyen.notes.ui.viewModel
import java.text.DateFormat
import javax.inject.Inject
import com.google.android.material.R as RMaterial

class SettingsFragment : PreferenceFragmentCompat(), ConfirmDialog.Callback, ExportPasswordDialog.Callback,
    ImportPasswordDialog.Callback {

    companion object {
        private const val RESTART_DIALOG_TAG = "restart_dialog"
        private const val CLEAR_DATA_DIALOG_TAG = "clear_data_dialog"
        private const val AUTOMATIC_EXPORT_DIALOG_TAG = "automatic_export_dialog"
    }

    @Inject
    lateinit var viewModelFactory: SettingsViewModel.Factory
    val viewModel by viewModel {
        viewModelFactory.create(it)
    }

    private var exportDataLauncher: ActivityResultLauncher<Intent>? = null
    private var autoExportLauncher: ActivityResultLauncher<Intent>? = null
    private var importDataLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val context = requireContext()

        (context.applicationContext as App?)?.appComponent?.inject(this)

        exportDataLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                val output = try {
                    // write and *truncate*. Otherwise the file is not overwritten!
                    context.contentResolver.openOutputStream(uri, "wt")
                } catch (e: Exception) {
                    Log.i(TAG, "Data export failed", e)
                    null
                }
                if (output != null) {
                    viewModel.exportData(output)
                } else {
                    showMessage(R.string.export_fail)
                }
            }
        }

        autoExportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                val output = try {
                    val cr = context.contentResolver
                    cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    cr.openOutputStream(uri)
                } catch (e: Exception) {
//                    Log.i(TAG, "Data export failed", e)
                    e.printStackTrace()
                    null
                }
                if (output != null) {
                    viewModel.setupAutoExport(output = output, uri = uri.toString())
                } else {
                    showMessage(R.string.export_fail)
                    autoExportPref.isChecked = false
                }
            }
        }

        importDataLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                val input = try {
                    context.contentResolver.openInputStream(uri)
                } catch (e: Exception) {
//                    Log.i(TAG, "Data import failed", e)
                    e.printStackTrace()
                    null
                }
                if (input != null) {
                    viewModel.importData(input)
                } else {
                    showMessage(R.string.import_bad_input)
                }
            }
        }

        enterTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(RMaterial.integer.material_motion_duration_short_2).toLong()
        }
        exitTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(RMaterial.integer.material_motion_duration_short_2).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FSettingsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Apply padding so that the settings don't overlap with the navigation bar
        val rcv = view.findViewById<RecyclerView>(R.id.recyclerView)
//        if (rcv == null) {
//            Log.e("roy93~", "onViewCreated: rcv == null")
//        } else {
//            Log.e("roy93~", "onViewCreated: rcv != null")
//        }

        ViewCompat.setOnApplyWindowInsetsListener(rcv) { _, insets ->
            val sysWindow = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            rcv.updatePadding(bottom = sysWindow.bottom)
            insets
        }

        setupViewModelObservers()
    }

    private fun setupViewModelObservers() {
        viewModel.messageEvent.observeEvent(viewLifecycleOwner, ::showMessage)
        viewModel.lastAutoExport.observe(viewLifecycleOwner) { date ->
            updateAutoExportSummary(autoExportPref.isChecked, date)
        }
        viewModel.releasePersistableUriEvent.observeEvent(viewLifecycleOwner) { uri ->
            try {
                requireContext().contentResolver.releasePersistableUriPermission(
                    Uri.parse(uri),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission was revoked? will probably happen sometimes
                Log.i(TAG, "Failed to release persistable URI permission", e)
            }
        }
        viewModel.showImportPasswordDialogEvent.observeEvent(viewLifecycleOwner) {
            ImportPasswordDialog.newInstance()
                .show(childFragmentManager, null)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        setPreferencesFromResource(R.xml.prefs, rootKey)

        requirePreference<DropDownPreference>(PrefsManager.THEME).setOnPreferenceChangeListener { _, theme ->
            (context.applicationContext as App).updateTheme(AppTheme.fromValue(theme as String))
            true
        }

        requirePreference<Preference>(PrefsManager.DYNAMIC_COLORS).apply {
            if (DynamicColors.isDynamicColorAvailable()) {
                setOnPreferenceClickListener {
                    ConfirmDialog.newInstance(
                        title = R.string.pref_restart_dialog_title,
                        message = R.string.pref_restart_dialog_description,
                        btnPositive = R.string.action_ok
                    ).show(childFragmentManager, RESTART_DIALOG_TAG)
                    true
                }
            } else {
                // Hide dynamic color / material you preference on unsupported Android versions
                isVisible = false
            }
        }

        requirePreference<Preference>(PrefsManager.PREVIEW_LINES).setOnPreferenceClickListener {
            findNavController().navigateSafe(
                SettingsFragmentDirections.actionNestedSettings(
                    R.xml.prefs_preview_lines, R.string.pref_preview_lines
                )
            )
            true
        }

        requirePreference<Preference>(PrefsManager.EXPORT_DATA).setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/json").addCategory(Intent.CATEGORY_OPENABLE)
            exportDataLauncher?.launch(intent)
            true
        }

        val encryptedExportPref: SwitchPreferenceCompat = requirePreference(PrefsManager.ENCRYPTED_EXPORT)
        // Older versions don't support PBKDF2withHmacSHA512
        if (Build.VERSION.SDK_INT < 26) {
            encryptedExportPref.isVisible = false
        }

        encryptedExportPref.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                ExportPasswordDialog.newInstance()
                    .show(childFragmentManager, null)
            } else {
                viewModel.deleteExportKey()
            }
            true
        }

        autoExportPref.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                ConfirmDialog.newInstance(
                    title = R.string.pref_data_auto_export,
                    message = R.string.auto_export_message,
                    btnPositive = R.string.action_ok
                ).show(childFragmentManager, AUTOMATIC_EXPORT_DIALOG_TAG)
            } else {
                updateAutoExportSummary(false)
                viewModel.disableAutoExport()
            }
            true
        }

        requirePreference<Preference>(PrefsManager.IMPORT_DATA).setOnPreferenceClickListener {
            // note: explicit mimetype fails for some devices, see #11
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*").addCategory(Intent.CATEGORY_OPENABLE)
            importDataLauncher?.launch(intent)
            true
        }

        requirePreference<Preference>(PrefsManager.CLEAR_DATA).setOnPreferenceClickListener {
            ConfirmDialog.newInstance(
                title = R.string.pref_data_clear,
                message = R.string.pref_data_clear_confirm_message,
                btnPositive = R.string.action_clear
            ).show(childFragmentManager, CLEAR_DATA_DIALOG_TAG)
            true
        }

//        requirePreference<Preference>(PrefsManager.VIEW_LICENSES).setOnPreferenceClickListener {
//            findNavController().navigate(R.id.action_about_libraries, bundleOf(
//                // Navigation component safe args seem to fail for cross module navigation.
//                // So pass the customization argument the old way.
//                "data" to LibsBuilder().apply {
//                    aboutShowIcon = false
//                    aboutShowVersion = false
//                }
//            ))
//            true
//        }

        requirePreference<Preference>("shareApp").setOnPreferenceClickListener {
            activity?.shareApp()
            true
        }
        requirePreference<Preference>("rateApp").setOnPreferenceClickListener {
            activity?.rateApp(activity?.packageName ?: "")
            true
        }
        requirePreference<Preference>("moreApp").setOnPreferenceClickListener {
            activity?.moreApp()
            true
        }
        requirePreference<Preference>("policy").setOnPreferenceClickListener {
            activity?.openBrowserPolicy()
            true
        }

        // Set version name as summary text for version preference
        requirePreference<Preference>(PrefsManager.VERSION).summary = com.mckimquyen.notes.BuildConfig.VERSION_NAME
    }

    override fun onDestroy() {
        super.onDestroy()
        exportDataLauncher = null
        autoExportLauncher = null
    }

    private fun showMessage(@StringRes messageId: Int) {
        val snackbar = Snackbar.make(requireView(), messageId, Snackbar.LENGTH_SHORT)
            .setGestureInsetBottomIgnored(true)
        snackbar.view.findViewById<androidx.appcompat.widget.AppCompatTextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            5
        snackbar.show()
    }

    private fun <T : Preference> requirePreference(key: CharSequence) =
        checkNotNull(findPreference<T>(key)) { "Could not find preference with key '$key'." }

    private val autoExportPref: SwitchPreferenceCompat
        get() = requirePreference(PrefsManager.AUTO_EXPORT)

    private fun updateAutoExportSummary(enabled: Boolean, date: Long = 0) {
        if (enabled) {
            autoExportPref.summary = buildString {
                appendLine(getString(R.string.pref_data_auto_export_summary))
                append(
                    getString(
                        R.string.pref_data_auto_export_date,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date)
                    )
                )
            }
        } else {
            autoExportPref.summary = getString(R.string.pref_data_auto_export_summary)
        }
    }

    override fun onDialogPositiveButtonClicked(tag: String?) {
        when (tag) {
            RESTART_DIALOG_TAG -> {
                // Reload MainActivity to apply theming changes
                requireActivity().finish()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }

            CLEAR_DATA_DIALOG_TAG -> {
                viewModel.clearData()
            }

            AUTOMATIC_EXPORT_DIALOG_TAG -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .setType("application/json")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                autoExportLauncher?.launch(intent)
            }
        }
    }

    override fun onDialogNegativeButtonClicked(tag: String?) {
        if (tag == AUTOMATIC_EXPORT_DIALOG_TAG) {
            // No file chosen for auto export, disable it.
            autoExportPref.isChecked = false
        }
    }

    override fun onDialogCancelled(tag: String?) {
        if (tag == AUTOMATIC_EXPORT_DIALOG_TAG) {
            // No file chosen for auto export, disable it.
            autoExportPref.isChecked = false
        }
    }

    private val exportEncryptionPref: SwitchPreferenceCompat
        get() = requirePreference(PrefsManager.ENCRYPTED_EXPORT)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onExportPasswordDialogPositiveButtonClicked(password: String) {
        viewModel.generateExportKeyFromPassword(password)
    }

    override fun onExportPasswordDialogNegativeButtonClicked() {
        exportEncryptionPref.isChecked = false
    }

    override fun onExportPasswordDialogCancelled() {
        exportEncryptionPref.isChecked = false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onImportPasswordDialogPositiveButtonClicked(password: String) {
        viewModel.importSavedEncryptedJsonData(password)
    }
}
