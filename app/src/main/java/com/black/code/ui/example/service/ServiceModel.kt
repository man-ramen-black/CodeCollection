package com.black.code.ui.example.service

import android.content.Context
import com.black.code.model.preferences.ForegroundServicePreference
import com.black.code.service.ForegroundService

/**
 * Created by jinhyuk.lee on 2022/05/07
 **/
class ServiceModel(private val context: Context) {
    private val preference = ForegroundServicePreference(context)

    fun startForegroundService() : Boolean {
        preference.putForegroundServiceActivated(true)
        return ForegroundService.start(context)
    }

    fun stopForegroundService() {
        preference.putForegroundServiceActivated(false)
        ForegroundService.stop(context)
    }

    fun getForegroundServiceActivated() : Boolean {
        return preference.getForegroundServiceActivated()
    }
}