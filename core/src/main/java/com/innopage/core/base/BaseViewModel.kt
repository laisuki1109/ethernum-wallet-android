package com.innopage.core.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.innopage.core.webservice.config.Constants
import com.innopage.core.webservice.model.Error
import com.innopage.core.webservice.model.ErrorField
import com.innopage.core.webservice.retrofit.exception.ErrorException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val _status = MutableLiveData<Constants.Status>()
    val status: LiveData<Constants.Status>
        get() = _status

    protected val _error = MutableLiveData<Error?>()
    val error: LiveData<Error?>
        get() = _error

    protected val _errorFields = MutableLiveData<List<ErrorField>?>()
    val errorFields: LiveData<List<ErrorField>?>
        get() = _errorFields

    fun ioLaunch(enableLoadingDialog: Boolean = true, block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (enableLoadingDialog) _status.postValue(Constants.Status.LOADING)
                    block()
                    _status.postValue(Constants.Status.DONE)
                } catch (e: ErrorException) {
                    _error.postValue(e.error)
                    _errorFields.postValue(e.errorFields)
                    _status.postValue(Constants.Status.ERROR)
                }
            }
        }
    }

    fun resetError() {
        _error.value = null
    }

    fun resetErrorFields() {
        _errorFields.value = null
    }
}