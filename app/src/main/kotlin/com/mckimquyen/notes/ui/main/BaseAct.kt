package com.mckimquyen.notes.ui.main

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

open class BaseAct : AppCompatActivity() {
    override fun attachBaseContext(context: Context) {
        val override = Configuration(context.resources.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
        super.attachBaseContext(context)
    }
}
