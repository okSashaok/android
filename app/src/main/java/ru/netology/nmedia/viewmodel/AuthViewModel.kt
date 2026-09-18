package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.dto.Token

class AuthViewModel : ViewModel() {
    val state: LiveData<Token?> = AppAuth.getInstance().state.asLiveData(Dispatchers.Default)
    val isAuthorized: Boolean
        get() = AppAuth.getInstance().state.value?.id != 0L
}