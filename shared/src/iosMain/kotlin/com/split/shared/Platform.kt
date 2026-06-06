package com.split.shared

import platform.UIKit.UIDevice

actual class Platform() {
    actual val platform: String
        get() = "iOS ${UIDevice.currentDevice.systemVersion}"
}
