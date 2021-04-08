package com.oasisfeng.nevo.decorators.wechat

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.collection.ArrayMap
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import com.oasisfeng.nevo.decorators.wechat.chatHistory.CenteredImageSpan
import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import kotlin.math.max

internal object EmojiTranslator {
    private val data = ArrayMap<String, Emoji>()
    private val extras = ArrayMap<String, Emoji>()

    fun loadData() {
        if (data.isEmpty) {
            val json = EmojiJNI.getConfig()
            val jsonified = IOUtils.toString(json, StandardCharsets.UTF_8.name())
            val decoded = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<Emojis>(jsonified)

            for (emoji in decoded.emoji) {
                if ((emoji.enabled || emoji.enabled_chat) && emoji.keys.isNotEmpty()) {
                    for (key in emoji.keys) {
                        data[key] = emoji
                    }

                    // also access by ID
                    data[emoji.id] = emoji
                }
            }

            for (i in decoded.config.boundary.toInt()-1 until decoded.emoji.size) {
                val emoji = decoded.emoji[i]
                if ((emoji.enabled || emoji.enabled_chat) && emoji.keys.isNotEmpty()) {
                    val id = if (emoji.emoji_compat != null && emoji.compat_with != null) {
                        if (Build.VERSION.SDK_INT <= emoji.compat_with) {
                            emoji.emoji_compat
                        } else {
                            emoji.emoji!!
                        }
                    } else {
                        emoji.emoji!!
                    }

                    extras[id] = emoji
                }
            }
        }
    }

    @Suppress("FunctionName")
    @JvmStatic
    fun translate_java(text: CharSequence): CharSequence {
        return translate(text)
    }

    fun translate(
        text: CharSequence,
        context: Context? = null,
        size: Int? = null,
        isChat: Boolean = false,
        isTitle: Boolean = false
    ): CharSequence {
        var bracketEnd = text.indexOf(']')
        if (bracketEnd == -1) {
            return text
        }

        var bracketStart = text.lastIndexOf('[', max(bracketEnd - 1, 0))
        if (bracketStart == -1) {
            return text
        }

        val builder = SpannableStringBuilder(text)
        var offset = 0
        while (bracketStart >= 0 && bracketEnd >= 0) {
            val marker = text.subSequence(bracketStart + 1, bracketEnd).toString()

            var emoji = data[marker]

            if (emoji != null) {
                if (!isChat && emoji.enabled) {
                    emoji = if (emoji.title_emoji != null) {
                        data[emoji.title_emoji]!!
                    } else {
                        emoji
                    }

                    val finEmoji = if (emoji.emoji_compat == null || emoji.compat_with == null) {
                        emoji.emoji!!
                    } else {
                        if (Build.VERSION.SDK_INT <= emoji.compat_with!!) {
                            emoji.emoji_compat!!
                        } else {
                            emoji.emoji!!
                        }
                    }

                    builder.replace(bracketStart + offset, bracketEnd + 1 + offset, finEmoji)
                    offset += finEmoji.length - marker.length - 2
                } else if (emoji.enabled_chat && isChat){
                    // use different image id if title image should be different
                    val id = if (isTitle && emoji.chat_title_emoji != null) {
                        emoji.chat_title_emoji!!
                    } else {
                        emoji.id
                    }

                    val drawable: Drawable = BitmapDrawable(context!!.resources, EmojiJNI.getEmojiBitmap(id)!!)
                    val image = CenteredImageSpan(drawable, customWidth = size, customHeight = size)
                    builder.setSpan(image, bracketStart, bracketEnd + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            } else if (BuildConfig.DEBUG && marker.isNotEmpty()) {
                Log.d(TAG, "Not translated: $marker")
            }

            if (!isChat) {
                bracketEnd = text.indexOf(']', bracketEnd + 3)        // "]...[X..."
                bracketStart = text.lastIndexOf('[', bracketEnd - 2)
            } else {
                bracketEnd = builder.indexOf(']', bracketEnd + 1)
                bracketStart = builder.lastIndexOf('[', max(bracketEnd - 1, 0))
            }
        }
        return builder
    }

    fun extractExtras(text: String): String {
        // car messsages will put [] for any wechat emoji
        // but use an actual emoji for one's it represents using images (but not wechat emoji), i.e. apple emoji
        // what we do here is replace those emoji with [emoji] messages to be parsed later

        var replaced: String? = null
        if (EmojiManager.containsEmoji(text)) {
            replaced = EmojiParser.parseFromUnicode(text) {
                val emoji = extras[it.emoji.unicode]

                if (emoji != null) {
                    "[${emoji.keys[0]}]"
                } else {
                    it.emoji.unicode
                }
            }
        }

        return replaced ?: text
    }
}
