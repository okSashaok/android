package ru.netology.nmedia.repository

import androidx.lifecycle.*
import com.google.android.gms.common.api.Api
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException){
            throw NetworkError
        } catch (e: Exception){
            throw e as? AppError ?: UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            dao.toggleLikeById(id)
            val likedByMe = dao.isLikedById(id)
            var response = PostsApi.service.getById(id);
            var serverPost = response.body() ?: throw ApiError(response.code(), response.message())
            if (likedByMe == serverPost.likedByMe) return
            response = if(likedByMe){
                PostsApi.service.likeById(id)
            } else {
                PostsApi.service.dislikeById(id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            serverPost = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(serverPost))
        } catch (e: IOException){
            throw NetworkError
        } catch (e: Exception){
            throw e as? AppError ?: UnknownError
        }
    }
}
