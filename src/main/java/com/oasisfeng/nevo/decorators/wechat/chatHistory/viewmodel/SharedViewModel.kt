package com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.MessageWithAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.repository.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class ChatData(
    val title: String,
    val uid: Long,
    val messageData: LiveData<PagingData<MessageWithAvatar>>
)

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseRepository = DatabaseRepository(AppDatabase.get(application))

    val userList = databaseRepository.userlist

    val chatData: LiveData<ChatData>
        get() = _chatData
    // Data for the selected chat we will pass to ChatFragment
    private var _chatData: MutableLiveData<ChatData> = MutableLiveData()

    private var config: PagingConfig = PagingConfig(
        initialLoadSize = 30,
        pageSize = 25,
        prefetchDistance = 25,
        enablePlaceholders = false,
        maxSize = 150
    )

    fun setChatData(uid: Long, title: String) {
        val items = Pager(config) {
            databaseRepository.getMessages(uid)
        }.liveData.cachedIn(this)

        _chatData.value = ChatData(title, uid, items)
    }

    fun clearChatData() {
        _chatData.value = null
    }

    fun deleteUser(uid: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            databaseRepository.deleteUser(uid)
            refreshUserList()
        }
    }

    suspend fun deleteAllUsers() {
        databaseRepository.deleteAllUsers()
    }

    fun refreshUserList() {
        GlobalScope.launch(Dispatchers.IO) {
            databaseRepository.refreshUserlist()
        }
    }
}
