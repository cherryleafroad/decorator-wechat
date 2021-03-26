package com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class SettingsFragment : PreferenceFragmentCompat() {
    private val mSharedModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        exitTransition = inflater.inflateTransition(R.transition.slide_left)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.chat_history_settings, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // add method makes backgroung transparent
        view?.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.chat_userlist_bg,
                null
            )
        )

        val enabled = preferenceManager.findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_avatar))!!
        enabled.setOnPreferenceChangeListener { _, newValue ->
            mSharedModel.selfUserData.avatarEnabled = newValue as Boolean
            true
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri? = data?.data

                saveAvatarFromUri(uri)
            }
        }

        val select = preferenceManager.findPreference<Preference>(getString(R.string.pref_select_avatar))!!
        select.setOnPreferenceClickListener {
            val i = Intent()
            i.type = "image/*"
            i.action = Intent.ACTION_GET_CONTENT

            resultLauncher.launch(Intent.createChooser(i, "Select Picture"))

            true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val imagePreview =
                preferenceManager.findPreference<Preference>(getString(R.string.pref_image_preview))!! as ImageViewPreference

            val selfPath = mSharedModel.getSelfAvatarFilename()
            if (selfPath.isNotEmpty()) {
                val bmp = BitmapFactory.decodeFile(selfPath)

                activity?.runOnUiThread {
                    imagePreview.setBitmap(bmp)
                }
            }
        }

        return view
    }

    private fun setImagePreview(bmp: Bitmap) {
        val imagePreview =
            preferenceManager.findPreference<Preference>(getString(R.string.pref_image_preview))!! as ImageViewPreference
        imagePreview.setBitmap(bmp)
    }

    private fun saveAvatarFromUri(uri: Uri?) {
        context?.contentResolver?.openInputStream(uri!!)?.let {
            val bmp = BitmapFactory.decodeStream(it)
            setImagePreview(bmp)
            mSharedModel.selfUserData.avatar = BitmapDrawable(resources, bmp)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val userYou = mSharedModel.getUserSelf()

                    val hash = userYou.username.hashCode()
                    val file = File(requireContext().cacheDir, "$hash.png")

                    val out = FileOutputStream(file)
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()

                    mSharedModel.saveAvatar(Avatar(userYou.u_id, file.absolutePath))
                } catch (e: FileNotFoundException) {
                    Log.w(TAG, "User selected avatar not saved because file missing")
                } catch (e: IOException) {
                    Log.w(TAG, "IOException saving avatar: $e")
                }
            }
        }
    }
}

class ImageViewPreference(context: Context?, attrs: AttributeSet?) : Preference(context, attrs) {
    private var imageView: ImageView? = null
    private var imageListener: View.OnClickListener? = null
    private var imageBitmap: Bitmap? = null

    //onBindViewHolder() will be called after we call setImageClickListener() from SettingsFragment
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        imageView = (holder.findViewById(R.id.avatar) as ImageView)

        if (imageBitmap != null) {
            imageView!!.setImageBitmap(imageBitmap)
        }
    }

    fun setImageClickListener(onClickListener: View.OnClickListener?) {
        imageListener = onClickListener
    }

    fun setBitmap(bmp: Bitmap) {
        imageBitmap = bmp

        imageView?.setImageBitmap(bmp)
    }
}
