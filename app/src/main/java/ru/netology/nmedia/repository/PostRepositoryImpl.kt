package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    private var newerPosts = emptyList<PostEntity>()

    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            dao.getPagingSource()
        },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        )
    ).flow.map {
        it.map(PostEntity::toDto)
    }

    override suspend fun save(post: Post, image: File?) {
        try {
            val media = image?.let {
                upload(it)
            }
            val postWithAttachment = media?.let {
                post.copy(attachment = Attachment(url = it.id, AttachmentType.IMAGE))
            } ?: post
            val response = apiService.save(post)
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
        apiService.upload(
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
            val response = apiService.removeById(id)
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
            var response = apiService.getById(id);
            var serverPost = response.body() ?: throw ApiError(response.code(), response.message())
            if (likedByMe == serverPost.likedByMe) return
            response = if (likedByMe) {
                apiService.likeById(id)
            } else {
                apiService.dislikeById(id)
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
