package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao

import androidx.room.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Draft

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts")
    suspend fun getAllDrafts(): List<Draft>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(draft: Draft)

    @Query("DELETE from drafts WHERE user_id = :user_id")
    suspend fun delete(user_id: Long)
}
