package io.minio.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MinIoApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}