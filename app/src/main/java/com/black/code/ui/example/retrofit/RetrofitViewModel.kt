package com.black.code.ui.example.retrofit

import androidx.lifecycle.MutableLiveData
import com.black.code.base.viewmodel.EventViewModel
import com.black.code.model.network.NetworkResult
import com.black.code.model.network.sample.NetworkSampleModel

/**
 * [RetrofitFragment]
 * Created by jinhyuk.lee on 2022/05/02
 **/
class RetrofitViewModel : EventViewModel() {
    val errorCode = MutableLiveData(0)
    val statusCode = MutableLiveData(0)
    val errorMessage = MutableLiveData("")
    val data = MutableLiveData("")
    val albumId = MutableLiveData(0)

    private var model: NetworkSampleModel? = null

    fun setModel(model: NetworkSampleModel) {
        this.model = model
    }

    private fun <T> setResponse(result: NetworkResult<List<T>>) {
        errorCode.value = result.code
        statusCode.value = result.statusCode
        errorMessage.value = result.message
        data.value = result.data?.joinToString(",\n") ?: "null"
    }

    fun onClickGetAlbums() {
        model?.loadAlbums(this::setResponse)
    }

    fun onClickGetAlbumsWrongResponse() {
        model?.loadAlbumsWrongResponse(this::setResponse)
    }

    fun onClickGetPhoto() {
        model?.loadPhotos(albumId.value ?: 0, this::setResponse)
    }
}