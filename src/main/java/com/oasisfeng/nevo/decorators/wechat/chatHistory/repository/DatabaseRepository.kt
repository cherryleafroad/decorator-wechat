package com.oasisfeng.nevo.decorators.wechat.chatHistory.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.*

class DatabaseRepository(private val database: AppDatabase) {
    private val _userlist: MutableLiveData<List<UserWithMessageAndAvatar>> = MutableLiveData()
    val userlist: LiveData<List<UserWithMessageAndAvatar>>
        get() = _userlist

    suspend fun getSidFromUid(uid: Long): String {
        return database.userDao().getSidFromUid(uid)
    }

    suspend fun getUserSelf(): User {
        return database.userDao().getUserSelf()
    }

    suspend fun getSelfAvatarFilename(): String {
        return database.avatarDao().getSelfAvatar()
    }

    suspend fun saveAvatar(avatar: Avatar) {
        database.avatarDao().update(avatar)
    }

    suspend fun getAllDrafts(): List<Draft> {
        return database.draftDao().getAllDrafts()
    }

    suspend fun saveDraft(uid: Long, message: String) {
        val draft = Draft(uid, message)
        database.draftDao().save(draft)
    }

    suspend fun deleteDraft(uid: Long) {
        database.draftDao().delete(uid)
    }

    fun getMessages(uid: Long): PagingSource<Int, MessageWithAvatar> {
        return database.messageDao().getMessagesWithAvatar(uid)
    }

    suspend fun refreshUserlist() {
        _userlist.postValue(database.userDao().getUsersWithMessageAndAvatar())
    }

    suspend fun deleteUser(uid: Long) {
        database.userDao().deleteByUid(uid)
    }

    suspend fun deleteAllUsers() {
        database.userDao().deleteAll()
    }
}