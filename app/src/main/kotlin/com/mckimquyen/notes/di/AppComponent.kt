package com.mckimquyen.notes.di

import android.content.Context
import com.mckimquyen.notes.RApp
import com.mckimquyen.notes.receiver.AlarmReceiver
import com.mckimquyen.notes.ui.edit.EditFragment
import com.mckimquyen.notes.ui.home.HomeFragment
import com.mckimquyen.notes.ui.labels.LabelEditDialog
import com.mckimquyen.notes.ui.labels.LabelFragment
import com.mckimquyen.notes.ui.main.MainActivity
import com.mckimquyen.notes.ui.noti.NotificationActivity
import com.mckimquyen.notes.ui.reminder.ReminderDialog
import com.mckimquyen.notes.ui.search.SearchFragment
import com.mckimquyen.notes.ui.setting.ExportPasswordDlg
import com.mckimquyen.notes.ui.setting.ImportPasswordDlg
import com.mckimquyen.notes.ui.setting.SettingsFrm
import com.mckimquyen.notes.ui.sort.SortDialog
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(RApp: RApp)

    fun inject(activity: MainActivity)
    fun inject(activity: NotificationActivity)

    fun inject(fragment: HomeFragment)
    fun inject(fragment: SearchFragment)
    fun inject(fragment: EditFragment)
    fun inject(fragment: LabelFragment)
    fun inject(fragment: SettingsFrm)
    fun inject(dialog: ReminderDialog)
    fun inject(dialog: LabelEditDialog)
    fun inject(dialog: SortDialog)
    fun inject(dialog: ExportPasswordDlg)
    fun inject(dialog: ImportPasswordDlg)
    fun inject(receiver: AlarmReceiver)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }
}
