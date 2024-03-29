package com.black.code.ui.example.recyclerview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.black.code.R
import com.black.code.base.viewmodel.EventObserver
import com.black.code.databinding.FragmentRecyclerviewBinding
import com.black.code.base.component.ExampleFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecyclerViewFragment : ExampleFragment<FragmentRecyclerviewBinding>(), EventObserver {
    override val title: String = "RecyclerView"
    override val layoutResId: Int = R.layout.fragment_recyclerview

    private val viewModel : RecyclerViewViewModel by viewModels()
    private val adapter by lazy { RecyclerViewAdapter(viewModel) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.recyclerView?.let {
            it.adapter = this@RecyclerViewFragment.adapter

            // RecyclerView는 divider 대신에 ItemDecoration을 사용... Interesting...
            // https://leveloper.tistory.com/180
            it.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun bindVariable(binding: FragmentRecyclerviewBinding) {
        binding.viewModel = viewModel.apply {
            observeEvent(viewLifecycleOwner, this@RecyclerViewFragment)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onReceivedEvent(action: String, data: Any?) {
        when (action) {
            RecyclerViewViewModel.EVENT_SHOW_DIALOG -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(data?.toString() ?: "")
                    .setNeutralButton("확인") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            RecyclerViewViewModel.EVENT_SHOW_TOAST -> {
                Toast.makeText(context, data?.toString() ?: "", Toast.LENGTH_SHORT)
                    .show()
            }

            RecyclerViewViewModel.EVENT_UPDATE_RECYCLER_VIEW -> {
                adapter.submitList(data as List<RecyclerViewData>)
            }
        }
    }
}