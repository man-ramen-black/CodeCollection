package com.black.code.contents.service

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import com.black.code.util.Log

class ServiceViewModel : ViewModel() {
    fun onClickStartForegroundService(view: View?) {
        val appContext = view?.context?.applicationContext
            ?: run {
                Log.e("context is null")
                return
            }
        ForegroundService.start(appContext)
    }

    fun onClickStopForegroundService(view: View?) {
        val appContext = view?.context?.applicationContext
            ?: run {
                Log.e("context is null")
                return
            }
        ForegroundService.stop(appContext)
    }
}