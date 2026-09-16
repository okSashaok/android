package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun getAll()
    suspend fun save(post: Post, image: File?)
    suspend fun saveNewer()
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
}
