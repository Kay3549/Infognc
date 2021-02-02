package com.github.arekolek.phone

import android.app.Application

import timber.log.Timber

class WH_App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
