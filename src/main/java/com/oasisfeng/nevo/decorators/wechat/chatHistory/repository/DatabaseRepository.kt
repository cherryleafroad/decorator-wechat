package com.oasisfeng.nevo.decorators.wechat.chatHistory.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Draft
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.MessageWithAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.UserWithMessageAndAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DatabaseRepository(private val database: AppDatabase) {
    private val _userlist: MutableLiveData<List<UserWithMessageAndAvatar>> = MutableLiveData()
    val userlist: LiveData<List<UserWithMessageAndAvatar>>
        get() = _userlist

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