package com.mowdowndevelopments.blurb.ui.navHost

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.responseModels.AutoCompleteResponse
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _autoCompleteDialogData: MutableLiveData<List<AutoCompleteResponse>?> = MutableLiveData()
    val autoCompleteDialogData: LiveData<List<AutoCompleteResponse>?>
        get() = _autoCompleteDialogData
    private val _logoutStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    val logoutStatus: LiveData<LoadingStatus>
        get() = _logoutStatus
    private val _inAppDialogStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    val inAppDialogStatus: LiveData<LoadingStatus>
        get() = _inAppDialogStatus
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var loadingForDialog = false
    private var purchaseRetryAttempts = 0

    fun resetRetryAttempts() {
        purchaseRetryAttempts = 0
    }

    fun incrementRetryAttempts() {
        ++purchaseRetryAttempts
    }

    fun canKeepRetryingPurchase(): Boolean = purchaseRetryAttempts < MAX_RETRY_ATTEMPTS

    fun postNewErrorMessage(message: String) = _errorMessage.postValue(message)

    fun postInAppDialogStatus(status: LoadingStatus) = _inAppDialogStatus.postValue(status)

    fun clearAutoCompleteDialogData() {
        _autoCompleteDialogData.value = null
    }

    fun logout() {
        _logoutStatus.postValue(LoadingStatus.LOADING)
        viewModelScope.launch {
            try {
                val c = getApplication<Application>().applicationContext
                val response = getNewsBlurAPI(c).logout()
                if (response.isSuccessful) {
                    _logoutStatus.postValue(LoadingStatus.DONE)
                    c.getSharedPreferences(c.getString(R.string.shared_pref_file), 0).edit {
                        putBoolean(c.getString(R.string.logged_in_key), false) }
                } else {
                    _logoutStatus.postValue(LoadingStatus.ERROR)
                    //TODO Add messages to explain HTTP Error codes in plain language
                    val errorMsg = c.getString(R.string.http_error, response.code())
                    _errorMessage.postValue(errorMsg)
                    Timber.e("onResponse: %s", errorMsg)
                }
            } catch (e: Exception) {
                _logoutStatus.postValue(LoadingStatus.ERROR)
                Timber.e(e, "onFailure: %s", e.message)
                _errorMessage.postValue(e.localizedMessage)
                FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", e.message))
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    //Used for NewFeedDialogFragment;
    fun loadDataForFeedAutoComplete(searchTerm: String?) {
        if (loadingForDialog) return
        loadingForDialog = true
        viewModelScope.launch {
            try {
                val response = getNewsBlurAPI(getApplication())
                        .getAutoCompleteResults(requireNotNull(searchTerm))
                if (response.isSuccessful) {
                    _autoCompleteDialogData.postValue(response.body())
                }
            } catch (e: Exception) { //Fail silently
            } finally {
                loadingForDialog = false
            }
        }
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 10
    }

}