package ru.netology.nmedia.authorization

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    }

    fun setAuth(token: String, id: Long) {
        _state.value = Token(id, token)
        prefs.edit() {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }
    }

    fun clearAuth() {
        _state.value = null
        prefs.edit {
            clear()
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