package com.aditasha.sepatubersih

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins

@HiltAndroidApp
class SepatuBersih : Application(), OnMapsSdkInitializedCallback {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED)
        registerReceiver(DateChangeBroadcastReceiver(), intentFilter)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) {
                Log.d(TAG, e.toString())
            } else {
                Thread.currentThread().also { thread ->
                    thread.uncaughtExceptionHandler?.uncaughtException(thread, e)
                }
            }
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d(
                "MapsDemo",
                "The latest version of the renderer is used."
            )
            MapsInitializer.Renderer.LEGACY -> Log.d(
                "MapsDemo",
                "The legacy version of the renderer is used."
            )
        }
    }
}