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
import java.util.HashMap;
import java.util.List;

import static android.app.Notification.EXTRA_TEXT;
import static com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG;


class ConversationHistory {
    public static final int MAX_NUM_CONVERSATIONS = 10;

    private static ArrayMap<String, ArrayList<String>> mConversationHistory = new ArrayMap<>();
    public static ArrayMap<String, Integer> mUnreadCount = new ArrayMap<>();


    private static void handleRecalledMessage(String key, String message, Context context, String[] messages, boolean isGroupChat) {
        // this should ALWAYS succeed
        if (mConversationHistory.containsKey(key)) {
            ArrayList<String> history = mConversationHistory.get(key);
            assert history != null;

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
                        mConversationHistory.put(key, history);
                        return;
                    }
                }
            } else {
                // CarExtender unreadmessages will have all but the missing message
                // that's how we can pinpoint the lost one.
                int lastIndex = messages.length-1;
                if (lastIndex == -1 && !history.get(0).startsWith(context.getString(R.string.recalled_message))) {
                    // this happens if we recalled the ONLY message in the notification
                    msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(0) : context.getString(R.string.recalled_message);
                    history.set(0, msg);
                    mConversationHistory.put(key, history);
                    return;
                } else if (lastIndex == -1) {
                    // already has the recalled entry
                    return;
                }

                // get the starting position
                int hist_start_index = history.indexOf(messages[lastIndex]);
                // unread messages contain no similar elements?
                if (hist_start_index == -1) return;

                // first check elements below the start index to ensure we already handled them
                int c_last_index = lastIndex;
                if (hist_start_index > 0) {
                    for (int i = 0; i < hist_start_index; i++) {
                        if (c_last_index < 0) break;

                        if (!history.get(i).equals(messages[c_last_index]) &&
                            !history.get(i).startsWith(context.getString(R.string.recalled_message))) {

                            msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(i) : context.getString(R.string.recalled_message);
                            history.set(i, msg);
                            mConversationHistory.put(key, history);
                            return;
                        }
                        c_last_index -= i;
                    }
                }

                // if there are any gaps in the data that are not == recalled_message
                // it means that we found the recalled message
                c_last_index = lastIndex;
                for (int i = hist_start_index; i < history.size(); i++) {
                    // all elements match up to the end match
                    // this means that there are no gaps and it was the last (oldest) element
                    if (c_last_index < 0) {
                        msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(i) : context.getString(R.string.recalled_message);
                        history.set(i, msg);
                        mConversationHistory.put(key, history);
                        return;
                    }

                    // verify that the element is equal &&
                    // if it's not equal, then it wasn't a previously recalled one
                    // (as a previously recalled one will also have a gap)
                    if (!history.get(i).equals(messages[c_last_index]) &&
                        !history.get(i).startsWith(context.getString(R.string.recalled_message))) {
                        msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(i) : context.getString(R.string.recalled_message);
                        history.set(i, msg);
                        mConversationHistory.put(key, history);
                        return;
                    } else if (!history.get(i).equals(messages[c_last_index])) {
                        // this is a gap, these need to re-sync together
                        // the size of the gap is unknown however

                        // next sync location - I literally feel like this this sync is a goto statement :/
                        int sync = history.indexOf(messages[c_last_index]);
                        // either it wasn't found.. so... there's nothing to do then
                        if (sync == -1) return;

                        // check the inside contents of the gap for any re-callable elements
                        // any element found is invalid
                        for (int j = i+1; j < sync; j++) {
                            // found an unrecalled element in the gap!
                            if (!history.get(j).startsWith(context.getString(R.string.recalled_message))) {
                                msg = visible ? context.getString(R.string.recalled_message) + " " + history.get(j) : context.getString(R.string.recalled_message);
                                history.set(j, msg);
                                mConversationHistory.put(key, history);
                                return;
                            }
                        }

                        // re-sync the loops - but variables will increment, so less by 1
                        i = sync;
                    }

                    c_last_index -= 1;
                }
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

        if (!mUnreadCount.containsKey(key)) {
            mUnreadCount.put(key, 0);
        }

        boolean isReplying = ((WeChatApp)context.getApplicationContext()).getReplying();
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
        // why do this? Because it stops it from thinking that many messages
        // with [Emoji] in them mean it's a group message
        newConversation.summary = EmojiTranslator.translate(conversation.summary);
        newConversation.ticker = EmojiTranslator.translate(conversation.ticker);
        int type = WeChatMessage.guessConversationType(newConversation);
        newConversation.summary = conversation.summary;
        newConversation.ticker = conversation.ticker;
        boolean isGroupChat;
        // treat unknown as a normal message
        isGroupChat = type != Conversation.TYPE_DIRECT_MESSAGE && type != Conversation.TYPE_BOT_MESSAGE && type != Conversation.TYPE_UNKNOWN;


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
        if (removeUnreadCount(conversation.summary.toString()).toString().startsWith("wxid_") &&
            conversation.summary.toString().indexOf(':') != -1 &&
            conversation.ticker.toString().indexOf(':') != -1 && isRecalled) {
            // there's STRONG evidence that this is actually a group chat regardless that isGroupChat is false
            // the ID's are strange if it's recalled though
            isGroupChat = true;

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
            if (!isRecalled) {
                split = splitSender(conversation.summary);
            } else {
                // data isn't available in summary for chat,
                // recalled single chat messages are usually:
                // [2]Recalled -> this has no information at all
                split = splitSender(conversation.ticker);
            }

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
        for (int i = unreadCount-1; i >= 0; i--) {
            int forwardIndex = unreadCount-1-i;

            // first used cached messages
            // guaranteed to have no [Message] nonsense
            if (messages_last_index >= i) {
                builder.addMessage(messages[i]);
                buffer[forwardIndex] = messages[i];
            } else {
                // we have to calculate it then...
                addBuffer = true;
                if (!msgCheck.equals("[Message]") &&
                    carExtenderMessages.length-1 >= forwardIndex) {

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

        // this is a special case needing to be handled separately
        // handle it at the end to make sure all messages are added
        if (isRecalled) {
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
            ((WeChatApp)context.getApplicationContext()).setReplying(false);
        }
        return builder.build();
    }
}
