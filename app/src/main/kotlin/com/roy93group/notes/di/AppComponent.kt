package com.roy93group.notes.di

import android.content.Context
import com.roy93group.notes.App
import com.roy93group.notes.receiver.AlarmReceiver
import com.roy93group.notes.ui.edit.EditFragment
import com.roy93group.notes.ui.home.HomeFragment
import com.roy93group.notes.ui.labels.LabelEditDialog
import com.roy93group.notes.ui.labels.LabelFragment
import com.roy93group.notes.ui.main.MainActivity
import com.roy93group.notes.ui.noti.NotificationActivity
import com.roy93group.notes.ui.reminder.ReminderDialog
import com.roy93group.notes.ui.search.SearchFragment
import com.roy93group.notes.ui.settings.ExportPasswordDialog
import com.roy93group.notes.ui.settings.ImportPasswordDialog
import com.roy93group.notes.ui.settings.SettingsFragment
import com.roy93group.notes.ui.sort.SortDialog
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(app: App)

    fun inject(activity: MainActivity)
    fun inject(activity: NotificationActivity)

    fun inject(fragment: HomeFragment)
    fun inject(fragment: SearchFragment)
    fun inject(fragment: EditFragment)
    fun inject(fragment: LabelFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(dialog: ReminderDialog)
    fun inject(dialog: LabelEditDialog)
    fun inject(dialog: SortDialog)
    fun inject(dialog: ExportPasswordDialog)
    fun inject(dialog: ImportPasswordDialog)
    fun inject(receiver: AlarmReceiver)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }
}
