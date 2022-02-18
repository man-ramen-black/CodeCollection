package com.black.code.contents

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.black.code.R
import com.black.code.base.BaseFragment
import com.black.code.contents.alarm.AlarmFragment
import com.black.code.contents.launcher.LauncherFragment
import com.black.code.contents.recyclerview.RecyclerViewFragment
import com.black.code.contents.sample.SampleFragment
import com.black.code.databinding.FragmentContentsListBinding
import kotlinx.android.synthetic.main.fragment_contents_list.*

class ContentsListFragment : BaseFragment<FragmentContentsListBinding>() {
    override val layoutResId: Int = R.layout.fragment_contents_list
    private val contentsList : List<ContentsFragment<*>> = listOf(
        SampleFragment(),
        RecyclerViewFragment(),
        AlarmFragment(),
        LauncherFragment()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addContentsFragment()
    }

    private fun addContentsFragment() {
        for (contentsFragment in contentsList) {
            val button = Button(context)
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
}