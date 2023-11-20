package com.lyk.router.demo

import android.app.Application
import com.lyk.gradle.router.runtime.Router

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Router.init()
    }
}