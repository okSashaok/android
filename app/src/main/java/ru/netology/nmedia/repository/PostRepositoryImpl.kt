package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    private var newerPosts = emptyList<PostEntity>()
    override val data = dao.getAll().map {
        it.map {
            it.toDto()
        }
    }.flowOn(Dispatchers.Default)

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

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            val response = PostsApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            newerPosts = body.toEntity()
            emit(body.size)
        }
    }.catch { e ->
        throw AppError.from(e)
    }

    override suspend fun save(post: Post, image: File?) {
        try {
            val media = image?.let {
                upload(it)
            }
            val postWithAttachment = media?.let {
                post.copy(attachment = Attachment(url = it.id, AttachmentType.IMAGE))
            } ?: post
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

    private suspend fun upload(file: File): Media =
        PostsApi.service.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )

    override suspend fun saveNewer() {
        if (newerPosts.isNotEmpty()) {
            dao.insert(newerPosts)
            newerPosts = emptyList()
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        try {
            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
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
            response = if (likedByMe) {
                PostsApi.service.likeById(id)
            } else {
                PostsApi.service.dislikeById(id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            serverPost = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(serverPost))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw e as? AppError ?: UnknownError
        }
    }
}
