package com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter.UserAdapter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter.UserAdapterOnClickListener
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.UserWithMessageAndAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.databinding.FragmentUserlistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserListFragment : Fragment(), UserAdapterOnClickListener {
    private var _mBinding: FragmentUserlistBinding? = null
    val mBinding
        get() = _mBinding!!

    private var mAdapterData = mutableListOf<UserWithMessageAndAvatar>()
    private lateinit var mAdapter: UserAdapter

    private val mSharedModel: SharedViewModel by activityViewModels()

    companion object {
        const val ACTION_NOTIFY_USER_CHANGE = "NOTIFY_USER_CHANGE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _mBinding = FragmentUserlistBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.toolbar.apply {
            inflateMenu(R.menu.userlist_menu)
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }

        mAdapter = UserAdapter(requireContext(), this, mAdapterData)
        val layout = LinearLayoutManager(requireContext())
        mBinding.apply {
            toolbarTitle.text = getString(R.string.wechat_title)

            userRecycler.adapter = mAdapter
            userRecycler.layoutManager = layout
        }

        mSharedModel.userList.observe(viewLifecycleOwner, {
            it ?: return@observe

            mAdapterData.apply {
                clear()
                addAll(it)
            }
            mAdapter.notifyDataSetChanged()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_all -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_chat_all))
                    .setMessage(getString(R.string.delete_chat_all_summary))

                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        mSharedModel.deleteAllUsers()
                    }

                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun userOnClick(uid: Long, username: String) {
        mSharedModel.setChatData(uid, username)
    }

    override fun userOnLongClick(uid: Long, username: String): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_chat).replace("%s", username))
            .setMessage(
                getString(R.string.delete_chat_summary).replace(
                    "%s",
                    username
                )
            )

            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    mSharedModel.deleteUser(uid)
                    mSharedModel.refreshUserList()
                }
            }

            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
        return true
    }

    private val mUserlistChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mSharedModel.refreshUserList()
        }
    }

    override fun onResume() {
        super.onResume()

        // refresh when switching back
        mSharedModel.refreshUserList()

        val filter = IntentFilter()
        filter.addAction(ACTION_NOTIFY_USER_CHANGE)
        requireActivity().registerReceiver(mUserlistChanged, filter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mUserlistChanged)
    }
}
