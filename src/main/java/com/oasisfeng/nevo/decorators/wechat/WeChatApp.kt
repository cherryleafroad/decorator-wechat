package com.oasisfeng.nevo.decorators.wechat

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProviderClient
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.Log
import androidx.room.Room
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.DatabaseHelpers


class WeChatApp : Application() {
    companion object {
        const val SETTINGS_PROVIDER = "com.oasisfeng.nevo.settings"
    }

    @JvmField
    var whenMap: ArrayMap<String, Long> = ArrayMap()
    var replying = false
    var isRecasted = false
    var sharedPreferences: SharedPreferences? = null
    var settingSynchronousRemoval = false
    var settingInsiderMode = false
    lateinit var db: AppDatabase
    lateinit var dbHelpers: DatabaseHelpers

    private val resolver = SettingsObserver(Handler(Looper.getMainLooper()))


    override fun onCreate() {
        super.onCreate()

        val url = "content://$SETTINGS_PROVIDER/"
        val data = Uri.parse(url)
        // normally you would unregister this, but we expect this to update throughout the application
        // lifetime. If the application gets killed, it will restart next time, so no biggie
        this.contentResolver.registerContentObserver(data, true, resolver)

        // initial setup of setting values
        val insiderMode = Uri.parse("/insider_mode")
        val synchronousRemoval = Uri.parse("/synchronous_removal")
        queryAndUpdateSetting(insiderMode)
        queryAndUpdateSetting(synchronousRemoval)

        dbHelpers = DatabaseHelpers()

        // I'd like to get rid of this if the feature is disabled
        // but initializing it later is too much work, better to leave it ready
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "messages"
        )
            .enableMultiInstanceInvalidation()
            .build()
    }

    @SuppressLint("Recycle")
    private fun queryAndUpdateSetting(uri: Uri?) {
        val fullUri = Uri.parse("content://$SETTINGS_PROVIDER${uri?.path}")

        val provider: ContentProviderClient? = this.contentResolver.acquireContentProviderClient(SETTINGS_PROVIDER)
        val cursor = provider?.query(fullUri, null, null, null, null)
        try {
            var res: Boolean? = null
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        res = cursor.getString(0).toBoolean()
                    } while (cursor.moveToNext())
                }
            }

            Log.d(WeChatDecorator.TAG, "Setting ${uri?.lastPathSegment} changed: $res")
            updateSetting(uri, res)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            cursor?.close()
            provider?.close()
        }
    }

    private fun updateSetting(path: Uri?, res: Boolean?) {
        when (path?.lastPathSegment) {
            "synchronous_removal" -> settingSynchronousRemoval = res!!
            "insider_mode" -> settingInsiderMode = res!!
        }
    }

    private inner class SettingsObserver(handler: Handler) : ContentObserver(handler) {
        override fun deliverSelfNotifications(): Boolean {
            return false
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            queryAndUpdateSetting(uri)
        }
    }

    private fun String.toBoolean() = this == "1"
}
