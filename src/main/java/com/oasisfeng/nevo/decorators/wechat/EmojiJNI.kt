package com.oasisfeng.nevo.decorators.wechat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.ArrayMap
import org.apache.commons.io.IOUtils
import java.io.InputStream

object EmojiJNI {
    init {
        System.loadLibrary("emoji")
    }

    private val bitmapCache: ArrayMap<String, Bitmap> = ArrayMap()
    private val bytesCache: ArrayMap<String, ByteArray> = ArrayMap()

    external fun nativeInit(context: Context)
    private external fun initData(bytes: ByteArray)
    private external fun getChunkBytes(key: String): ByteArray?
    private external fun getChunkBitmap(key: String): Bitmap?
    external fun getConfig(): ByteArray
    private external fun isInit(): Boolean

    fun initialize(context: Context) {
        if (!isInit()) {
            val stream: InputStream = context.assets.open("emojidata")
            val bytes = IOUtils.toByteArray(stream)
            initData(bytes)
        }
    }

    fun getEmojiBitmapNative(key: String): Bitmap? {
        return if (bitmapCache.containsKey(key)) {
            bitmapCache[key]
        } else {
            val bitmap = getChunkBitmap(key)
            if (bitmap != null) {
                bitmapCache[key] = bitmap
            }

            bitmap
        }
    }

    fun getEmojiBitmap(key: String): Bitmap? {
        return if (bitmapCache.containsKey(key)) {
            bitmapCache[key]
        } else {
            val bytes = getChunkBytes(key)
            var bitmap: Bitmap? = null
            if (bytes != null) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmapCache[key] = bitmap
            }

            bitmap
        }
    }

    fun getEmojiBytes(key: String): ByteArray? {
        return if (bytesCache.containsKey(key)) {
            bytesCache[key]
        } else {
            val bytes = getChunkBytes(key)
            if (bytes != null) {
                bytesCache[key] = bytes
            }

            bytes
        }
    }
}
