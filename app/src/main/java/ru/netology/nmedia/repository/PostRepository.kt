package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun save(post: Post, image: File?)
    suspend fun saveNewer()
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
}
