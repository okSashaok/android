package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT likedByMe FROM PostEntity WHERE id = :id")
    suspend fun isLikedById(id: Long): Boolean

    @Query("""
        UPDATE PostEntity 
        SET likes = CASE WHEN likedByMe = 1 THEN likes - 1 ELSE likes + 1 END,
            likedByMe = CASE WHEN likedByMe = 1 THEN 0 ELSE 1 END 
        WHERE id = :id
    """)
    suspend fun toggleLikeById(id: Long)
}
