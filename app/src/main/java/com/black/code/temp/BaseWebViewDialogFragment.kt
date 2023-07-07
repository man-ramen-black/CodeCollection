package com.netmarble.nmapp.base.component

import androidx.databinding.ViewDataBinding
import com.netmarble.nmapp.webkit.NMWebView

/**
 * Created by jinhyuk.lee on 2022/05/20
 **/
abstract class BaseWebViewDialogFragment<T : ViewDataBinding> : BaseDialogFragment<T>() {
    protected abstract val webView: NMWebView

    override fun onBackPressed() {
        when {
            webView.isShowingCustomView() -> {
                webView.hideCustomView()
            }
            webView.canGoBack() -> {
                webView.goBack()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        webView.onDestroy()
        super.onDestroyView()
    }
}