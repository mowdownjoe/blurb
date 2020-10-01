package com.mowdowndevelopments.blurb.ui.feeds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mowdowndevelopments.blurb.network.LoadingStatus

abstract class BaseFeedViewModel(application: Application) : AndroidViewModel(application) {
    private val _loadingStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    open val loadingStatus: LiveData<LoadingStatus>
        get() = _loadingStatus
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    open val errorMessage: LiveData<String>
        get() = _errorMessage

    fun setErrorMessage(message: String) {
        _errorMessage.postValue(message)
    }

    fun setLoadingStatus(status: LoadingStatus) {
        _loadingStatus.postValue(status)
    }

}