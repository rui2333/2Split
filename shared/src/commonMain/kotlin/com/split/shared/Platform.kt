package com.split.shared

expect class Platform() {
    val platform: String
}

fun getPlatformName(): String = "Hello from ${Platform().platform}"
