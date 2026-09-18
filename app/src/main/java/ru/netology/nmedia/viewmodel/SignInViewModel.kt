package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.model.ModelState

class SignInViewModel : ViewModel() {
    private val appAuth = AppAuth.getInstance()
    private val _successSignIn = MutableLiveData<Unit>()
    val successSignIn: LiveData<Unit>
        get() = _successSignIn
    private val _dataState = MutableLiveData<ModelState>()
    val dataState: LiveData<ModelState>
        get() = _dataState

    fun signIn(login: String, pass: String) {
        if(login.isBlank() || pass.isBlank()){
            _dataState.value = ModelState(errorEvent = R.string.error_empty_fields)
            return
        }
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                val response = PostsApi.service.updateUser(login, pass)
                _dataState.value = ModelState(loading = false)
                val authResponse = response.body()
                if (response.isSuccessful && authResponse != null) {
                    appAuth.setAuth(authResponse.token, authResponse.id)
                    _successSignIn.value = Unit
                } else {
                    _dataState.value = ModelState(errorEvent = R.string.error_login_or_password)
                }
            } catch (e: Exception) {
                _dataState.value = ModelState(errorEvent = R.string.error_authorization)
            }
        }
    }
}