package com.netmarble.nmapp.base.component

import androidx.activity.ComponentActivity

/**
 * 개선된 OnBackPressedCallback
 * Created by jinhyuk.lee on 2023/06/19
 **/
open class OnBackPressedCallback(enabled: Boolean = true, private val onBackPressed: OnBackPressedCallback.() -> Unit)
    : androidx.activity.OnBackPressedCallback(enabled)
{
    constructor(onBackPressed: OnBackPressedCallback.() -> Unit) : this(true, onBackPressed)

    /**
     * 부모의 OnBackPressed를 호출한다.
     */
    fun callSuperOnBackPressed(activity: ComponentActivity) {
        /*
        현재 콜백을 제거하고, onBackPressed를 호출하고 다시 addCallback
         */
        remove()
        val dispatcher = activity.onBackPressedDispatcher
        dispatcher.onBackPressed()
        dispatcher.addCallback(this)
    }

    override fun handleOnBackPressed() {
        onBackPressed(this)
    }
}