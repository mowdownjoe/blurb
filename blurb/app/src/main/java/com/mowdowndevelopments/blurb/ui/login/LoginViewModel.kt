package com.mowdowndevelopments.blurb.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.BlurbApp
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.responseModels.AuthResponse
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    val loginStatus: LiveData<LoadingStatus>
        get() = _loginStatus
    private val _errorToast: MutableLiveData<String> = MutableLiveData()
    val errorToast: LiveData<String>
        get() = _errorToast

    fun login(username: String, password: String) {
        if (_loginStatus.value === LoadingStatus.LOADING) return
        viewModelScope.launch {
            _loginStatus.postValue(LoadingStatus.LOADING)
            try {
                val response = getNewsBlurAPI(getApplication()).login(username, password)
                checkLoginAuthResponse(response)
            } catch (e: Exception) {
                authFailure(e)
            }
        }
    }

    fun login(username: String) {
        if (_loginStatus.value === LoadingStatus.LOADING) return
        viewModelScope.launch {
            _loginStatus.postValue(LoadingStatus.LOADING)
            try {
                val response = getNewsBlurAPI(getApplication()).login(username)
                checkLoginAuthResponse(response)
            } catch (e: Exception) {
                authFailure(e)
            }
        }
    }

    private fun checkLoginAuthResponse(response: Response<AuthResponse>) {
        val authenticated = response.body()?.isAuthenticated
        when {
            response.isSuccessful && authenticated!! -> _loginStatus.postValue(LoadingStatus.DONE)
            response.isSuccessful && !authenticated!! -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                _errorToast.postValue(getApplication<BlurbApp>().getString(R.string.bad_credential_error))
                Timber.w("loginCallback.onResponse: User entered incorrect credentials.")
            }
            !response.isSuccessful -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                //TODO Add messages to explain HTTP Error codes in plain language
                val errorMsg = getApplication<BlurbApp>().getString(R.string.http_error, response.code())
                _errorToast.postValue(errorMsg)
                Timber.e("loginCallback.onResponse: $errorMsg")
            }
            else -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                val errorMsg = getApplication<BlurbApp>().getString(R.string.msg_unknown_error)
                _errorToast.postValue(errorMsg)
                Timber.e("loginCallback.onResponse: $errorMsg")
            }
        }
    }

    fun registerNewAccount(username: String, password: String, emailAddress: String) {
        if (_loginStatus.value === LoadingStatus.LOADING) return
        viewModelScope.launch {
            _loginStatus.postValue(LoadingStatus.LOADING)
            try {
                val response = getNewsBlurAPI(getApplication()).signup(username, password, emailAddress)
                checkRegisterAuthResponse(response)
            } catch (e: Exception) {
                authFailure(e)
            }
        }
    }

    fun registerNewAccount(username: String, emailAddress: String) {
        if (_loginStatus.value === LoadingStatus.LOADING) return
        viewModelScope.launch {
            _loginStatus.postValue(LoadingStatus.LOADING)
            try {
                val response = getNewsBlurAPI(getApplication()).signup(username, emailAddress)
                checkRegisterAuthResponse(response)
            } catch (e: Exception) {
                authFailure(e)
            }
        }
    }

    private fun checkRegisterAuthResponse(response: Response<AuthResponse>) {
        val authenticated = response.body()?.isAuthenticated
        when {
            response.isSuccessful && authenticated == true -> _loginStatus.postValue(LoadingStatus.DONE)
            response.isSuccessful && authenticated == false -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                _errorToast.postValue(getApplication<Application>().getString(R.string.bad_registration_error))
                Timber.w("registrationCallback.onResponse: User entered incorrect credentials.")
            }
            !response.isSuccessful -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                //TODO Add messages to explain HTTP Error codes in plain language
                val errorMsg = getApplication<BlurbApp>().getString(R.string.http_error, response.code())
                _errorToast.postValue(errorMsg)
                Timber.e("registrationCallback.onResponse: $errorMsg")
            }
            else -> {
                _loginStatus.postValue(LoadingStatus.ERROR)
                val errorMsg = getApplication<Application>().getString(R.string.msg_unknown_error)
                _errorToast.postValue(errorMsg)
                Timber.e("registrationCallback.onResponse: $errorMsg")
            }
        }
    }

    private fun authFailure(e: Exception) {
        _loginStatus.postValue(LoadingStatus.ERROR)
        Timber.e(e, "onFailure: %s", e.message)
        _errorToast.postValue(e.localizedMessage)
        FirebaseCrashlytics.getInstance().log(String.format("onFailure: %s", e.message))
        FirebaseCrashlytics.getInstance().recordException(e)
    }

}