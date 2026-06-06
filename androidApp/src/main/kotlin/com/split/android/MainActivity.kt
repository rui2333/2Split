package com.split.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.split.shared.getPlatformName

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val platformName = getPlatformName()
        // TODO: Display platformName in UI
    }
}
