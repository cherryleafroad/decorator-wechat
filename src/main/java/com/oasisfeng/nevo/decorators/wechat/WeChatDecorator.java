/*
 * Copyright (C) 2015 The Nevolution Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oasisfeng.nevo.decorators.wechat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.oasisfeng.nevo.sdk.MutableNotification;
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification;
import com.oasisfeng.nevo.sdk.NevoDecoratorService;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.MessagingStyle;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Notification.EXTRA_TITLE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION;
import static android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.service.notification.NotificationListenerService.REASON_APP_CANCEL;
import static android.service.notification.NotificationListenerService.REASON_CANCEL;

/**
 * Bring state-of-art notification experience to WeChat.
 *
 * Created by Oasis on 2015/6/1.
 */
public class WeChatDecorator extends NevoDecoratorService {

	private static final int MAX_NUM_ARCHIVED = 20;
	private static final long GROUP_CHAT_SORT_KEY_SHIFT = 24 * 60 * 60 * 1000L;        // Sort group chat as one day older message.
	private static final int NID_CONVERSATION_START = 4096;
	private static final String CHANNEL_MESSAGE = "message";
	private static final String CHANNEL_GROUP_CONVERSATION = "group";
	private static final String CHANNEL_MISC = "misc";

	private static final @ColorInt int PRIMARY_COLOR = 0xFF33B332;
	static final String SENDER_MESSAGE_SEPARATOR = ": ";

	@Override public void apply(final MutableStatusBarNotification evolving) {
		final MutableNotification n = evolving.getNotification();
		final Bundle extras = n.extras;

		CharSequence title = extras.getCharSequence(EXTRA_TITLE);
		if (title == null || title.length() == 0) {
			Log.e(TAG, "Title is missing: " + evolving);
			return;
		}
		if (title != (title = EmojiTranslator.translate(title))) extras.putCharSequence(EXTRA_TITLE, title);

		final int original_id = evolving.getOriginalId();
		if (BuildConfig.DEBUG) extras.putString("nevo.debug", "ID:" + original_id + ",t:" + n.tickerText);

		n.color = PRIMARY_COLOR;        // Tint the small icon

		if (original_id < NID_CONVERSATION_START) {
			if (SDK_INT >= O) n.setChannelId(CHANNEL_MISC);
			Log.d(TAG, "Skip further process for non-conversation notification. ID: " + original_id);    // E.g. web login confirmation notification.
			return;
		}

		// WeChat uses dynamic counter as notification ID, which will be reused by future conversations when cancelled by WeChat itself,
		//   causing conversation notifications overwritten or duplicate.
		evolving.setId(title.hashCode());		// Don't use the hash code of original title, which might have already evolved.

		extras.putBoolean(Notification.EXTRA_SHOW_WHEN, true);
		if (BuildConfig.DEBUG) n.flags &= ~ Notification.FLAG_LOCAL_ONLY;

		final CharSequence content_text = extras.getCharSequence(Notification.EXTRA_TEXT);
		final boolean group_chat = isGroupChat(n.tickerText, title.toString(), content_text);
		n.setSortKey(String.valueOf(Long.MAX_VALUE - n.when + (group_chat ? GROUP_CHAT_SORT_KEY_SHIFT : 0)));    // Place group chat below other messages
		if (SDK_INT >= O) n.setChannelId(group_chat ? CHANNEL_GROUP_CONVERSATION : CHANNEL_MESSAGE);

		MessagingStyle messaging = mMessagingBuilder.buildFromExtender(evolving.getOriginalKey(), n, title, group_chat);
		if (messaging == null)	// EXTRA_TEXT will be written in buildFromArchive()
			messaging = mMessagingBuilder.buildFromArchive(n, title, group_chat, getArchivedNotifications(evolving.getOriginalKey(), MAX_NUM_ARCHIVED));
		if (messaging == null) return;
		if (group_chat) messaging.setGroupConversation(true).setConversationTitle(title);

		final List<NotificationCompat.MessagingStyle.Message> messages = messaging.getMessages();
		if (messages.isEmpty()) return;
		final NotificationCompat.MessagingStyle.Message last_message = messages.get(messages.size() - 1);
		File image = null;
		if (WeChatImageLoader.isImagePlaceholder(this, last_message.getText().toString())) {
			if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
				if (mImageLoader == null) mImageLoader = new WeChatImageLoader(this);
				image = mImageLoader.loadImage();
				if (image != null) last_message.setData("image/jpeg", Uri.fromFile(image));	// TODO: Keep image mapping for previous messages.
			} else n.addAction(new Notification.Action.Builder(null, getText(R.string.action_preview_image), WeChatImageLoader.buildPermissionRequest(this)).build());
		}

		if (SDK_INT <= O_MR1 && messaging.getMessages().size() == 1 && image != null) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = SDK_INT >= O ? Bitmap.Config.HARDWARE : Bitmap.Config.ARGB_8888;
			extras.putString(Notification.EXTRA_TEMPLATE, TEMPLATE_BIG_PICTURE);
			extras.putParcelable(Notification.EXTRA_PICTURE, BitmapFactory.decodeFile(image.getPath(), options));
			extras.putCharSequence(Notification.EXTRA_SUMMARY_TEXT, last_message.getText());
		} else {
			final Bundle addition = new Bundle();
			messaging.addCompatExtras(addition);
			for (final String key : addition.keySet()) {    // Copy the extras generated by MessagingStyle to notification extras.
				final Object value = addition.get(key);
				if (value == null) continue;
				if (value instanceof CharSequence) extras.putCharSequence(key, (CharSequence) value);
				else if (value instanceof Parcelable[]) extras.putParcelableArray(key, (Parcelable[]) value);
				else if (value instanceof Bundle) extras.putBundle(key, (Bundle) value);
				else if (value instanceof Boolean) extras.putBoolean(key, (Boolean) value);
				else Log.e(TAG, "Unsupported extra \"" + key + "\": " + value);
			}
			extras.putCharSequence(NotificationCompat.EXTRA_CONVERSATION_TITLE, title);
			extras.putString(Notification.EXTRA_TEMPLATE, TEMPLATE_MESSAGING);
		}

		if (SDK_INT >= N && extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY) != null)
			n.flags |= Notification.FLAG_ONLY_ALERT_ONCE;		// No more alert for direct-replied notification.
	}

	// [Direct message with 1 unread]	Ticker: "Oasis: Hello",		Title: "Oasis",	Content: "Hello"
	// [Direct message with >1 unread]	Ticker: "Oasis: Hello",		Title: "Oasis",	Content: "[2]Oasis: Hello"
	// [Service message with 1 unread]	Ticker: "FedEx: Delivered",	Title: "FedEx",	Content: "[Link] Delivered"
	// [Group chat with 1 unread]		Ticker: "Oasis: Hello",		Title: "Group",	Content: "Oasis: Hello"
	// [Group chat with >1 unread]		Ticker: "Oasis: [Link] Mm",	Title: "Group",	Content: "[2]Oasis: [Link] Mm"
	private static boolean isGroupChat(final CharSequence ticker_text, final String title, final CharSequence content_text) {
		if (ticker_text == null || content_text == null) return false;
		final String ticker = ticker_text.toString();    // Ticker text always starts with sender (same as title for direct message, but not for group chat).
		final String content = content_text.toString();    // Content text includes sender for group and service messages, but not for direct messages.
		final int pos = content.indexOf(ticker.substring(0, Math.min(10, ticker.length())));    // Seek for the first 10 chars of ticker in content.
		if (pos >= 0 && pos <= 6) {        // Max length (up to 999 unread): [999t]
			final String message = pos > 0 && content.charAt(0) == '[' ? content.substring(pos) : content;    // Content without unread count prefix
			return ! message.startsWith(title + SENDER_MESSAGE_SEPARATOR);    // If positive, most probably a direct message with more than 1 unread
		} else return false;                                        // Most probably a direct message with 1 unread
	}

	@Override protected void onNotificationRemoved(final String key, final int reason) {
		if (reason == REASON_CANCEL) {
			mMessagingBuilder.markRead(key);
		} else if (reason == REASON_APP_CANCEL) {
			Log.d(TAG, "Cancel notification: " + key);
			cancelNotification(key);	// Will cancel all notifications evolved from the this original key.
		}
	}

	@Override protected void onConnected() {
		if (SDK_INT >= O) createNotificationChannels("com.tencent.mm", Arrays.asList(
				makeChannel(CHANNEL_MESSAGE, R.string.channel_message),
				makeChannel(CHANNEL_GROUP_CONVERSATION, R.string.channel_group_message),
				makeChannel(CHANNEL_MISC, R.string.channel_misc)));
	}

	@RequiresApi(O) private NotificationChannel makeChannel(final String channel_id, final @StringRes int name) {
		final NotificationChannel channel = new NotificationChannel(channel_id, getString(name), NotificationManager.IMPORTANCE_HIGH/* Allow heads-up (by default) */);
		channel.setSound(Uri.EMPTY,	// Default to none, due to sound being actually played by WeChat app itself (not via Notification).
				new AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_COMMUNICATION_INSTANT).setContentType(CONTENT_TYPE_SONIFICATION).build());
		channel.enableLights(true);
		channel.setLightColor(PRIMARY_COLOR);
		return channel;
	}

	@Override public void onCreate() {
		super.onCreate();
		mMessagingBuilder = new MessagingBuilder(this, this::recastNotification);
	}

	@Override public void onDestroy() {
		mMessagingBuilder.close();
		super.onDestroy();
	}

	private MessagingBuilder mMessagingBuilder;
	private WeChatImageLoader mImageLoader;

	static final String TAG = "Nevo.Decorator[WeChat]";
}
