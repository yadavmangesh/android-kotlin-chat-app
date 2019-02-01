package com.inscripts.cometchatpulse.Fragment


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.models.TextMessage
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.Activities.UserProfileViewActivity
import com.inscripts.cometchatpulse.Adapter.OneToOneAdapter
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.CustomView.AttachmentTypeSelector
import com.inscripts.cometchatpulse.CustomView.StickyHeaderDecoration
import com.inscripts.cometchatpulse.Helpers.*
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.StringContract.IntentString.Companion.EXTRA_MIME_DOC
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.Utils.FileUtil
import com.inscripts.cometchatpulse.ViewModel.OnetoOneViewModel
import com.inscripts.cometchatpulse.databinding.FragmentContactDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class OneToOneFragment : Fragment(), View.OnClickListener, RecordListener {

    companion object {
        const val ARG_PARAM1 = "param1"
        const val ARG_PARAM2 = "param2"
    }

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var oneToOneAdapter: OneToOneAdapter

    private var ownerId: String

    lateinit var binding: FragmentContactDetailBinding

    private lateinit var userId: String

    private lateinit var config: Configuration

    private var status: String? = null

    private var name: String? = null

    private var avatar: String? = null

    private var uid: String? = null

    private lateinit var onetoOneViewModel: OnetoOneViewModel

    private var audioFileNamewithPath: String? = null

    private var parentJob = Job()

    private lateinit var clickListener: OnBackArrowClickListener

    var mediaRecorder: MediaRecorder? = null

    var count: Int = 0

    private var attachmentTypeSelector: AttachmentTypeSelector? = null

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    var currentScrollPosition = 0

    var scrollFlag: Boolean = true


    init {
        ownerId = CometChat.getLoggedInUser().uid

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_contact_detail,
                container, false)

        setHasOptionsMenu(true)


        name = arguments?.getString(StringContract.IntentString.USER_NAME)

        status = arguments?.getString(StringContract.IntentString.USER_STATUS)

        avatar = arguments?.getString(StringContract.IntentString.USER_AVATAR)

        uid = arguments?.getString(StringContract.IntentString.USER_ID)

        binding.name = name

        binding.status = status

        binding.avatar = avatar

        binding.lastActive = arguments?.getLong(StringContract.IntentString.LAST_ACTIVE)

        userId = arguments?.getString(StringContract.IntentString.USER_ID)!!

        onetoOneViewModel = OnetoOneViewModel(CometChatPro.applicationContext() as Application)

        binding.messageBox?.recordButton?.setListenForRecord(true)

        binding.messageBox?.recordAudioView?.setCancelOffset(16.toFloat())

        binding.messageBox?.recordAudioView?.setLessThanSecondAllowed(false)

        binding.messageBox?.recordAudioView?.setSlideToCancelText(getString(R.string.slide_to_cancel))

        binding.messageBox?.recordAudioView?.setCustomSounds(R.raw.record_start, R.raw.record_finished, R.raw.record_error)

        binding.messageBox?.recordButton?.setRecordAudio(binding.messageBox?.recordAudioView)

        binding.messageBox?.recordAudioView?.setOnRecordListener(this)

        config = activity?.resources?.configuration!!

        clickListener = context as OnBackArrowClickListener

        linearLayoutManager = LinearLayoutManager(activity)
        binding.recycler.layoutManager = linearLayoutManager

        binding.title.typeface = StringContract.Font.name
        binding.subTitle.typeface = StringContract.Font.status

        binding.recycler.getItemAnimator()?.changeDuration = 0
        oneToOneAdapter = OneToOneAdapter(context!!, CometChat.getLoggedInUser().uid)
        binding.recycler.addItemDecoration(StickyHeaderDecoration(oneToOneAdapter))
        binding.recycler.setRecyclerListener(RecycleListenerHelper())

        binding.recycler.adapter = oneToOneAdapter


        (activity as AppCompatActivity).setSupportActionBar(binding.cometchatToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.cometchatToolbar.title = ""

        binding.cometchatToolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.cometchatToolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        binding.subTitle.isSelected = true

        binding.contactPic.borderColor = StringContract.Color.white
        binding.contactPic.borderWidth = 2


        binding.messageBox?.buttonSendMessage?.setOnClickListener(this)
        binding.messageBox?.ivAttchment?.setOnClickListener(this)
        binding.rlTitlecontainer.setOnClickListener(this)


        if (StringContract.AppDetails.theme == Appearance.AppTheme.AZURE_RADIANCE) {
            binding.title.setTextColor(StringContract.Color.black)
            binding.subTitle.setTextColor(StringContract.Color.black)
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)
        } else {
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.ivAttchment?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.messageBox?.recordButton?.drawable?.setColorFilter(StringContract.Color.primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.title.setTextColor(StringContract.Color.white)
            binding.subTitle.setTextColor(StringContract.Color.white)
            binding.messageBox?.buttonSendMessage?.drawable?.setColorFilter(StringContract.Color.white, PorterDuff.Mode.SRC_ATOP)
        }

        binding.cometchatToolbar.overflowIcon?.setColorFilter(StringContract.Color.iconTint, PorterDuff.Mode.SRC_ATOP)

        binding.messageBox?.buttonSendMessage?.backgroundTintList = ColorStateList.valueOf(StringContract.Color.primaryColor)


        scope.launch(Dispatchers.IO) {
            onetoOneViewModel.fetchMessage(LIMIT = 30, userId = userId)

        }

        onetoOneViewModel.messageList.observe(this, Observer { messages ->
            messages?.let {
                oneToOneAdapter.setMessageList(it)
                if (scrollFlag) {
                    scrollBottom()
                    scrollFlag = false
                }
            }
        })

        onetoOneViewModel.user.observe(this, Observer { user ->
            user?.let {
                if (user.uid.equals(userId)) {
                    binding.lastActive = it.lastActiveAt
                    binding.status = it.status
                }
            }
        })

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                binding.cometchatToolbar.isSelected = binding.recycler.canScrollVertically(-1)

                if (!recyclerView.canScrollVertically(-1)) {
                    onetoOneViewModel.fetchMessage(LIMIT = 30, userId = userId)
                    scrollFlag = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }

        })

        return binding.root
    }

    fun scrollBottom() {
        binding.recycler.scrollToPosition(oneToOneAdapter.itemCount - 1)
    }


    override fun onRecordCancel() {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(true)
    }

    override fun onRecordFinish(time: Long) {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(false)

        if (audioFileNamewithPath != null) {
            Logger.error("audioFileNamewithPath", audioFileNamewithPath)
            onetoOneViewModel.sendMediaMessage(audioFileNamewithPath, CometChatConstants.MESSAGE_TYPE_AUDIO, userId)

        }

    }

    override fun onRecordLessTime() {
        binding.messageBox?.editTextChatMessage?.hint = getString(R.string.type_your_message)
        stopRecording(true)
    }

    override fun onRecordStart() {
        binding.messageBox?.editTextChatMessage?.hint = ""
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
                mediaRecorder?.reset()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        menu?.clear()
        inflater?.inflate(R.menu.option_menu, menu)

        val audioCall = menu?.findItem(R.id.voice_call)
        val videoCall = menu?.findItem(R.id.video_call)

        menu?.findItem(R.id.menu_leave)?.setVisible(false)

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

                    onetoOneViewModel.initCall(context!!, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VOICE_CALL)
                }

            }

            R.id.video_call -> {
                if (CCPermissionHelper.hasPermissions(activity, *arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO))) {

                    onetoOneViewModel.initCall(context!!, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO)
                } else {
                    CCPermissionHelper.requestPermissions(activity as Activity, arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA, CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO),
                            StringContract.RequestCode.VIDEO_CALL)
                }


            }
        }
        return true
    }


    override fun onClick(p0: View?) {

        when (p0?.id) {


            R.id.buttonSendMessage -> {

                val messageText: String? = binding.messageBox?.editTextChatMessage?.text.toString().trim()

                if (messageText != null && !messageText.isEmpty()) {

                    val textMessage = TextMessage(userId, messageText, CometChatConstants.MESSAGE_TYPE_TEXT, CometChatConstants.RECEIVER_TYPE_USER)
                    val J = JSONObject()
                    J.put("path", "stringfile")
                    textMessage.metadata = J

                    binding.messageBox?.editTextChatMessage?.setText("")

                    onetoOneViewModel.sendTextMessage(textMessage)

                    scrollFlag = true

                }
            }

            R.id.rl_titlecontainer -> {

                val profilViewIntent = Intent(context, UserProfileViewActivity::class.java)
                profilViewIntent.putExtra(StringContract.IntentString.USER_NAME, name)
                profilViewIntent.putExtra(StringContract.IntentString.USER_ID, uid)
                profilViewIntent.putExtra(StringContract.IntentString.USER_STATUS, status)
                profilViewIntent.putExtra(StringContract.IntentString.USER_AVATAR, avatar)
                startActivity(profilViewIntent)

            }

            R.id.iv_attchment -> {
                showPopUp()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            StringContract.RequestCode.ADD_DOCUMENT ->

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AttachmentHelper.selectMedia(activity, "", EXTRA_MIME_DOC)
                } else {
                    showToast()
                }

            StringContract.RequestCode.ADD_GALLERY ->

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.selectMedia(activity, "", StringContract.IntentString.EXTRA_MIME_TYPE)

                    startActivityForResult(intent, StringContract.RequestCode.ADD_GALLERY)

                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_PHOTO ->
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    val intent = AttachmentHelper.captureImage()
                    startActivityForResult(intent, StringContract.RequestCode.TAKE_PHOTO)

                } else {
                    showToast()
                }

            StringContract.RequestCode.TAKE_VIDEO ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
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

                context?.let { onetoOneViewModel.initCall(it, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO) }

            } else {
                showToast()
            }
            StringContract.RequestCode.VIDEO_CALL -> if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                context?.let { onetoOneViewModel.initCall(it, userId, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO) }
            } else {
                showToast()
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK && data != null) {

            when (requestCode) {

                StringContract.RequestCode.ADD_GALLERY -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], filePath[1], userId)
                    scrollFlag = true
                }
                StringContract.RequestCode.TAKE_PHOTO -> {
                    val filePath = AttachmentHelper.handleCameraImage(context, data)
                    scrollFlag = true
                    onetoOneViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_IMAGE, userId)

                }

                StringContract.RequestCode.TAKE_VIDEO -> {
                    val filePath = AttachmentHelper.handleCameraVideo(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath, CometChatConstants.MESSAGE_TYPE_VIDEO, userId)
                    scrollFlag = true

                }

                StringContract.RequestCode.ADD_SOUND -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], CometChatConstants.MESSAGE_TYPE_AUDIO, userId)
                    scrollFlag = true
                }

                StringContract.RequestCode.ADD_DOCUMENT -> {
                    val filePath = AttachmentHelper.handleFile(context, data)
                    onetoOneViewModel.sendMediaMessage(filePath[0], filePath[1], userId)
                    scrollFlag = true

                }
            }
        }

    }


    private fun showToast() {
        Toast.makeText(context, "PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
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
                    CCPermissionHelper.requestPermissions(activity as Activity, StringContract.RequestPermission.STORAGE_PERMISSION, StringContract.RequestCode.ADD_DOCUMENT)
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

            StringContract.RequestCode.LOCATION -> {

                try {

                    if (CommonUtil.checkPermission(context!!)) {
                        val locationIntent = Intent(context, LocationActivity::class.java)
                        locationIntent.putExtra(StringContract.IntentString.ID, userId)
                        locationIntent.putExtra(StringContract.IntentString.RECIVER_TYPE,
                                CometChatConstants.RECEIVER_TYPE_USER)
                        startActivity(locationIntent)
                    } else {
                        showDialog("App requires to access your location", "Enable Location")
                    }

                } catch (e: Exception) {

                }
            }


        }
    }

    fun hideKeyboard( activity:Activity?) {
       var  imm =activity?. getSystemService (Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var  view = activity.getCurrentFocus ();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = activity as View
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private fun showDialog(message: String, title: String) {
        val builder = context?.let { android.support.v7.app.AlertDialog.Builder(it) }
        builder?.setTitle(context?.let { CommonUtil.setTitle(title, it) })?.setMessage(message)
                ?.setCancelable(true)
                ?.setNegativeButton(context?.let { CommonUtil.setTitle("Cancel", it) }) { dialogInterface, i -> dialogInterface.dismiss() }
                ?.setPositiveButton(context?.let { CommonUtil.setTitle("Go to settings to enable", it) }) { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }?.show()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
        onetoOneViewModel.receiveMessageListener(StringContract.ListenerName.MESSAGE_LISTENER, ownerId)
        onetoOneViewModel.addPresenceListener(StringContract.ListenerName.USER_LISTENER)
    }

    override fun onStop() {
        super.onStop()
        parentJob.cancel()
        onetoOneViewModel.removeMessageListener(StringContract.ListenerName.MESSAGE_LISTENER)
        onetoOneViewModel.removePresenceListener(StringContract.ListenerName.USER_LISTENER)
        stopRecording(false)
    }


}
