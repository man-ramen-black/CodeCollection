package com.netmarble.nmapp.base.component

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.ViewDataBinding
import com.netmarble.nmapp.util.Log

/**
 * Created by jinhyuk.lee on 2022/05/09
 **/
@SuppressLint("CustomSplashScreen")
abstract class BaseSplashActivity<T : ViewDataBinding>  : BaseActivity<T>() {
    @Volatile var keepOnSplashScreen = true // Splash 화면 유지 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(this::class.java.simpleName)
        /*
        Android 12 Splash 대응
        https://developer.android.com/guide/topics/ui/splash-screen/migrate
         */
        installSplashScreen().apply {
            // setKeepOnScreenCondition에 설정한 Block은 짧은 주기로 계속 호출되면서 상태를 확인한다.
            setKeepOnScreenCondition { keepOnSplashScreen }
            setOnExitAnimationListener { provider ->
                ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                    duration = 300L
                    doOnEnd { provider.remove() }
                }.start()
            }
        }
        super.onCreate(savedInstanceState)
    }

    /**
     * launchMode가 singleTop, singleTask인 경우,
     * Intent.FLAG_ACTIVITY_SINGLE_TOP로 Activity를 실행한 경우
     * onPause -> onNewIntent -> onResume 순서로 실행됨
     * https://developer.android.com/reference/android/app/Activity#onNewIntent(android.content.Intent)
     */
    override fun onNewIntent(intent: Intent?) {
        Log.d(this::class.java.simpleName)
        super.onNewIntent(intent)
        // startNextActivity()에서 onNewIntent의 intent를 사용하도록 setIntent 호출
        setIntent(intent)
    }
}