package com.oasisfeng.nevo.decorators.wechat;

import android.app.Notification;
import android.app.Notification.CarExtender.UnreadConversation;
import android.app.Notification.CarExtender;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.oasisfeng.nevo.decorators.wechat.ConversationManager.Conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.app.Notification.EXTRA_TEXT;
import static com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG;


class ConversationHistory {
    public static final int MAX_NUM_CONVERSATIONS = 10;

    private static final ArrayMap<String, ArrayList<String>> mConversationHistory = new ArrayMap<>();
    private final static ArrayMap<String, Integer> mUnreadCount = new ArrayMap<>();
    private final static ArrayMap<String, Integer> mUnreadOffset = new ArrayMap<>();
    private final static ArrayMap<String, Integer> mReplyFlag = new ArrayMap<>();


    private static void handleRecalledMessage(String key, String message, Context context, String[] car_messages, boolean isGroupChat) {
        // this should ALWAYS succeed
        // Please note that it is IMPOSSIBLE to differentiate which message was recalled
        // when both messages content is exactly the same. This is an unfortunate side effect
        // and there's no way to solve it as carExtenderMessages only tells us the missing ones (not which one it was)

        if (mConversationHistory.containsKey(key)) {
            // we MUST slice this to prevent previously returned history (that we aren't showing) from
            // influencing the data
            List<String> history = new ArrayList<>((List<String>)mConversationHistory.get(key)).subList(0, mUnreadCount.get(key));
            boolean visible = ((WeChatApp) context.getApplicationContext()).getSharedPreferences().getBoolean(context.getString(R.string.pref_recalled), false);

            String msg;
            if (isGroupChat) {
                // check back history
                for (int i = 0; i < history.size(); i++) {
                    if (history.get(i).equals(message)) {

                        String[] sender = splitSender(history.get(i));
                        if (visible) {
                            // [Recalled] Message
                            msg = sender[0] + ": " + context.getString(R.string.recalled_message) + " " + sender[1];
                        } else {
                            msg = sender[0] + ": " + context.getString(R.string.recalled_message);
                        }
                        history.set(i, msg);
                        mConversationHistory.put(key, new ArrayList<>(history));
                        return;
                    }
                }
            } else {

                List<String> messages = Arrays.asList(car_messages);
                // the list is originally ordered from oldest->newest
                // ours is newest->oldest. Best that they are both the same and exact same length
                Collections.reverse(messages);
                messages = messages.subList(0, Math.max(Math.min(messages.size(), history.size()-1), 0));

                int m_size = messages.size();
                int h_size = history.size();


                //
                // Check for when there's only 1 message
                //
                if (h_size == 1 && m_size == 0 && !history.get(0).startsWith(context.getString(R.string.recalled_message))) {
                    msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(0) : context.getString(R.string.recalled_message);
                    history.set(0, msg);
                    mConversationHistory.put(key, new ArrayList<>(history));
                    return;
                } else if (h_size == 1 && m_size == 0) {
                    // already has the recalled entry
                    return;
                }


                // Here we check the valid indexes by removing invalid ones, if there's only 1 left
                // then that's the correct one
                //
                // the remaining indexes in history that could be the potential recalled message
                List<Integer> valid_indexes = new ArrayList<>();
                // initialize all potential indexes
                for (int i = 0; i < history.size(); i++) {
                    valid_indexes.add(i);
                }

                // remove invalid indexes from List
                for (int i = 0; i < h_size; i++) {
                    // any history message contained in messages is invalid as it wasn't removed
                    // from messages. Also, already recalled message indexes are also invalid
                    if (messages.contains(history.get(i)) ||
                            history.get(i).startsWith(context.getString(R.string.recalled_message))) {

                        // remove invalid index
                        valid_indexes.remove(Integer.valueOf(i));
                    }
                }

                // found the exact one!
                if (valid_indexes.size() == 1) {
                    int index = valid_indexes.get(0);
                    msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(index) : context.getString(R.string.recalled_message);
                    history.set(index, msg);
                    mConversationHistory.put(key, new ArrayList<>(history));
                    return;
                }
                //
                // /End
                //


                //
                // Two or more duplicate elements were found
                //

                // TODO find a way to triangulate the one if there's multiple same messages

                ///
                /// /End
                ///
            }
        }
        // this point should never be reached
        Log.d(TAG, "handleRecalledMessage() reached unreachable point");
    }

    // returns the sender and message separately
    private static String[] splitSender(CharSequence message) {
        String[] returnStr = new String[2];
        int pos;

        if ((pos = TextUtils.indexOf(message, ':', 1)) > 0) {
            CharSequence sender = removeUnreadCount(message.subSequence(0, pos));
            CharSequence msg = message.subSequence(pos+2, message.length());
            returnStr[0] = sender.toString();
            returnStr[1] = msg.toString();
        }

        return returnStr;
    }

    public static void addConversationMessage(String key, String message) {
        ArrayList<String> conversation;
        if (mConversationHistory.containsKey(key)) {
            conversation = mConversationHistory.get(key);
            conversation.add(0, message);

            if (conversation.size() > MAX_NUM_CONVERSATIONS) {
                conversation.remove(conversation.size()-1);
            }
        } else {
            conversation = new ArrayList<>();
            conversation.add(message);
        }
        mConversationHistory.put(key, conversation);
    }

    public static String[] getConversationHistory(String key) {
        if (mConversationHistory.containsKey(key)) {
            return mConversationHistory.get(key).toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    private static String[] extractNotificationMessages(List<StatusBarNotification> notifications) {
        String[] messages = new String[notifications.size()];
        for (int i = 0; i < notifications.size(); i++) {
            final Notification notification = notifications.get(i).getNotification();
            final Bundle its_extras = notification.extras;
            final String its_text = its_extras.getString(EXTRA_TEXT);
            if (its_text == null) {
                Log.w(TAG, "No text in archived notification.");
                messages[i] = "[Unknown]";
                continue;
            }
            messages[i] = its_text;
        }

        return messages;
    }

    public static void markAsRead(String key) {
        mUnreadCount.put(key, 0);
        mUnreadOffset.put(key, 0);
    }

    private static CharSequence removeUnreadCount(CharSequence message) {
        final int content_length = message.length();
        // need to remove unread count from message for ticker
        int pos;
        if (content_length > 3 && message.charAt(0) == '[' && (pos = TextUtils.indexOf(message, ']', 1)) > 0) {
            CharSequence prefix = message.subSequence(1, pos);
            final int length = prefix.length();
            final CharSequence count = length > 1 && ! Character.isDigit(prefix.charAt(length - 1)) ? prefix.subSequence(0, length - 1) : prefix;

            // see if it's a number, otherwise we want the full message
            try {
                Integer.parseInt(count.toString());
                return message.subSequence(pos+1, message.length());
            } catch (final NumberFormatException ignored) {     // Probably just emoji like "[Cry]"
                Log.d(TAG, "Failed to parse as int: " + prefix);
                return message;
            }
        }
        return message;
    }

    // make edited conversation acceptable for processing
    private static Conversation formatConversation(Conversation conversation, String message) {
        conversation.summary = message;
        conversation.ticker = removeUnreadCount(conversation.ticker);

        return conversation;
    }

    // The meat and bones right here. it maintains conversation history
    // adds the new one, replaces and fixes the carExtenders conversations
    // AND returns the fixed unreadConversation from the car extender
    //
    // The purpose of this class is to fix an issue where WeChat sends "[Message]"
    // when you do certain actions like quote a message, which makes it unusable
    public static UnreadConversation getUnreadConversation(Context context, String key, UnreadConversation unreadConversation,
                                                           Conversation conversation, List<StatusBarNotification> notificationHistory, boolean isRecalled) {
        if (conversation.ticker == null) {
            conversation.ticker = removeUnreadCount(conversation.summary);
        }

        boolean isReplying = ((WeChatApp)context.getApplicationContext()).getReplying();

        // create keys for new conversation if not here
        if (!mConversationHistory.containsKey(key)) {
            mConversationHistory.put(key, new ArrayList<>());
            mUnreadCount.put(key, 0);
            mUnreadOffset.put(key, 0);
            mReplyFlag.put(key, 0);
        }

        if (mReplyFlag.get(key) == 1 && !isReplying) {
            // we can now reset the history because we're done replying
            markAsRead(key);
            mReplyFlag.put(key, 0);
        }

        Conversation newConversation;
        try {
            newConversation = conversation.clone();
        } catch (Exception e) {
            // simply return the original unreadConversation because this may fail otherwise
            // if we need to read the notifications
            Log.e(TAG, "Failed to clone conversation - aborted unreadConversation patch");
            return unreadConversation;
        }


        boolean isGroupChat = conversation.isGroupChat();


        // A simple unread message counter
        // Only will reset when we clear the notification
        // this simple way of counting is more reliable than
        // relying on the apps notification counter which is notoriously unreliable (specifically
        // when dealing with recalled messages)
        // -> This is used to tell us how many messages we should display

        // Don't create extra notifications when replying or recalling a message
        // this is a regular message
        if (!isReplying && !isRecalled) {
            mUnreadCount.put(key, mUnreadCount.get(key) + 1);
        } else if (!isReplying && isRecalled) {
            mUnreadOffset.put(key, mUnreadOffset.get(key) + 1);
        }

        // unread count will be set to 0 respectively when we clear the notification
        // recalled messages are simply skipped as they are the same message
        // see @WeChatDecorator.onNotificationRemoved

        // only MAX is allowed
        int unreadCount = Math.min(mUnreadCount.get(key), MAX_NUM_CONVERSATIONS);
        conversation.count = unreadCount;


        // this part serves the purpose of :
        // fixing the conversation ticker and summary when isRecalled is true
        // and it's in a group ->
        // Sadly, can't figure out the proper single chat fix here, but
        // unread car extender messages will have the missing message
        // so we can pinpoint the deleted one
        if (isGroupChat && isRecalled) {
            // update conversation fields to be correct
            String[] senderS = splitSender(conversation.summary);
            String[] senderT = splitSender(conversation.ticker);
            if (unreadCount > 0) {
                // rebuild correct message
                conversation.summary = "[" + unreadCount + "]" + senderT[0] + ": " + senderS[1];
            } else {
                conversation.summary = senderT[0] + ": " + senderS[1];
            }
            // rebuild correct message
            conversation.ticker = senderT[0] + ": " + senderS[1];
            newConversation.summary = conversation.summary;
            newConversation.ticker = conversation.ticker;
        } else {
            // fix counter in messages
            String[] split;
            split = splitSender(conversation.ticker);

            // fix the fields to make sure they're correct, also replaces the count
            if (unreadCount > 0) {
                conversation.summary = "[" + unreadCount + "]" + split[0] + ": " + split[1];
            } else {
                conversation.summary = split[0] + ": " + split[1];
            }
        }

        // car extender messages are ordered from oldest to newest
        String[] carExtenderMessages = unreadConversation.getMessages();
        int lastIndex = carExtenderMessages.length - 1;
        // carextender missing data for some reason? strange
        if (lastIndex == -1) {
            lastIndex = 0;
            // we >Probably< have it in history, BUT if not...
            if (mConversationHistory.get(key).size() > 0) {
                carExtenderMessages = new String[] {mConversationHistory.get(key).get(0)};
            } else {
                String[] split = splitSender(conversation.ticker);
                if (split[0] != null) {
                    carExtenderMessages = new String[]{split[1]};
                } else {
                    carExtenderMessages = new String[]{"[Unknown]"};
                }
            }
        }
        // if it's an erroneous message, go to fallback, otherwise use original
        // make sure to grab the latest which is the last one
        String msgCheck;
        String real_message = isRecalled ? conversation.summary.toString() : carExtenderMessages[lastIndex];

        if (!isGroupChat) {
            // Single chat or Bot
            msgCheck = real_message;
        } else {
            // this is a group chat
            // need to slice off -> Name: Msg -> Msg
            msgCheck = splitSender(real_message)[1];
        }

        // Replying has a double entry, so don't add twice it if we're replying
        // this means we're only getting the history, not adding to it
        // And recalling messages do not have any extra message to add
        if (!isReplying && !isRecalled) {
            if (!msgCheck.equals("[Message]")) {
                // car extender has the correct msg in both cases
                // Name: Msg, for groups
                // for regular chat just, Msg
                addConversationMessage(key, carExtenderMessages[lastIndex]);
            } else {
                // fallback, real message is in the ticker
                String msg;
                if (!isGroupChat) {
                    msg = WeChatMessage.getTickerMessage(formatConversation(newConversation, newConversation.summary.toString()));
                } else {
                    msg = conversation.ticker.toString();
                }
                addConversationMessage(key, msg);
            }
        }

        boolean convertedNotifications = false;
        String[] notificationMessages = new String[0];
        CarExtender.Builder builder = new CarExtender.Builder(EmojiTranslator.translate(unreadConversation.getParticipant()).toString());
        // our array is ordered from newest to oldest, but needs to be inserted from oldest to newest

        String[] messages = getConversationHistory(key);

        boolean addBuffer = false;

        // go through each of the calculated unread messages
        int messages_last_index = messages.length-1;
        String[] buffer = new String[unreadCount];
        // don't add regular messages when it was recalled (recalled won't need to recalculate data anyways)
        // because a recalled message requires a re-build due to changing message content
        if (!isRecalled) {
            for (int i = unreadCount - 1; i >= 0; i--) {
                int forwardIndex = unreadCount - 1 - i;

                // first used cached messages
                // guaranteed to have no [Message] nonsense
                if (messages_last_index >= i) {
                    builder.addMessage(messages[i]);
                    buffer[forwardIndex] = messages[i];
                } else {
                    // we have to calculate it then...
                    addBuffer = true;
                    if (!msgCheck.equals("[Message]") &&
                            carExtenderMessages.length - 1 >= forwardIndex) {

                        // valid message for both chats and groups
                        builder.addMessage(carExtenderMessages[forwardIndex]);
                        // add it to cache also
                        buffer[forwardIndex] = carExtenderMessages[forwardIndex];
                        continue;
                    }
                    // deliberate fallthrough to try another method if the above fails

                    // lazy evaluation
                    if (!convertedNotifications) {
                        notificationMessages = extractNotificationMessages(notificationHistory);
                        convertedNotifications = true;
                    }

                    // process the ticker message using WeChatMessage
                    int index = notificationMessages.length - 1 - i;
                    if (index < 0) {
                        // we don't have this message sadly :(
                        // however this shouldn't happen often at all at least
                        if (!isGroupChat) {
                            builder.addMessage("[Unknown]");
                            buffer[forwardIndex] = "[Unknown]";
                        } else {
                            builder.addMessage("Unknown: [Unknown]");
                            buffer[forwardIndex] = "Unknown: [Unknown]";
                        }
                        continue;
                    }

                    newConversation = formatConversation(newConversation, notificationMessages[index]);
                    String newMsg;
                    if (!isGroupChat) {
                        newMsg = WeChatMessage.getTickerMessage(newConversation);
                    } else {
                        newMsg = newConversation.ticker.toString();
                    }

                    builder.addMessage(newMsg);
                    buffer[forwardIndex] = newMsg;
                }
            }

            // add buffer history to conversation history to supplement it
            // also so we don't have to rebuild the messages again
            // at this point we're simply overwriting everything in it with the
            // new history
            if (addBuffer) {
                // add oldest elements first (buffer is stored oldest to newest)
                // this way the newest element is first (we store history as newest to oldest)
                for (String s : buffer) {
                    addConversationMessage(key, s);
                }
            }
        } else {
            // this is a special case needing to be handled separately
            // handle it at the end to make sure all messages are added
            handleRecalledMessage(key, isGroupChat ? conversation.ticker.toString() : splitSender(conversation.ticker)[1], context, carExtenderMessages, isGroupChat);

            // builder doesn't reflect our changed messages, so we need to re-fill it
            messages = mConversationHistory.get(key).toArray(new String[0]);
            builder = new CarExtender.Builder(EmojiTranslator.translate(unreadConversation.getParticipant()).toString());

            for (int i = unreadCount-1; i >= 0; i--) {
                if (messages.length-1 >= i) {
                    builder.addMessage(messages[i]);
                }
            }
        }

        builder.setLatestTimestamp(unreadConversation.getLatestTimestamp());
        builder.setReadPendingIntent(unreadConversation.getReadPendingIntent());
        builder.setReplyAction(unreadConversation.getReplyPendingIntent(), unreadConversation.getRemoteInput());

        if (isReplying) {
            // now that we got the full conversation, flag it to be changed next when we send a regular message
            if (mReplyFlag.get(key) == 0) {
                mReplyFlag.put(key, 1);
            }
            ((WeChatApp)context.getApplicationContext()).setReplying(false);
        }
        return builder.build();
    }
}
