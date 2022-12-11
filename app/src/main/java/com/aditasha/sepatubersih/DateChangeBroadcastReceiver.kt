package com.aditasha.sepatubersih

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DateChangeBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var serverTime: ServerTime
    override fun onReceive(context: Context?, intent: Intent?) {
        serverTime.refreshServerTime()
    }
}