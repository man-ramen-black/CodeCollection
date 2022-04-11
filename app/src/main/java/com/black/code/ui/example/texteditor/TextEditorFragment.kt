package com.black.code.ui.example.texteditor

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.black.code.R
import com.black.code.base.viewmodel.EventObserver
import com.black.code.databinding.FragmentTextEditorBinding
import com.black.code.util.FileUtil
import com.black.code.util.Log
import com.black.code.util.PermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextEditorFragment : com.black.code.ui.example.ExampleFragment<FragmentTextEditorBinding>(), EventObserver {

    companion object {
        private val PERMISSIONS = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override val layoutResId: Int
        get() = R.layout.fragment_text_editor

    override val title: String
        get() = "TextEditor"

    private val viewModel : TextEditorViewModel by viewModels()

    private lateinit var openDocumentLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var createDocumentLauncher : ActivityResultLauncher<String>

    private val permissionHelper by lazy { PermissionHelper(requireActivity(), this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHelper.init()

        // registerForActivityResult는 onAttach 또는 onCreate에서 호출되어야 함
        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument(), this::onOpenDocument)
        createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument(), this::onCreateDocument)
    }

    override fun bindVariable(binding: FragmentTextEditorBinding) {
        binding.fragment = this
        binding.viewModel = viewModel
        viewModel.event.observe(this, this)
    }

    override fun onReceivedEvent(action: String, data: Any?) {
        when (action) {
            TextEditorViewModel.EVENT_LOAD -> load()

            TextEditorViewModel.EVENT_SAVE_NEW_DOCUMENT -> saveNewFile()

            TextEditorViewModel.EVENT_SAVE_OVERWRITE -> saveOverwrite()

            TextEditorViewModel.EVENT_CLEAR -> confirmNewFile()

            TextEditorViewModel.EVENT_TOAST -> Toast.makeText(requireContext(), data?.toString() ?: "", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onOpenDocument(uri: Uri?) {
        uri ?: run {
            Log.w("uri is null")
            return
        }

        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val path = FileUtil.getPath(requireContext(), uri)
        viewModel.loadFile(uri, path, inputStream)
    }

    private fun onCreateDocument(uri: Uri?) {
        uri ?: run {
            Log.w("uri is null")
            return
        }

        // wt : 스트림을 열면서 해당 파일 내용을 지움
        val outputStream = requireActivity().contentResolver.openOutputStream(uri, "wt")
        val path = FileUtil.getPath(requireContext(), uri)
        viewModel.saveNewFile(uri, path, outputStream)
    }

    private fun requestPermission(onPermissionGranted : () -> Unit) {
        permissionHelper.requestPermissions(PERMISSIONS) {
            if (it.deniedPermissions.isEmpty() && it.retryPermissions.isEmpty()) {
                onPermissionGranted()
            } else {
                Toast.makeText(requireContext(), "권한 허용 필요", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun load() {
        requestPermission {
            openDocumentLauncher.launch(arrayOf("text/plain"))
        }
    }

    private fun saveNewFile() {
        requestPermission {
            createDocumentLauncher.launch(".txt")
        }
    }

    private fun saveOverwrite() {
        val openedFileUri = viewModel.openedFileUri ?: run {
            Toast.makeText(requireContext(), "openedFileUri is null", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val stream = requireActivity().contentResolver.openOutputStream(openedFileUri, "wt")
        viewModel.saveOverwrite(openedFileUri, stream)
    }

    private fun confirmNewFile() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("새 문서")
            .setMessage("새 문서 작성을 위해 초기화합니다.")
            .setPositiveButton("확인") { dialog, which ->
                viewModel.clear()
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }
}