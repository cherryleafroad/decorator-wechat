package com.oasisfeng.nevo.decorators.wechat;

import android.app.Notification;
import android.app.Notification.CarExtender.UnreadConversation;
import android.app.Notification.CarExtender;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.oasisfeng.nevo.decorators.wechat.ConversationManager.Conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.app.Notification.EXTRA_TEXT;
import static com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG;
import static com.oasisfeng.nevo.decorators.wechat.WeChatMessage.SENDER_MESSAGE_SEPARATOR;


class ConversationHistory {
    public static final int MAX_NUM_CONVERSATIONS = 10;

    private static HashMap<String, ArrayList<String>> mConversationHistory = new HashMap<>();


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

    // make edited conversation acceptable for processing
    private static Conversation formatConversation(Conversation conversation, String message) {
        conversation.summary = message;
        CharSequence charMsg;

        conversation.ticker = message;
        final CharSequence ticker = conversation.ticker;
        final int content_length = ticker.length();
        // need to remove unread count from message for ticker
        int pos;
        if (content_length > 3 && ticker.charAt(0) == '[' && (pos = TextUtils.indexOf(ticker, ']', 1)) > 0) {
            CharSequence prefix = ticker.subSequence(1, pos);
            final int length = prefix.length();
            final CharSequence count = length > 1 && ! Character.isDigit(prefix.charAt(length - 1)) ? prefix.subSequence(0, length - 1) : prefix;

            // see if it's a number, otherwise we want the full message
            try {
                Integer.parseInt(count.toString());
                conversation.ticker = ticker.subSequence(pos+1, ticker.length());
            } catch (final NumberFormatException ignored) {     // Probably just emoji like "[Cry]"
                Log.d(TAG, "Failed to parse as int: " + prefix);
                conversation.ticker = ticker;
            }
        }

        return conversation;
    }

    // The meat and bones right here. it maintains conversation history
    // adds the new one, replaces and fixes the carExtenders conversations
    // AND returns the fixed unreadConversation from the car extender
    //
    // The purpose of this class is to fix an issue where WeChat sends "[Message]"
    // when you do certain actions like quote a message, which makes it unusable
    public static UnreadConversation getUnreadConversation(String key, UnreadConversation unreadConversation,
                                           Conversation conversation, List<StatusBarNotification> notificationHistory) {
        Conversation newConversation;
        try {
            newConversation = conversation.clone();
        } catch (Exception e) {
            // simply return the original unreadConversation because this may fail otherwise
            // if we need to read the notifications
            Log.e(TAG, "Failed to clone conversation - aborted unreadConversation patch");
            return unreadConversation;
        }

        // figure out the type of conversation we're dealing with
        newConversation.ext = unreadConversation;
        int type = WeChatMessage.guessConversationType(newConversation);
        boolean isGroupChat;
        if (type == Conversation.TYPE_DIRECT_MESSAGE || type == Conversation.TYPE_BOT_MESSAGE || type == Conversation.TYPE_UNKNOWN) {
            // treat unknown as a normal message
            isGroupChat = false;
        } else {
            isGroupChat = true;
        }

        // car extender messages are ordered from oldest to newest
        String[] carExtenderMessages = unreadConversation.getMessages();
        int lastIndex = carExtenderMessages.length - 1;
        // if it's an erroneous message, go to fallback, otherwise use original
        // make sure to grab the latest which is the last one
        String msgCheck;
        if (!isGroupChat) {
            // Single chat or Bot
            msgCheck = carExtenderMessages[lastIndex];
        } else {
            // this is a group chat
            // need to slice off -> Name: Msg -> Msg
            Conversation msgCmp = formatConversation(newConversation, carExtenderMessages[lastIndex]);
            msgCheck = WeChatMessage.getTickerMessage(msgCmp);
        }


        if (!msgCheck.equals("[Message]")) {
            // car extender has the correct msg in both cases
            // Name: Msg, for groups
            // for regular chat just, Msg
            addConversationMessage(key, carExtenderMessages[lastIndex]);
        } else {
            // fallback, real message is in the ticker
            String msg;
            if (!isGroupChat) {
                msg = WeChatMessage.getTickerMessage(formatConversation(newConversation, (String) newConversation.summary));
            } else {
                msg = (String)conversation.ticker;
            }
            addConversationMessage(key, msg);
        }

        boolean convertedNotifications = false;
        String[] notificationMessages = new String[0];
        CarExtender.Builder builder = new CarExtender.Builder(unreadConversation.getParticipant());
        // our array is ordered from newest to oldest, but needs to be inserted from oldest to newest

        String[] messages = getConversationHistory(key);
        String[] buffer = new String[lastIndex+1];
        boolean addBuffer = false;
        for (int i = lastIndex; i >= 0; i--) {
            int forwardIndex = lastIndex-i;

            // how many times we need to do the fallback iter first
            int fallbackIters = lastIndex+1-messages.length;
            if (fallbackIters > 0) {
                // there's no more cache now, so go to fallback methods
                // we have to process this first due to older first requirement

                // this probably happens cause the process wasn't alive
                // somehow carextender sent more messages than we had cached at the time
                // this can happen in one instance when you reinstall but had pending unread messages
                //
                // I'll have to use either notification history or carExtender messages
                // to get the other missing ones

                // use car extender message if it's not corrupted
                // we can go forwards cause car extender is reversed order
                addBuffer = true;
                if (!msgCheck.equals("[Message]")) {
                    // valid message for both chats and groups
                    builder.addMessage(carExtenderMessages[forwardIndex]);
                    // add it to cache also
                    buffer[forwardIndex] = carExtenderMessages[forwardIndex];
                } else {
                    // lazy evaluation
                    if (!convertedNotifications) {
                        notificationMessages = extractNotificationMessages(notificationHistory);
                        convertedNotifications = true;
                    }

                    // process the ticker message using WeChatMessage
                    int index = notificationMessages.length-1-i;
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
                        newMsg = (String)newConversation.ticker;
                    }

                    builder.addMessage(newMsg);
                    buffer[forwardIndex] = newMsg;
                }
            } else {
                // add it from the history cache
                // message exists for this nth index
                builder.addMessage(messages[i]);
                buffer[forwardIndex] = messages[i];
            }
        }

        // add buffer history to conversation history to supplement it
        // also so we don't have to rebuild the messages again
        // at this point we're simply overwriting everything in it with the
        // new history
        if (addBuffer) {
            // add oldest elements first (buffer is stored oldest to newest)
            // this way the newest element is first (we store history as newest to oldest)
            for (int i = 0; i < buffer.length; i++) {
                addConversationMessage(key, buffer[i]);
            }
        }

        builder.setLatestTimestamp(unreadConversation.getLatestTimestamp());
        builder.setReadPendingIntent(unreadConversation.getReadPendingIntent());
        builder.setReplyAction(unreadConversation.getReplyPendingIntent(), unreadConversation.getRemoteInput());

        return builder.build();
    }
}
