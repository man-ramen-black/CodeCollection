package com.netmarble.nmapp.base.component

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.netmarble.nmapp.util.Log

/**
 * Fragment Navigate 시 Fragment 상태를 유지시켜주는 Navigator
 * Created by jinhyuk.lee on 2022/06/14
 **/
@Navigator.Name("keep-fragment")
class KeepFragmentNavigator(private val context: Context, private val fragmentManager: FragmentManager, private val containerId: Int)
    : Navigator<FragmentNavigator.Destination>() {
    private companion object {
        private const val KEY_BACK_STACK = "KeepFragmentNavigator.BackStack"
    }

    private val backStack = ArrayList<String>()

    /*
    최근 NavOptions 애니메이션을 저장하여 popBackStack에서 사용
    애니메이션을 적용하지 않고 맨 첫번째 Fragment(Root)로 이동하면 UI가 노출되지 않음
     */
    private var enterAnim = -1
    private var exitAnim = -1
    private var popEnterAnim = -1
    private var popExitAnim = -1

    private var isAnimationEnabled = true

    override fun navigate(
        destination: FragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        val tag = destination.className

        if (backStack.lastOrNull() == tag) {
            Log.w("Duplicate navigate")
            return null
        }

        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }

        val foundFrag = fragmentManager.findFragmentByTag(tag)
        val frag = foundFrag ?: fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
        frag.arguments = args

        if (isAnimationEnabled) {
            enterAnim = navOptions?.enterAnim ?: -1
            exitAnim = navOptions?.exitAnim ?: -1
            popEnterAnim = navOptions?.popEnterAnim ?: -1
            popExitAnim = navOptions?.popExitAnim ?: -1
        } else {
            enterAnim = -1
            exitAnim = -1
            popEnterAnim = -1
            popExitAnim = -1
        }

        fragmentManager.commit {
            setCustomAnimations()
            hideLatest()

            if (foundFrag == null) {
                add(containerId, frag, tag)
            } else {
                show(frag)
            }
            // setMaxLifecycle : 화면이 보일때 onResume, 보이지 않을땨 onPause가 호출되도록 설정
            // https://pluu.github.io/blog/android/2023/01/19/fragment_visible_lifecycleowner/
            // https://stackoverflow.com/questions/56130557/how-to-use-setuservisiblehint-in-fragment-on-android
            setMaxLifecycle(frag, Lifecycle.State.RESUMED)

            /*
            setReorderingAllowed(true)는 애니메이션과 전환이 올바르게 작동하도록 트랜잭션과 관련된 프래그먼트의 상태 변경을 최적화합니다.
            https://developer.android.com/guide/fragments/fragmentmanager?hl=ko#perform
             */
            setReorderingAllowed(true)
        }

        backStack.add(tag)
        return destination
    }

    override fun popBackStack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }

        val hideTag = backStack.lastOrNull()
        val hideFrag = fragmentManager.findFragmentByTag(hideTag)
        backStack.removeLast()

        val showTag = backStack.lastOrNull()
        val showFrag = fragmentManager.findFragmentByTag(showTag)

        fragmentManager.commit {
            setCustomAnimations()
            hideFrag?.run {
                hide(hideFrag)
                setMaxLifecycle(hideFrag, Lifecycle.State.STARTED)
            }
            showFrag?.run {
                show(showFrag)
                setMaxLifecycle(showFrag, Lifecycle.State.RESUMED)
            }
            setReorderingAllowed(true)
        }
        return true
    }

    private fun FragmentTransaction.setCustomAnimations() : FragmentTransaction {
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = if (enterAnim != -1) enterAnim else 0
            exitAnim = if (exitAnim != -1) exitAnim else 0
            popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
            popExitAnim = if (popExitAnim != -1) popExitAnim else 0
            this.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }
        return this
    }

    /**
     * 가장 마지막에 BackStack에 추가된 Fragment를 숨김
     */
    private fun FragmentTransaction.hideLatest() : FragmentTransaction {
        val hideFrag = fragmentManager.findFragmentByTag(backStack.lastOrNull())
            ?: run {
                Log.w("Latest fragment not found")
                return this@hideLatest
            }
        hide(hideFrag)
        setMaxLifecycle(hideFrag, Lifecycle.State.STARTED)
        return this
    }

    /**
     * [FragmentNavigator.onSaveState]를 참고하여 구현
     */
    override fun onSaveState(): Bundle? {
        Log.d()
        if (backStack.isEmpty()) {
            return null
        }
        return bundleOf(KEY_BACK_STACK to ArrayList(backStack))
    }

    /**
     * [FragmentNavigator.onRestoreState]를 참고하여 구현
     */
    override fun onRestoreState(savedState: Bundle) {
        Log.d(savedState)
        val backStack = savedState.getStringArrayList(KEY_BACK_STACK)
        if (backStack != null) {
            this.backStack.clear()
            this.backStack.addAll(backStack)
        }
    }

    override fun createDestination(): FragmentNavigator.Destination = FragmentNavigator.Destination(this)

    /**
     * 화면 이동 시 페이드 인/아웃 애니메이션 활성화/비활성화
     */
    fun setAnimationEnabled(enable: Boolean) {
        isAnimationEnabled = enable
    }
}