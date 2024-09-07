package com.mckimquyen.notes.receiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mckimquyen.notes.model.ReminderAlarmCallback
import com.mckimquyen.notes.model.ReminderAlarmManager
import javax.inject.Inject

/**
 * Implementation of the alarm callback for [ReminderAlarmManager].
 * Uses the app context to set alarms broadcasted to [AlarmReceiver].
 */
class ReceiverAlarmCallback @Inject constructor(
    private val context: Context,
) : ReminderAlarmCallback {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    override fun addAlarm(noteId: Long, time: Long) {
        val alarmIntent = getAlarmPendingIndent(noteId)
        //TODO roy93~ co the chuyen sang dung alarm chinh xac setExactAndAllowWhileIdle trong tuong lai
//        alarmManager.setExactAndAllowWhileIdle(
//            /* type = */ AlarmManager.RTC_WAKEUP,
//            /* triggerAtMillis = */ time,
//            /* operation = */ alarmIntent,
//        )
//        alarmManager.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            time,
//            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//            alarmIntent
//        )
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            time,
            alarmIntent
        )
    }

    override fun removeAlarm(noteId: Long) {
        getAlarmPendingIndent(noteId).cancel()
    }

    private fun getAlarmPendingIndent(noteId: Long): PendingIntent {
        // Make alarm intent
        val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
            putExtra(AlarmReceiver.EXTRA_NOTE_ID, noteId)
        }
        var flags = 0
        flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ noteId.toInt(),
            /* intent = */ receiverIntent,
            /* flags = */ flags
        )
    }
}
