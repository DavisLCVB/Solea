package com.grupo03.solea

import android.app.Application
import com.grupo03.solea.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SoleaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SoleaApplication)
            modules(appModule)
        }
    }
}
