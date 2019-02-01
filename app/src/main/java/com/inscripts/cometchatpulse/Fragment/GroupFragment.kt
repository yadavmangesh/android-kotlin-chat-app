package com.inscripts.cometchatpulse.Fragment


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.media.MediaRecorder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.models.TextMessage
import com.inscripts.cometchatpulse.Activities.GroupDetailActivity
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.Adapter.GroupChatAdapter
import com.inscripts.cometchatpulse.CustomView.AttachmentTypeSelector
import com.inscripts.cometchatpulse.CustomView.StickyHeaderDecoration
import com.inscripts.cometchatpulse.Helpers.AttachmentHelper
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Helpers.OnBackArrowClickListener
import com.inscripts.cometchatpulse.Helpers.RecordListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import com.inscripts.cometchatpulse.databinding.FragmentGroupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class GroupFragment : Fragment(), View.OnClickListener, RecordListener {

    private lateinit var binding: FragmentGroupBinding

    private lateinit var guid: String

    private lateinit var groupChatViewModel: GroupChatViewModel

    private lateinit var config: Configuration

    private lateinit var groupViewModel: GroupViewModel

    private var audioFileNamewithPath: String? = null

    private lateinit var clickListener: OnBackArrowClickListener

    private  var groupDescription:String?=null

    var mediaRecorder: MediaRecorder? = null

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var groupChatAdapter: GroupChatAdapter

    private lateinit var groupName: String

    private var attachmentTypeSelector: AttachmentTypeSelector? = null

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    var scrollFlag:Boolean=true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_group,
                container, false)

        setHasOptionsMenu(true)

        guid = arguments?.getString(StringContract.IntentString.GROUP_ID).toString()

        groupName = arguments?.getString(StringContract.IntentString.GROUP_NAME).toString()

        binding.name = arguments?.getString(StringContract.IntentString.GROUP_NAME)

        binding.icon = arguments?.getString(StringContract.IntentString.GROUP_ICON)

        groupDescription=arguments?.getString(StringContract.IntentString.GROUP_DESCRIPTION)

        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel::class.java)

        config = activity?.resources?.configuration!!

        clickListener = context as OnBackArrowClickListener

        linearLayoutManager = LinearLayoutManager(activity)
        binding.recycler.layoutManager = linearLayoutManager


        binding.recycler.getItemAnimator()?.changeDuration = 0
        groupChatAdapter = GroupChatAdapter(context!!, guid, CometChat.getLoggedInUser().uid)
        binding.recycler.adapter = groupChatAdapter

        binding.recycler.addItemDecoration(StickyHeaderDecoration(groupChatAdapter))



        (activity as AppCompatActivity).setSupportActionBar(binding.cometchatToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cometchatToolbar.title = ""

        binding.cometchatToolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.title.typeface=StringContract.Font.name
        binding.subTitle.typeface=StringContract.Font.status

        binding.messageBox?.buttonSendMessage?.setOnClickListener(this)
        binding.messageBox?.ivAttchment?.setOnClickListener(this)
        binding.rlTitlecontainer.setOnClickListener(this)

        binding.messageBox?.recordButton?.setListenForRecord(true)

        binding.messageBox?.recordAudioView?.setCancelOffset(8.toFloat())

        binding.messageBox?.recordAudioView?.setLessThanSecondAllowed(false)

        binding.messageBox?.recordAudioView?.setSlideToCancelText(getString(R.string.slide_to_cancel))

        binding.messageBox?.recordAudioView?.setCustomSounds(R.raw.record_start,
                R.raw.record_finished, R.raw.record_error)

        binding.messageBox?.recordButton?.setRecordAudio(binding.messageBox?.recordAudioView)

        binding.messageBox?.recordAudioView?.setOnRecordListener(this)

        binding.cometchatToolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.cometchatToolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        binding.messageBox?.buttonSendMessage?.backgroundTintList =
                ColorStateList.valueOf(StringContract.Color.primaryColor)


        if (StringContract.AppDetails.theme== Appearance.AppTheme.AZURE_RADIANCE) {
            binding.title.setTextColor(StringContract.Color.black)
            binding.subTitle.setTextColor(StringContract.Color.black)
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)
        }
        else{
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.title.setTextColor(StringContract.Color.white)
            binding.subTitle.setTextColor(StringContract.Color.white)
            binding.messageBox?.buttonSendMessage?.drawable?.
                    setColorFilter(StringContract.Color.white,PorterDuff.Mode.SRC_ATOP)
        }
        binding.contactPic.borderColor = StringContract.Color.white
        binding.contactPic.borderWidth = 2

        binding.cometchatToolbar.overflowIcon?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)

        Thread {
            groupChatViewModel.fetchMessage(LIMIT = 30, guid = guid)

        }.start()


        Thread {
            groupViewModel.fetchGroupMemeber(LIMIT = 30, guid = guid)
        }.start()


        groupChatViewModel.messageList.observe(this, Observer { messages ->
            messages?.let {
                groupChatAdapter.setMessageList(it)
                if (scrollFlag) {
                    scrollBottom()
                    scrollFlag=false
                }
            }
        })

        groupViewModel.groupMemberList.observe(this, Observer { groupMemeberList ->
            groupMemeberList?.let {
                setGroupMemberList(it, binding.subTitle)

            }
        })

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                binding.cometchatToolbar.isSelected = binding.recycler.canScrollVertically(-1)

                 if (!recyclerView.canScrollVertically(-1)) {
                     groupChatViewModel.fetchMessage(LIMIT = 30, guid = guid)


                 }

            }

        })


        return binding.root
    }

    private fun scrollBottom(){
        binding.recycler.scrollToPosition(groupChatAdapter.itemCount-1)
    }

    override fun onRecordCancel() {
        binding.messageBox?.editTextChatMessage?.setHint(getString(R.string.type_your_message))
        stopRecording(true)
    }

    override fun onRecordFinish(time: Long) {
        binding.messageBox?.editTextChatMessage?.setHint(getString(R.string.type_your_message))
        stopRecording(false)

        if (audioFileNamewithPath != null) {
            Logger.error("audioFileNamewithPath", audioFileNamewithPath)
            groupChatViewModel.sendMediaMessage(audioFileNamewithPath, CometChatConstants.MESSAGE_TYPE_AUDIO, guid)

        }

    }

    override fun onRecordLessTime() {
        binding.messageBox?.editTextChatMessage?.setHint(getString(R.string.type_your_message))
        stopRecording(true)
    }

    override fun onRecordStart() {
        binding.messageBox?.editTextChatMessage?.setHint("")
        startRecording()
    }


    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            audioFileNamewithPath = FileUtil.getOutputMediaFile(context).toString()
            mediaRecorder?.setOutputFile(audioFileNamewithPath)

            try {
                mediaRecorder?.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mediaRecorder?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopRecording(isCancel: Boolean) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                if (isCancel) {
                    File(audioFileNamewithPath).delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.option_menu, menu)

        val audioCall = menu?.findItem(R.id.voice_call)
        val videoCall = menu?.findItem(R.id.video_call)

        val leaveMenu = menu?.findItem(R.id.menu_leave)
        leaveMenu?.setVisible(true)
        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            val drawable = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_more_vert) }
            drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
            leaveMenu?.setIcon(drawable)
        }

        audioCall?.icon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        videoCall?.icon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            android.R.id.home -> {

                if (config.smallestScreenWidthDp >= 600) {
                    clickListener.onBackClick()
                } else {
                    activity?.onBackPressed()
                }
            }
            R.id.voice_call -> {

                if (CCPermissionHelper.hasPermissions(activity, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    groupViewModel.initCall(context, guid, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_AUDIO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VOICE_CALL)
                }
            }

            R.id.video_call -> {

                if (CCPermissionHelper.hasPermissions(activity, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    groupViewModel.initCall(context, guid, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_VIDEO)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VIDEO_CALL)
                }

            }

            R.id.menu_leave -> {

                groupViewModel.leaveGroup(guid, activity)
            }


        }
        return true
    }


    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.buttonSendMessage -> {

                val messageText: String? = binding.messageBox?.editTextChatMessage?.text.toString().trim()

                if (messageText != null && !messageText.isEmpty()) {

                    val textMessage = TextMessage(guid, messageText, CometChatConstants.MESSAGE_TYPE_TEXT,
                            CometChatConstants.RECEIVER_TYPE_GROUP)

                    binding.messageBox?.editTextChatMessage?.setText("")

                    groupChatViewModel.sendTextMessage(textMessage)
                    scrollFlag=true
                }
            }

            R.id.iv_attchment -> {
                showPopUp()
            }

            R.id.rl_titlecontainer -> {
                val groupDetailIntent = Intent(context, GroupDetailActivity::class.java)
                groupDetailIntent.putExtra(StringContract.IntentString.GROUP_ID, guid)
                groupDetailIntent.putExtra(StringContract.IntentString.GROUP_ICON,
                        arguments?.getString(StringContract.IntentString.GROUP_ICON))
                groupDetailIntent.putExtra(StringContract.IntentString.GROUP_OWNER,
                        arguments?.getString(StringContract.IntentString.GROUP_OWNER))

                groupDetailIntent.putExtra(StringContract.IntentString.GROUP_DESCRIPTION
                        ,groupDescription)

                groupDetailIntent.putExtra(StringContract.IntentString.GROUP_NAME,
                        arguments?.getString(StringContract.IntentString.GROUP_NAME))
                startActivity(groupDetailIntent)

            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        when (requestCode) {
            StringContract.RequestCode.ADD_DOCUMENT ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AttachmentHelper.selectMedia(activity, "*/*", StringContract.IntentString.EXTRA_MIME_DOC)
                } else {
                    showToast()
                }

            StringContract.RequestCode.ADD_GALLERY ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.selectMedia(activity, "*/*",
                            StringContract.IntentString.EXTRA_MIME_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_GALLERY)
                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_PHOTO ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.captureImage()
                    startActivityForResult(intent, StringContract.RequestCode.TAKE_PHOTO)

                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_VIDEO ->

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.captureVideo()

                    startActivityForResult(intent, StringContract.RequestCode.TAKE_VIDEO)

                } else {
                    showToast()
                }

            StringContract.RequestCode.RECORD_CODE ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showToast()
                }

            StringContract.RequestCode.ADD_SOUND -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                AttachmentHelper.selectMedia(activity, StringContract.IntentString.AUDIO_TYPE, null)
            } else {
                showToast()
            }
            StringContract.RequestCode.VOICE_CALL -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                groupViewModel.initCall(context, guid, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_AUDIO)

            } else {
                showToast()
            }
            StringContract.RequestCode.VIDEO_CALL -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                groupViewModel.initCall(context, guid, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_VIDEO)

            } else {
                showToast()
            }

        }

    }

    private fun showToast() {
        Toast.makeText(context, "PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null) {

            when (requestCode) {

                StringContract.RequestCode.ADD_GALLERY -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    groupChatViewModel.sendMediaMessage(filePath[0], filePath[1], guid)

                    scrollFlag=true
                }
                StringContract.RequestCode.TAKE_PHOTO -> {
                    val filePath = AttachmentHelper.handleCameraImage(context, data)
                    groupChatViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_IMAGE, guid)
                    scrollFlag=true
                }

                StringContract.RequestCode.TAKE_VIDEO -> {
                    val filePath = AttachmentHelper.handleCameraVideo(context, data)
                    groupChatViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_VIDEO, guid)
                    scrollFlag=true
                }

                StringContract.RequestCode.ADD_SOUND -> {
                }

                StringContract.RequestCode.ADD_DOCUMENT -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    groupChatViewModel.sendMediaMessage(filePath[0], filePath[1], guid)
                    scrollFlag=true

                }
            }
        }

    }


    private fun showPopUp() {

        try {
            if (attachmentTypeSelector == null) {
                attachmentTypeSelector = AttachmentTypeSelector(context!!, AttachmentTypeListener())
            }
            attachmentTypeSelector!!.show(activity as Activity, binding.messageBox?.ivAttchment as View)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class AttachmentTypeListener : AttachmentTypeSelector.AttachmentClickedListener {
        override fun onClick(type: Int) {
            addAttachment(type, activity)
        }
    }

    fun addAttachment(type: Int, activity: FragmentActivity?) {
        when (type) {
            StringContract.RequestCode.ADD_GALLERY ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, "*/*",
                            StringContract.IntentString.EXTRA_MIME_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_GALLERY)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_GALLERY)
                }
            StringContract.RequestCode.ADD_DOCUMENT ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, "*/*",
                            StringContract.IntentString.DOCUMENT_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_DOCUMENT)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION,
                            StringContract.RequestCode.ADD_DOCUMENT)
                }
            StringContract.RequestCode.ADD_SOUND ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.STORAGE_PERMISSION)) {

                    val intent = AttachmentHelper.selectMedia(activity, StringContract.IntentString.AUDIO_TYPE,
                            null)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_SOUND)

                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_SOUND)
                }

            StringContract.RequestCode.TAKE_PHOTO ->

                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.CAMERA_PERMISSION)) {

                    val intent = AttachmentHelper.captureImage()

                    startActivityForResult(intent, StringContract.RequestCode.TAKE_PHOTO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.CAMERA_PERMISSION, StringContract.RequestCode.TAKE_PHOTO)
                }
            StringContract.RequestCode.TAKE_VIDEO ->
                if (CCPermissionHelper.hasPermissions(activity, *StringContract.RequestPermission.CAMERA_PERMISSION)) {
                    val intent = AttachmentHelper.captureVideo()
                    startActivityForResult(intent, StringContract.RequestCode.TAKE_VIDEO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.CAMERA_PERMISSION, StringContract.RequestCode.TAKE_VIDEO)

                }

            StringContract.RequestCode.LOCATION ->
                locationIntent()

        }
    }


    private fun locationIntent(){
        val locationIntent = Intent(context, LocationActivity::class.java)
        locationIntent.putExtra(StringContract.IntentString.ID,guid)
        locationIntent.putExtra(StringContract.IntentString.RECIVER_TYPE,CometChatConstants.RECEIVER_TYPE_GROUP)
        startActivity(locationIntent)
    }


    private fun setGroupMemberList(list: MutableList<GroupMember>, subtitle: TextView) {
        val builder = StringBuilder()

        for (member in list) {

            builder.append(member.user.name)
            builder.append(",")

        }
        val memeberList = builder.deleteCharAt(builder.length - 1).toString()
        Logger.errorLong("LISTMEMBER", memeberList)
        subtitle.text=memeberList
    }

    override fun onStart() {
        super.onStart()
        groupChatViewModel.addGroupEventListener(StringContract.ListenerName.GROUP_EVENT_LISTENER)
        groupChatViewModel.addGroupMessageListener(StringContract.ListenerName.MESSAGE_LISTENER,CometChat.getLoggedInUser().uid)
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()
        stopRecording(false)
        groupChatViewModel.removeGroupEventListener(StringContract.ListenerName.GROUP_EVENT_LISTENER)
        groupChatViewModel.removeMessageListener(StringContract.ListenerName.MESSAGE_LISTENER)
    }


}
