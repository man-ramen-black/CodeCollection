package com.black.code.ui.example

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.black.code.R
import com.black.code.base.component.BaseFragment
import com.black.code.databinding.FragmentContentsListBinding
import com.black.code.ui.example.alarm.AlarmFragment
import com.black.code.ui.example.architecture.ArchitectureFragment
import com.black.code.ui.example.launcher.LauncherFragment
import com.black.code.ui.example.notification.NotificationFragment
import com.black.code.ui.example.recyclerview.RecyclerViewFragment
import com.black.code.ui.example.sample.ETCFragment
import com.black.code.ui.example.service.ServiceFragment
import com.black.code.ui.example.texteditor.TextEditorFragment
import com.black.code.ui.example.usagetimechecker.UsageTimeCheckerFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_contents_list.*

class ExampleListFragment : BaseFragment<FragmentContentsListBinding>() {
    override val layoutResId: Int = R.layout.fragment_contents_list
    private val exampleList : List<com.black.code.ui.example.ExampleFragment<*>> = listOf(
        RecyclerViewFragment(),
        AlarmFragment(),
        LauncherFragment(),
        NotificationFragment(),
        ServiceFragment(),
        UsageTimeCheckerFragment(),
        ArchitectureFragment(),
        TextEditorFragment(),
        ETCFragment()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addContentsFragment()
    }

    private fun addContentsFragment() {
        for (contentsFragment in exampleList) {
            /*
            Theme, Style 적용하여 뷰 생성
            val button = Button(ContextThemeWrapper(context, R.style.AppTheme), null, R.style.AppTheme)
             */
             // AppTheme이 Material 라이브러리 테마인 경우 Material View를 생성해주면 앱 테마에 맞는 뷰가 생성됨
            val button = MaterialButton(requireContext())
                .apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    text = contentsFragment.title
                    setOnClickListener {
                        parentFragmentManager.beginTransaction()
                            // add는 Fragment가 오버랩되어 추가됨
                            // 일반적으로 생각하는 add를 하려면 replace -> addToBackStack으로 구현
                            .replace(R.id.fragmentContainer, contentsFragment)
                            // addToBackStack은 commit 전에 호출
                            // FragmentManager.BackStackEntry 등의 기능으로 백스택 추적, 관리하지 않으면 name은 null로 설정
                            // https://developer.android.com/training/basics/fragments/fragment-ui?hl=ko#Replace
                            .addToBackStack(null)
                            .commit()
                    }
                }
            contentsContainer.addView(button)
        }
    }

    override fun bindVariable(binding: FragmentContentsListBinding) {
    }

}