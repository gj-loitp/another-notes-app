package com.mckimquyen.notes.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mckimquyen.notes.R
import com.mckimquyen.notes.ui.main.BaseAct
import com.mckimquyen.notes.ui.main.MainAct

class SplashAct : BaseAct() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_DayNight)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainAct::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
