package com.black.code.ui.example.studypopup

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.black.code.R
import com.black.code.base.view.BaseListAdapter
import com.black.code.databinding.ItemStudyPopupListContentsBinding
import com.black.code.databinding.ItemStudyPopupListFileManagerBinding
import com.black.code.model.database.studypopup.StudyPopupData

/**
 * Created by jinhyuk.lee on 2022/05/23
 **/
class StudyPopupListAdapter(private val viewModel: StudyPopupFragmentViewModel) : BaseListAdapter<StudyPopupData>() {
    companion object {
        private const val VIEW_TYPE_FILE_MANAGER = 0
        private const val VIEW_TYPE_CONTENTS = 1
    }

    class FileManagerViewHolder(binding: ItemStudyPopupListFileManagerBinding, private val viewModel: StudyPopupFragmentViewModel)
        : BaseListAdapter.BaseViewHolder<ItemStudyPopupListFileManagerBinding, StudyPopupData>(binding)
    {
        override fun bind(item: StudyPopupData) {
            binding.viewModel = viewModel
        }
    }

    class ContentsViewHolder(binding: ItemStudyPopupListContentsBinding, private val viewModel: StudyPopupFragmentViewModel)
        : BaseListAdapter.BaseViewHolder<ItemStudyPopupListContentsBinding, StudyPopupData>(binding)
    {
        override fun bind(item: StudyPopupData) {
            binding.viewModel = viewModel
            binding.viewHolder = this
            binding.data = item as StudyPopupData.Contents
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StudyPopupData.FileManager -> VIEW_TYPE_FILE_MANAGER
            is StudyPopupData.Contents -> VIEW_TYPE_CONTENTS
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding, StudyPopupData> {
        return when (viewType) {
            VIEW_TYPE_FILE_MANAGER -> FileManagerViewHolder(inflateForViewHolder(parent, R.layout.item_study_popup_list_file_manager), viewModel)
            else -> ContentsViewHolder(inflateForViewHolder(parent, R.layout.item_study_popup_list_contents), viewModel)
        }
    }
}