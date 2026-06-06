package com.split.shared

import android.os.Build

actual class Platform actual constructor() {
    actual val platform: String = "Android ${Build.VERSION.SDK_INT}"
}
