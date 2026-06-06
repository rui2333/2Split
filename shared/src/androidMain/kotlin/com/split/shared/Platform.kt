package com.split.shared

import android.os.Build

actual class Platform() {
    actual val platform: String
        get() = "Android ${Build.VERSION.SDK_INT}"
}
