package ru.netology.nmedia.authorization

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<Token?>(null)
    val state = _state.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)
        if (token != null && id != 0L) {
            _state.value = Token(id, token)
        }
        else{
            prefs.edit{
                clear()
            }
        }
        sendPushToken()
    }

    fun setAuth(token: String, id: Long) {
        _state.value = Token(id, token)
        prefs.edit() {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }
        sendPushToken()
    }

    fun clearAuth() {
        _state.value = null
        prefs.edit {
            clear()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null){
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                PostsApi.service.sendPushToken(
                    PushToken(FirebaseMessaging.getInstance().token.await())
                )
            }
        }
    }

    companion object {
        private var instance: AppAuth? = null
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"
        fun getInstance(): AppAuth = requireNotNull(instance) {
            "Need init() first"
        }

        fun init(context: Context) {
            instance = AppAuth(context.applicationContext)
        }
    }
}