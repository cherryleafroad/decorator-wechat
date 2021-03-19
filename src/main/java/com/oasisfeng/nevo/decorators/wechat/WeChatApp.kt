package com.oasisfeng.nevo.decorators.wechat

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProviderClient
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.Log
import androidx.annotation.RequiresApi
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG


class WeChatApp : Application() {
    companion object {
        const val SETTINGS_PROVIDER = "com.oasisfeng.nevo.settings"
    }

    @JvmField
    var whenMap: ArrayMap<String, Long> = ArrayMap()
    var replying = false
    var sharedPreferences: SharedPreferences? = null
    var settingSynchronousRemoval = false
    var settingInsiderMode = false

    private lateinit var resolver: SettingsObserver
    private var isUIProcess = false


    //@RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()

        //isUIProcess = getProcessName().substringAfter(':', "") == "ui"
        // for now, never run
        isUIProcess = true

        // this feature is not used for UI process. It's redundant
        if (!isUIProcess) {
            resolver = SettingsObserver(Handler(Looper.getMainLooper()))

            val url = "content://$SETTINGS_PROVIDER/"
            val data = Uri.parse(url)

            this.contentResolver.registerContentObserver(data, true, resolver)

            // initial setup of setting values
            val insiderMode = Uri.parse("/insider_mode")
            val synchronousRemoval = Uri.parse("/synchronous_removal")
            queryAndUpdateSetting(insiderMode)
            queryAndUpdateSetting(synchronousRemoval)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        if (!isUIProcess) {
            this.contentResolver.unregisterContentObserver(resolver)
        }
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

            // in case it was still null
            res = res ?: false

            Log.d(TAG, "Setting ${uri?.lastPathSegment} changed: $res")
            updateSetting(uri, res)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            cursor?.close()
            provider?.close()
        }
    }

    private fun updateSetting(path: Uri?, res: Boolean) {
        when (path?.lastPathSegment) {
            "synchronous_removal" -> settingSynchronousRemoval = res
            "insider_mode" -> settingInsiderMode = res
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
