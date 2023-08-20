package com.black.code.util

import android.content.res.Resources
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch

/**
 * Created by jinhyuk.lee on 2023/07/07
 **/
object FragmentExtension {
    /**
     * 이 프래그먼트가 가지고 있는 NavHostFragment 획득한다.
     * onViewCreated에서 호출 필요
     */
    fun Fragment.findNavHostFragment() : NavHostFragment? {
        return childFragmentManager.fragments.find { it is NavHostFragment } as? NavHostFragment
    }

    /**
     * 이 프래그먼트가 가지고 있는 navController를 획득한다.
     * onViewCreated에서 호출 필요
     */
    fun Fragment.findChildNavController() : NavController {
        val navHostFragment = findNavHostFragment()
            ?: throw Resources.NotFoundException("NavHostFragment not found")
        return navHostFragment.navController
    }

    /**
     * 현재 위치가 더 이상 pop되면 안되는 최상단 상태인지 반환
     */
    fun NavController.isRootDestination() : Boolean {
        return previousBackStackEntry == null
    }

    /**
     * popBackStack 후 이전 프래그먼트가 전달받을 데이터를 set
     */
    fun <T> NavController.setPopBackStackArgs(key: String, value: T?) {
        previousBackStackEntry?.savedStateHandle?.set(key, value)
    }

    /**
     * [setPopBackStackArgs]으로 설정되는 데이터를 observe
     */
    fun <T> NavController.observePopBackStackArgs(owner: LifecycleOwner, key: String, observer: Observer<T>) {
        currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)
            ?.observe(owner, observer)
    }

    /**
     * [setPopBackStackArgs]으로 설정되는 데이터를 observe
     * 이벤트는 onResume 시 호출됨
     */
    fun <T> NavController.observePopBackStackArgsWithResumed(owner: LifecycleOwner, key: String, observer: Observer<T>) {
        observePopBackStackArgs<T>(owner, key) {
            owner.lifecycleScope.launch {
                owner.lifecycle.withResumed {
                    observer.onChanged(it)
                }
            }
        }
    }

    /**
     * 현재 destination에서 navigate할 수 없는 directions이면 navigate하지 않음
     */
    fun NavController.navigateSafety(directions: NavDirections) {
        if (currentDestination?.getAction(directions.actionId) != null) {
            navigate(directions)
        }
    }

    /**
     * 부모 프래그먼트의 ViewModel 획득
     */
    @MainThread
    inline fun <reified VM : ViewModel> Fragment.parentViewModels() : Lazy<VM> {
        return viewModels({ requireParentFragment() })
    }
}