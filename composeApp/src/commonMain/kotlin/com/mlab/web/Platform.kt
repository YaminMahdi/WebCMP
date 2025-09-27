package com.mlab.web

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

