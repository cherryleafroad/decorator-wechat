package com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.preference.PreferenceManager
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.ReplyIntent
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.MessageWithAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.User
import com.oasisfeng.nevo.decorators.wechat.chatHistory.repository.DatabaseRepository
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class ChatData(
    val title: String,
    val uid: Long,
    val messageData: LiveData<PagingData<MessageWithAvatar>>
)

data class SelfUserData(
    var avatar: Drawable?,
    var avatarEnabled: Boolean
)

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseRepository = DatabaseRepository(AppDatabase.get(application))

    val userList = databaseRepository.userlist

    lateinit var selfUserData: SelfUserData

    // saving drafts for the user
    val drafts = ArrayMap<Long, String>()

    // single event used for getting intent AFTER observation if it's not in the map
    val replyIntentEmitter: EventEmitter<ReplyIntent> = EventEmitter()
    val replyIntentEvent: EventSource<ReplyIntent> = replyIntentEmitter

    val replyIntents = ArrayMap<Long, ReplyIntent>()


    val chatData: LiveData<ChatData?>
        get() = _chatData
    // Data for the selected chat we will pass to ChatFragment
    private var _chatData: MutableLiveData<ChatData?> = MutableLiveData()

    // used for when theme changing restarts chat activity
    var restartedChat = false
    var inputData = ""

    private var config: PagingConfig = PagingConfig(
        initialLoadSize = 30,
        pageSize = 25,
        prefetchDistance = 25,
        enablePlaceholders = false,
        maxSize = 150
    )


    init {
        refreshUserList()
        loadDrafts()

        viewModelScope.launch(Dispatchers.Main) {
            var userAvatar: Drawable? = null
            val job = viewModelScope.launch(Dispatchers.IO) {
                val avatarFilename = getSelfAvatarFilename()
                if (avatarFilename.isNotEmpty()) {
                    userAvatar = BitmapDrawable(
                        application.applicationContext.resources,
                        BitmapFactory.decodeFile(avatarFilename)
                    )
                }
            }

            job.join()

            val preferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
            val userAvatarEnabled = preferences.getBoolean(
                application.applicationContext.getString(R.string.pref_enable_avatar),
                true
            )

            selfUserData = SelfUserData(
                userAvatar,
                userAvatarEnabled
            )
        }
    }

    private fun loadDrafts() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.getAllDrafts().forEach { drafts[it.user_id] = it.message }
        }
    }

    fun saveDraft(uid: Long, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.saveDraft(uid, message)
        }
    }

    fun deleteDraft(uid: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteDraft(uid)
        }
    }

    fun setChatData(uid: Long, title: String) {
        val items = Pager(config) {
            databaseRepository.getMessages(uid)
        }.liveData.cachedIn(this)

        _chatData.value = ChatData(title, uid, items)
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun clearChatData() {
        _chatData.value = null
    }

    fun deleteUser(uid: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteUser(uid)
            _refreshUserList()
        }
    }

    fun deleteAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteAllUsers()
            _refreshUserList()
        }
    }

    fun refreshUserList() {
        viewModelScope.launch(Dispatchers.IO) {
            _refreshUserList()
        }
    }

    suspend fun getUserSelf(): User {
        return databaseRepository.getUserSelf()
    }

    suspend fun getSelfAvatarFilename(): String {
        return databaseRepository.getSelfAvatarFilename()
    }

    suspend fun saveAvatar(avatar: Avatar) {
        databaseRepository.saveAvatar(avatar)
    }

    suspend fun getSidFromUid(uid: Long): String {
        return databaseRepository.getSidFromUid(uid)
    }

    @Suppress("FunctionName")
    private suspend fun _refreshUserList() {
        databaseRepository.refreshUserlist()
    }
}
