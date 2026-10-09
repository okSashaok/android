package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.dto.Token
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {
    val state: LiveData<Token?> = appAuth.state.asLiveData(Dispatchers.Default)
    val isAuthorized: Boolean
        get() = appAuth.state.value?.id != 0L
}