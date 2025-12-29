package com.ato.recordodon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform