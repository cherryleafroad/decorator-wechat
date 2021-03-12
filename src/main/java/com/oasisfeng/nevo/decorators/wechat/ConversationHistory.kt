package com.oasisfeng.nevo.decorators.wechat

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.ChatHistoryActivity.Companion.EXTRA_USER_ID
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.*
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.UserActivity.Companion.ACTION_NOTIFY_USER_CHANGE
import kotlinx.coroutines.*
import kotlin.properties.Delegates

internal object ConversationHistory {
    const val MAX_NUM_CONVERSATIONS = 10
    private val mConversationHistory = ArrayMap<String, ArrayList<String?>>()
    private val mUnreadCount = ArrayMap<String, Int>()
    private val mUnreadOffset = ArrayMap<String, Int>()
    private val mReplyFlag = ArrayMap<String, Int>()
    private lateinit var mDb: AppDatabase
    private var mChatHistoryEnabled by Delegates.notNull<Boolean>()

    private fun handleRecalledMessage(
        key: String,
        message: String,
        context: Context,
        car_messages: ArrayList<String>,
        isGroupChat: Boolean,
        user: User
    ) {
        // Please note that it is IMPOSSIBLE to differentiate which message was recalled
        // when both messages content is exactly the same. This is an unfortunate side effect
        // and there's no way to solve it as carExtenderMessages only tells us the missing ones (not which one it was)

        // we MUST slice this to prevent previously returned history (that we aren't showing) from
        // influencing the data
        val history = ArrayList(mConversationHistory[key]).subList(
            0, (mUnreadCount[key]!!).coerceAtMost(MAX_NUM_CONVERSATIONS)
        )

        if (isGroupChat) {
            // check back history
            for (i in history.indices) {
                if (history[i] == message) {
                    saveRecalledMsg(context, key, history, i, user, isGroupChat)
                    return
                }
            }
        } else {
            //
            // Check for when there's only 1 message
            //
            if (history.size == 1 && car_messages.size == 0 && !history[0]!!.startsWith(context.getString(R.string.recalled_message))) {
                saveRecalledMsg(context, key, history, 0, user, isGroupChat)
                return
            } else if (history.size == 1 && car_messages.size == 0) {
                // already has the recalled entry
                return
            }


            // Here we check the valid indexes by removing invalid ones, if there's only 1 left
            // then that's the correct one
            //
            // the remaining indexes in history that could be the potential recalled message
            val validIndices: MutableList<Int> = ArrayList()
            // initialize all potential indexes
            validIndices.addAll(0 until history.size)

            // remove invalid indexes from List
            for (i in 0 until history.size) {
                // any history message contained in messages is invalid as it wasn't removed
                // from messages. Also, already recalled message indexes are also invalid
                if (car_messages.contains(history[i]) ||
                    history[i]!!.startsWith(context.getString(R.string.recalled_message))
                ) {
                    // remove invalid index
                    validIndices.remove(i)
                }
            }

            // found the exact one!
            if (validIndices.size == 1) {
                val index = validIndices[0]
                saveRecalledMsg(context, key, history, index, user, isGroupChat)
                return
            }
            //
            // /End
            //


            //
            // Two or more duplicate elements were found
            //

            // get the duplicates indices
            val dupIndices: MutableList<Int> = ArrayList()
            // initialize all potential indices
            dupIndices.addAll(0 until history.size)
            // remove one's that don't match
            for ((i, msg) in history.withIndex()) {
                val c = history.count { it == msg }
                if (c < 2) dupIndices.remove(i)
            }



            ///
            /// /End
            ///
        }

        // this point should never be reached
        Log.d(TAG, "handleRecalledMessage() reached unreachable point")
    }

    private fun saveRecalledMsg(context: Context, key: String, history: MutableList<String?>, index: Int, user: User, isGroupChat: Boolean) {
        val visible = (context.applicationContext as WeChatApp).sharedPreferences?.getBoolean(context.getString(R.string.pref_recalled), false)

        val sender = splitSender(history[index])

        val newMsg: String = if (visible == true) {
            // [Recalled] Message
            if (isGroupChat) {
                "${sender[0]}: ${context.getString(R.string.recalled_message)} ${sender[1]}"
            } else {
                "${context.getString(R.string.recalled_message)} ${history[index]}"
            }
        } else {
            if (isGroupChat) {
                "${sender[0]}: ${context.getString(R.string.recalled_message)}"
            } else {
                context.getString(R.string.recalled_message)
            }
        }

        var databaseMsg = history[index]!!
        history[index] = newMsg
        mConversationHistory[key] = ArrayList(history)

        if (mChatHistoryEnabled) {
            GlobalScope.launch(Dispatchers.IO) {
                val username: String
                if (visible == true) {
                    if (isGroupChat) {
                        databaseMsg = context.getString(R.string.message_header_recalled_visible)
                            .replace("%s", sender[0].toString())
                            .replace("%m", sender[1].toString())
                        username = sender[0]!!
                    } else {
                        databaseMsg = context.getString(R.string.message_header_recalled_visible)
                            .replace("%m", databaseMsg)
                            .replace("%s", user.username)
                        username = user.username
                    }
                } else {
                    if (isGroupChat) {
                        databaseMsg = context.getString(R.string.message_header_recalled_invisible).replace("%s", sender[0].toString())
                        username = sender[0]!!
                    } else {
                        databaseMsg = context.getString(R.string.message_header_recalled_invisible).replace("%s", user.username)
                        username = user.username
                    }
                }

                val us = mDb.userDao().findByUsername(username)!!

                val dbHistory = mDb.messageDao().getAllMessagesByUserLimitDesc(us, mUnreadCount[key]!!)
                dbHistory[index].message = databaseMsg
                dbHistory[index].message_type = MessageType.RECALLED
                mDb.messageDao().update(dbHistory[index])

                notifyChatUiChangedData(context, us.u_id)
            }
        }
    }

    // modify in place
    private fun <K, V> ArrayMap<K, V>.mapInPlace(transform: (V) -> V) {
        for (k in this.keys) {
            this[k] = transform(this[k]!!)
        }
    }

    @JvmStatic
    fun markReadAll() {
        mUnreadCount.mapInPlace { 0 }
        mUnreadOffset.mapInPlace { 0 }
    }

    @JvmStatic
    fun getUnreadCount(key: String): Int {
        return mUnreadCount[key] ?: 0
    }

    // returns the sender and message separately
    private fun splitSender(message: CharSequence?): Array<String?> {
        val returnStr = arrayOfNulls<String>(2)
        var pos: Int
        if (TextUtils.indexOf(message, ':', 1).also { pos = it } > 0) {
            val sender = removeUnreadCount(message!!.subSequence(0, pos))
            val msg = message.subSequence(pos + 2, message.length)
            returnStr[0] = sender.toString()
            returnStr[1] = msg.toString()
        }
        return returnStr
    }

    private fun addConversationMessage(key: String, message: String?) {
        val conversation: ArrayList<String?>
        if (mConversationHistory.containsKey(key)) {
            conversation = mConversationHistory[key]!!
            conversation.add(0, message)
            if (conversation.size > MAX_NUM_CONVERSATIONS) {
                conversation.removeAt(conversation.size - 1)
            }
        } else {
            conversation = ArrayList()
            conversation.add(message)
        }
        mConversationHistory[key] = conversation
    }

    private fun extractNotificationMessages(
        notifications: List<StatusBarNotification>,
        isGroupChat: Boolean
    ): MutableList<String?> {
        val messages: MutableList<String?> = ArrayList()
        for (i in notifications.indices) {
            val notification = notifications[i].notification
            val itsExtras = notification.extras
            val itsText = itsExtras.getString(Notification.EXTRA_TEXT)
            if (itsText == null) {
                Log.w(TAG, "No text in archived notification.")
                messages.add("[Unknown]")
                continue
            }
            val split = splitSender(itsText)
            val msg: String? = if (!isGroupChat) {
                split[1]
            } else {
                "${split[0].toString()}: ${split[1]}"
            }
            messages.add(msg)
        }
        return messages
    }

    @JvmStatic
    fun markAsRead(key: String) {
        mUnreadCount[key] = 0
        mUnreadOffset[key] = 0
    }

    private fun removeUnreadCount(message: CharSequence): CharSequence {
        val contentLength = message.length
        // need to remove unread count from message for ticker
        var pos = 0
        if (contentLength > 3 && message[0] == '[' && TextUtils.indexOf(message, ']', 1)
                .also { pos = it } > 0
        ) {
            val prefix = message.subSequence(1, pos)
            val length = prefix.length
            val count =
                if (length > 1 && !Character.isDigit(prefix[length - 1])) prefix.subSequence(
                    0,
                    length - 1
                ) else prefix

            // see if it's a number, otherwise we want the full message
            return try {
                count.toString().toInt()
                message.subSequence(pos + 1, message.length)
            } catch (ignored: NumberFormatException) {     // Probably just emoji like "[Cry]"
                Log.d(TAG, "Failed to parse as int: $prefix")
                message
            }
        }
        return message
    }

    private fun notifyChatUiChangedData(context: Context, uid: Long) {
        // notify of any new messages if you're in the activity
        val intent = Intent(ACTION_NOTIFY_USER_CHANGE)
        intent.putExtra(EXTRA_USER_ID, uid)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    // The meat and bones right here. it maintains conversation history
    // adds the new one, replaces and fixes the carExtenders conversations
    // AND returns the fixed unreadConversation from the car extender
    //
    // The purpose of this class is to fix an issue where WeChat sends "[Message]"
    // when you do certain actions like quote a message, which makes it unusable
    @JvmStatic
    fun getUnreadConversation(
        context: Context,
        key: String,
        unreadConversation: Notification.CarExtender.UnreadConversation,
        conversation: ConversationManager.Conversation,
        notificationHistory: List<StatusBarNotification>,
        isRecalled: Boolean,
        isDuplicate: Boolean
    ): Notification.CarExtender.UnreadConversation {
        mChatHistoryEnabled =
            (context.applicationContext as WeChatApp).sharedPreferences!!.getBoolean(
                context.getString(R.string.pref_chat_history), false
            )

        // only get db if it's relevant
        if (!this::mDb.isInitialized && mChatHistoryEnabled) {
            mDb = (context.applicationContext as WeChatApp).db
        }

        if (conversation.ticker == null) {
            conversation.ticker = removeUnreadCount(conversation.summary)
        }

        val isReplying = (context.applicationContext as WeChatApp).replying
        var shouldSkip = isDuplicate
        if ((context.applicationContext as WeChatApp).isRecasted) {
            // reset flag to false
            (context.applicationContext as WeChatApp).isRecasted = false
            shouldSkip = true
        }

        // create keys for new conversation if not here
        if (!mConversationHistory.containsKey(key)) {
            mConversationHistory[key] = ArrayList()
            mUnreadCount[key] = 0
            mUnreadOffset[key] = 0
            mReplyFlag[key] = 0
        }

        if (mReplyFlag[key] == 1 && !isReplying) {
            // we can now reset the history because we're done replying
            markAsRead(key)
            mReplyFlag[key] = 0
        }

        val isGroupChat = conversation.isGroupChat


        // A simple unread message counter
        // Only will reset when we clear the notification
        // this simple way of counting is more reliable than
        // relying on the apps notification counter which is notoriously unreliable (specifically
        // when dealing with recalled messages)
        // -> This is used to tell us how many messages we should display

        // Don't create extra notifications when replying or recalling a message
        // this is a regular message
        if (!isReplying && !isRecalled && !shouldSkip) {
            mUnreadCount[key] = mUnreadCount[key]?.plus(1)
        } else if (!isReplying && isRecalled && !shouldSkip) {
            mUnreadOffset[key] = mUnreadOffset[key]!! + 1
        }

        // unread count will be set to 0 respectively when we clear the notification
        // recalled messages are simply skipped as they are the same message
        // see @WeChatDecorator.onNotificationRemoved

        // only MAX is allowed
        val unreadCount = (mUnreadCount[key]!!).coerceAtMost(MAX_NUM_CONVERSATIONS)
        conversation.count = unreadCount

        // this part serves the purpose of :
        // fixing the conversation ticker and summary when isRecalled is true
        // and it's in a group ->
        // Sadly, can't figure out the proper single chat fix here, but
        // unread car extender messages will have the missing message
        // so we can pinpoint the deleted one
        if (isGroupChat && isRecalled) {
            // update conversation fields to be correct
            val senderS = splitSender(conversation.summary)
            val senderT = splitSender(conversation.ticker)
            if (unreadCount > 0) {
                // rebuild correct message
                conversation.summary = "[$unreadCount]${senderT[0]}: ${senderS[1]}"
            } else {
                conversation.summary = "${senderT[0]}: ${senderS[1]}"
            }
            // rebuild correct message
            conversation.ticker = "${senderT[0]}: ${senderS[1]}"
        } else {
            // fix counter in messages
            val split: Array<String?> = splitSender(conversation.ticker)

            // fix the fields to make sure they're correct, also replaces the count
            if (unreadCount > 0) {
                conversation.summary = "[$unreadCount]${split[0]}: ${split[1]}"
            } else {
                conversation.summary = "${split[0]}: ${split[1]}"
            }
        }


        // car extender messages are ordered from oldest to newest
        var carExtenderMessages = arrayListOf<String>(*unreadConversation.messages)

        // carextender missing data for some reason? strange
        val isCarMessagesEmpty = carExtenderMessages.isEmpty()
        if (isCarMessagesEmpty && !isRecalled && !shouldSkip) {
            val split = splitSender(conversation.ticker)
            carExtenderMessages.add(0, split[1] ?: "[Unknown]")
        }

        // the length of messages should be mUnread - mOffset
        // if messages have more than that, that means the car bundle returned more than it was
        // supposed to, and we need to chop off the extra : oldest -> newest
        if (!shouldSkip) {
            val extra = carExtenderMessages.size - (mUnreadCount[key]!! - mUnreadOffset[key]!!)
            if (extra > 0 && extra < carExtenderMessages.size) {
                // only view the proper amount of data
                carExtenderMessages =
                    ArrayList(carExtenderMessages.subList(extra, carExtenderMessages.size))
            }
        }
        // sort into newest -> oldest
        carExtenderMessages.reverse()


        val messages = mConversationHistory[key]!!
        // if it's an erroneous message, go to fallback, otherwise use original
        // make sure to grab the latest which is the last one
        var msgCheck: String? = ""
        val username = if (!isGroupChat) {
            EmojiTranslator.translate(splitSender(conversation.ticker)[0]).toString()
        } else {
            conversation.title.toString()
        }

        if (!isRecalled && !shouldSkip && !isReplying) {
            msgCheck = if (!isGroupChat) {
                // Single chat or Bot
                carExtenderMessages[0]
            } else {
                // this is a group chat
                // need to slice off -> Name: Msg -> Msg
                splitSender(carExtenderMessages[0])[1]
            }

            // Replying has a double entry, so don't add twice it if we're replying
            // this means we're only getting the history, not adding to it
            // And recalling messages do not have any extra message to add
            if (msgCheck != "[Message]") {
                // car extender has the correct msg in both cases
                // Name: Msg, for groups
                // for regular chat just, Msg
                addConversationMessage(key, carExtenderMessages[0])
            } else {
                // fallback, real message is in the ticker
                val msg: String? = if (!isGroupChat) {
                    splitSender(conversation.summary.toString())[1]
                } else {
                    conversation.ticker.toString()
                }
                addConversationMessage(key, msg)
            }

            if (mChatHistoryEnabled) {
                // Add to database
                GlobalScope.launch(Dispatchers.IO) {
                    var userD = mDb.userDao().findByUserId(conversation.id!!)
                    if (userD == null) {
                        val user = User(
                            user_id = conversation.id!!,
                            username = username,
                            conversation.timestamp
                        )

                        val newUid = mDb.userDao().insert(user)
                        // make sure there's always an avatar in the db, even if it's empty
                        val avatar = Avatar(
                            newUid,
                            ""
                        )
                        // update user so we can use it
                        userD = User(
                            user_id = user.user_id,
                            username = user.username,
                            conversation.timestamp,
                            newUid
                        )
                        mDb.avatarDao().insert(avatar)
                    } else {
                        // update username
                        if (userD.username != username) {
                            userD.username = username
                        }

                        userD.latest_message = conversation.timestamp
                        mDb.userDao().update(userD)
                    }

                    val message = run {
                        val chatType: ChatType
                        val groupChatUsername: String
                        val msg: String


                        if (!isGroupChat) {
                            chatType = ChatType.CHAT
                            groupChatUsername = ""
                            msg = EmojiTranslator.translate(mConversationHistory[key]!![0]!!).toString()
                        } else {
                            val split = splitSender(EmojiTranslator.translate(conversation.ticker))
                            chatType = ChatType.GROUP
                            groupChatUsername = split[0].toString()
                            msg = split[1].toString()
                        }

                        Message(userD.u_id, msg, MessageType.RECEIVER, chatType, conversation.timestamp, groupChatUsername)
                    }

                    // TODO: Implement date message type

                    mDb.messageDao().insert(message)
                    notifyChatUiChangedData(context, userD.u_id)
                }
            }
        }

        val builder = Notification.CarExtender.Builder(
            EmojiTranslator.translate(unreadConversation.participant).toString()
        )

        // don't add regular messages when it was recalled (recalled won't need to recalculate data anyways)
        // because a recalled message requires a re-build due to changing message content
        if (!isRecalled || shouldSkip) {
            // our array is ordered from newest to oldest, but needs to be inserted from oldest to newest
            var notificationMessages: MutableList<String?> = ArrayList()

            var convertedNotifications = false
            var addBuffer = false
            val buffer = arrayOfNulls<String>(unreadCount)
            //val messages = mConversationHistory[key]!!
            val carMessageIndex = carExtenderMessages.size - 1
            val messagesLastIndex = messages.size - 1

            for (i in unreadCount - 1 downTo 0) {
                // first used cached messages
                // guaranteed to have no [Message] nonsense
                if (messagesLastIndex >= i) {
                    builder.addMessage(messages[i])
                    buffer[i] = messages[i]
                } else {
                    // we have to calculate it then...
                    addBuffer = true
                    if (msgCheck != "[Message]" &&
                        carMessageIndex >= i
                    ) {

                        // valid message for both chats and groups
                        builder.addMessage(carExtenderMessages[i])
                        // add it to cache also
                        buffer[i] = carExtenderMessages[i]
                        continue
                    }

                    // lazy evaluation
                    if (!convertedNotifications) {
                        notificationMessages =
                            extractNotificationMessages(notificationHistory, isGroupChat)
                        notificationMessages.reverse()
                        convertedNotifications = true
                    }

                    // process the ticker message using WeChatMessage
                    if (notificationMessages.size < i) {
                        // we don't have this message sadly :(
                        // however this shouldn't happen often at all at least
                        if (!isGroupChat) {
                            builder.addMessage("[Unknown]")
                            buffer[i] = "[Unknown]"
                        } else {
                            builder.addMessage("Unknown: [Unknown]")
                            buffer[i] = "Unknown: [Unknown]"
                        }
                        continue
                    }
                    builder.addMessage(notificationMessages[i])
                    buffer[i] = notificationMessages[i]
                }
            }

            // add buffer history to conversation history to supplement it
            // also so we don't have to rebuild the messages again
            // at this point we're simply overwriting everything in it with the
            // new history
            if (addBuffer) {
                // add oldest elements first (buffer is stored oldest to newest)
                // this way the newest element is first (we store history as newest to oldest)
                for (s in buffer) {
                    addConversationMessage(key, s)
                }
            }
        } else {
            // this is a special case needing to be handled separately
            // no messages have been added, but one might change though (it was recalled!)

            // check whether the recalled message was bogus or not
            // a bogus recalled message occurs when it was recalled, but it's not within our
            // unread history. this causes issues cause we expect it to be a valid recall!
            var bogusRecalled = false

            var confidence = 0
            // this has been messed with above, we need a fresh set
            var carMessages: MutableList<String> = arrayListOf(*unreadConversation.messages)

            // chop it into pieces, this is my last resort...
            // appropriate for the size as IF it hadn't been recalled
            //
            // -1 because we added to the offset above, but if it was fake then the +1 offset before
            // is not applicable
            val extra = carMessages.size - (mUnreadCount[key]!! - (mUnreadOffset[key]!!-1))
            if (extra > 0 && extra < carMessages.size) {
                // only view the proper amount of data
                carMessages = carMessages.subList(extra, carMessages.size)
            }
            carMessages.reverse()

            // same amount of messages, even though one should've been recalled!
            // Recalled message sus!
            // -1 because we modified the offset count above
            val size = mUnreadCount[key]!! - (mUnreadOffset[key]!!-1)
            if (size == carMessages.size) confidence++

            // the size may be the same and return previous
            // elements (even though it was read). Furthermore, it's most likely the elements
            // aren't the same data as our history anymore
            val hist = ArrayList(
                mConversationHistory[key]!!.subList(
                    0, mUnreadCount[key]!!.coerceAtMost(
                        MAX_NUM_CONVERSATIONS
                    )
                )
            )
            hist.removeAll { it?.startsWith(context.getString(R.string.recalled_message)) == true }
            // we lost confidence if the message isn't even in our history
            for (msg in carMessages) {
                if (!hist.contains(msg)) {
                    confidence--
                }
            }

            // Often it will also return duplicates which will make the above ^^ fail

            // next compare frequency of elements
            // if we have only 1 occurrence
            for (msg in hist) {
                val unrecalledCount = hist.count { it == msg }
                val carMsgCount = carMessages.count { it == msg }

                // we've lost confidence that this is reliably and in actuality incorrect
                if (carMsgCount > unrecalledCount) {
                    confidence--
                }
            }


            // 1 or higher is a good confidence that it's not our recalled message
            if (confidence >= 1) {
                bogusRecalled = true
                // since it's a bogus message, fix the offset
                mUnreadOffset[key] = mUnreadOffset[key]?.minus(1)
            }
            //
            // End bogus recall message handling
            //

            if (!bogusRecalled) {
                val user = User(
                    conversation.id!!,
                    username
                )

                val msg = if (isGroupChat) {
                    EmojiTranslator.translate(conversation.ticker).toString()
                } else {
                    splitSender(
                        conversation.ticker
                    )[1].toString()
                }

                handleRecalledMessage(
                    key,
                    msg,
                    context,
                    carExtenderMessages,
                    isGroupChat,
                    user
                )
            }

            val history = mConversationHistory[key]!!.subList(0, unreadCount)

            for (i in unreadCount-1 downTo 0) {
                builder.addMessage(history[i])
            }
        }

        builder.setLatestTimestamp(unreadConversation.latestTimestamp)
        builder.setReadPendingIntent(unreadConversation.readPendingIntent)
        builder.setReplyAction(
            unreadConversation.replyPendingIntent,
            unreadConversation.remoteInput
        )

        if (isReplying) {
            // now that we got the full conversation, flag it to be changed next when we send a regular message
            if (mReplyFlag[key] == 0) {
                mReplyFlag[key] = 1
            }
            (context.applicationContext as WeChatApp).replying = false
        }

        return builder.build()
    }
}