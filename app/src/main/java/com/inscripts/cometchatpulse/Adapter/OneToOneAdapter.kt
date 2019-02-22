package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Handler
import android.provider.BaseColumns
import android.provider.MediaStore
import android.support.v4.util.LongSparseArray
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Activities.ImageViewActivity
import com.inscripts.cometchatpulse.CustomView.StickyHeaderAdapter
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.DateUtil
import com.inscripts.cometchatpulse.ViewHolder.*
import com.inscripts.cometchatpulse.databinding.*
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.util.*

class OneToOneAdapter(val context: Context, val ownerId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        StickyHeaderAdapter<TextHeaderHolder> {


    lateinit var viewHolder: RecyclerView.ViewHolder
    private var messagesList: MutableList<BaseMessage> = mutableListOf()


    private var currentPlayingSong: String? = null
    private var timerRunnable: Runnable? = null
    private val seekHandler = Handler()

    private var currentlyPlayingId = 0L

    var player: MediaPlayer? = null


    init {
        if (player == null) {
            player = MediaPlayer()
        }
        audioDurations = LongSparseArray()
        videoThumbnails = LongSparseArray()
    }

    companion object {
        private lateinit var audioDurations: LongSparseArray<Int>
        private lateinit var videoThumbnails: LongSparseArray<Bitmap>
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        when (p1) {

            StringContract.ViewType.RIGHT_TEXT_MESSAGE -> {
                val binding: RightTextBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_text, p0, false)
                return RightTextMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_TEXT_MESSAGE -> {
                val binding: LeftTextBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_text, p0, false)
                return LeftTextMessageHolder(binding)
            }

            StringContract.ViewType.RIGHT_IMAGE_MESSAGE -> {
                val binding: CcImageVideoLayoutRightBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_right, p0, false)
                return RightImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_IMAGE_MESSAGE -> {
                val binding: CcImageVideoLayoutLeftBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_left, p0, false)
                return LeftImageVideoMessageHolder(binding)

            }

            StringContract.ViewType.RIGHT_VIDEO_MESSAGE -> {

                val binding: CcImageVideoLayoutRightBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_right, p0, false)
                return RightImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_VIDEO_MESSAGE -> {
                val binding: CcImageVideoLayoutLeftBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cc_image_video_layout_left, p0, false)
                return LeftImageVideoMessageHolder(binding)
            }

            StringContract.ViewType.RIGHT_AUDIO_MESSAGE -> {
                val binding: RightAudioBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_audio, p0, false)
                return RightAudioMessageHolder(binding)
            }

            StringContract.ViewType.LEFT_AUDIO_MESSAGE -> {
                val binding: LeftAudioBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_audio, p0, false)
                return LeftAudioMessageHolder(binding)
            }

            StringContract.ViewType.CALL_MESSAGE -> {

                val binding: ListHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_header, p0, false)
                return TextHeaderHolder(binding)
            }

            StringContract.ViewType.RIGHT_FILE_MESSAGE -> {
                val binding: RightFileBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_file, p0, false)
                return RightFileViewHolder(binding)
            }

            StringContract.ViewType.LEFT_FILE_MESSAGE -> {
                val binding: LeftFileBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_file, p0, false)
                return LeftFileViewHolder(binding)
            }

            StringContract.ViewType.RIGHT_LOCATION_MESSAGE -> {
                val binding: RightLocationBinding = DataBindingUtil.inflate(layoutInflater, R.layout.right_location, p0, false)
                return RightLocationViewHolder(binding)
            }

            StringContract.ViewType.LEFT_LOCATION_MESSAGE -> {
                val binding: LeftLocationBinding = DataBindingUtil.inflate(layoutInflater, R.layout.left_location, p0, false)
                return LeftLocationViewHolder(binding)
            }

            else -> {
                return viewHolder
            }
        }

    }

    override fun getHeaderId(var1: Int): Long {
        return java.lang.Long.parseLong(DateUtil.getDateId(messagesList.get(var1).getSentAt() * 1000))
    }

    override fun onCreateHeaderViewHolder(var1: ViewGroup): TextHeaderHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val binding: ListHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_header, var1, false)
        return TextHeaderHolder(binding)

    }

    override fun onBindHeaderViewHolder(var1: TextHeaderHolder, var2: Int, var3: Long) {
        val date = Date(messagesList.get(var2).getSentAt() * 1000)

        val formattedDate = DateUtil.getCustomizeDate(date.getTime())

        var1.binding.txtMessageDate.setBackground(context.resources.getDrawable(R.drawable.cc_rounded_date_button))

        var1.binding.txtMessageDate.setText(formattedDate)

    }


    override fun getItemCount(): Int {

        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {

        if (messagesList.get(position).category.equals(CometChatConstants.CATEGORY_MESSAGE, ignoreCase = true)) {

            if (ownerId.equals(messagesList.get(position).sender.uid, ignoreCase = true)) {

                when (messagesList.get(position).type) {

                    CometChatConstants.MESSAGE_TYPE_TEXT -> {

                        if ((messagesList.get(position) as TextMessage).text.equals("custom_location")) {
                            return StringContract.ViewType.RIGHT_LOCATION_MESSAGE
                        }
                        return StringContract.ViewType.RIGHT_TEXT_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                        return StringContract.ViewType.RIGHT_IMAGE_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_AUDIO -> {
                        return StringContract.ViewType.RIGHT_AUDIO_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                        return StringContract.ViewType.RIGHT_VIDEO_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_FILE -> {

                        return StringContract.ViewType.RIGHT_FILE_MESSAGE
                    }


                }
            } else {

                when (messagesList.get(position).type) {

                    CometChatConstants.MESSAGE_TYPE_TEXT -> {

                        if ((messagesList.get(position) as TextMessage).text.equals("custom_location")) {

                            return StringContract.ViewType.LEFT_LOCATION_MESSAGE
                        }
                        return StringContract.ViewType.LEFT_TEXT_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                        return StringContract.ViewType.LEFT_IMAGE_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_AUDIO -> {
                        return StringContract.ViewType.LEFT_AUDIO_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                        return StringContract.ViewType.LEFT_VIDEO_MESSAGE
                    }

                    CometChatConstants.MESSAGE_TYPE_FILE -> {
                        return StringContract.ViewType.LEFT_FILE_MESSAGE
                    }

                }
            }
        } else if (messagesList.get(position).category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)) {
            return StringContract.ViewType.CALL_MESSAGE
        }

        return super.getItemViewType(position)
    }

    fun setMessageList(messageList: MutableList<BaseMessage>) {
        this.messagesList = messageList
        notifyDataSetChanged()

    }


    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val baseMessage = messagesList.get(p1)
        val timeStampLong = messagesList.get(p1).sentAt
        var message: String? = null
        var filePath: String? = null
        var mediaFile: String? = null

        if (baseMessage is MediaMessage) {
            if (baseMessage.metadata != null) {
                try {
                    Log.d("metaAdapter", baseMessage.metadata.toString())

                    filePath = baseMessage.metadata?.getString("path")
                    Log.d("CUSTOM", filePath)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }

        if (baseMessage is Call) {
            val user = baseMessage.callInitiator as User
            val callReceiver = baseMessage.callReceiver as User

            if (user.uid.equals(ownerId, ignoreCase = true)) {
                message = "You Call " + callReceiver.name
            } else {
                message = "You received call from " + user.name
            }
        }

        if (baseMessage is MediaMessage) {
            mediaFile = baseMessage.url
        }

        when (p0.itemViewType) {

            StringContract.ViewType.RIGHT_TEXT_MESSAGE -> {
                val rightTextMessageHolder = p0 as RightTextMessageHolder
                rightTextMessageHolder.binding.message = (baseMessage as TextMessage)
                rightTextMessageHolder.binding.tvMessage.typeface = StringContract.Font.message
                rightTextMessageHolder.binding.timestamp.typeface = StringContract.Font.status
                rightTextMessageHolder.binding.tvMessage.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
            }

            StringContract.ViewType.LEFT_TEXT_MESSAGE -> {
                val leftTextMessageHolder = p0 as LeftTextMessageHolder
                leftTextMessageHolder.binding.message = (baseMessage as TextMessage)
                leftTextMessageHolder.binding.tvMessage.typeface = StringContract.Font.message
                leftTextMessageHolder.binding.senderName.typeface = StringContract.Font.status
                leftTextMessageHolder.binding.timestamp.typeface = StringContract.Font.status
                leftTextMessageHolder.binding.tvMessage.background.setColorFilter(StringContract.Color.leftMessageColor, PorterDuff.Mode.SRC_ATOP)
            }

            StringContract.ViewType.LEFT_IMAGE_MESSAGE -> {
                val leftImageVideoMessageHolder = p0 as LeftImageVideoMessageHolder
                leftImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftImageVideoMessageHolder.binding.senderName.typeface = StringContract.Font.status
                leftImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                leftImageVideoMessageHolder.binding.imageMessage.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val imageIntent = Intent(context, ImageViewActivity::class.java)
                        imageIntent.putExtra(StringContract.IntentString.FILE_TYPE, baseMessage.type);
                        imageIntent.putExtra(StringContract.IntentString.URL, (baseMessage.url))
                        context.startActivity(imageIntent)
                    }

                })
            }

            StringContract.ViewType.RIGHT_IMAGE_MESSAGE -> {
                val rightImageVideoMessageHolder = p0 as RightImageVideoMessageHolder
                rightImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightImageVideoMessageHolder.binding.imageMessage.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val imageIntent = Intent(context, ImageViewActivity::class.java)
                        imageIntent.putExtra(StringContract.IntentString.FILE_TYPE, baseMessage.type);
                        imageIntent.putExtra(StringContract.IntentString.URL, (baseMessage.url))
                        context.startActivity(imageIntent)
                    }

                })

            }

            StringContract.ViewType.RIGHT_LOCATION_MESSAGE -> {
                val rightLocationViewHolder = p0 as RightLocationViewHolder

                rightLocationViewHolder.binding.message = baseMessage as TextMessage
                rightLocationViewHolder.binding.timestamp.typeface = StringContract.Font.status
                rightLocationViewHolder.bindView(p1, messagesList)

            }

            StringContract.ViewType.LEFT_LOCATION_MESSAGE -> {
                val leftLocationViewHolder = p0 as LeftLocationViewHolder
                leftLocationViewHolder.binding.message = baseMessage as TextMessage
                leftLocationViewHolder.binding.timestamp.typeface = StringContract.Font.status
                leftLocationViewHolder.bindView(p1, messagesList)
            }

            StringContract.ViewType.RIGHT_FILE_MESSAGE -> {
                try {

                    val rightFileViewHolder = p0 as RightFileViewHolder
                    rightFileViewHolder.binding.message = baseMessage as MediaMessage
                    rightFileViewHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
                    val t = mediaFile?.substring(mediaFile.lastIndexOf("/"))?.split("_".toRegex())?.dropLastWhile({ it.isEmpty() })?.toTypedArray()
                    rightFileViewHolder.binding.fileName.setText(t?.get(2))
                    rightFileViewHolder.binding.fileType.setText(mediaFile?.substring(mediaFile.lastIndexOf(".") + 1))
                    val finalMediaFile = mediaFile
                    rightFileViewHolder.binding.fileName.typeface = StringContract.Font.name
                    rightFileViewHolder.binding.fileType.typeface = StringContract.Font.name
                    rightFileViewHolder.binding.timeStamp.typeface = StringContract.Font.status
                    rightFileViewHolder.binding.fileName.setOnClickListener(View.OnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalMediaFile))) })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            StringContract.ViewType.LEFT_FILE_MESSAGE -> {

                try {

                    val leftFileViewHolder = p0 as LeftFileViewHolder
                    leftFileViewHolder.binding.message = baseMessage as MediaMessage
                    leftFileViewHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.leftMessageColor, PorterDuff.Mode.SRC_ATOP)
                    val t = mediaFile?.substring(mediaFile.lastIndexOf("/"))?.split("_".toRegex())?.dropLastWhile({ it.isEmpty() })?.toTypedArray()
                    leftFileViewHolder.binding.fileName.setText(t?.get(2))
                    leftFileViewHolder.binding.fileType.setText(mediaFile?.substring(mediaFile.lastIndexOf(".") + 1))
                    val finalMediaFile = mediaFile
                    leftFileViewHolder.binding.fileName.typeface = StringContract.Font.name
                    leftFileViewHolder.binding.fileType.typeface = StringContract.Font.name
                    leftFileViewHolder.binding.timeStamp.typeface = StringContract.Font.status
                    leftFileViewHolder.binding.senderName.typeface = StringContract.Font.status
                    leftFileViewHolder.binding.fileName.setOnClickListener(View.OnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalMediaFile))) })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            StringContract.ViewType.LEFT_VIDEO_MESSAGE -> {
                val leftImageVideoMessageHolder = p0 as LeftImageVideoMessageHolder
                leftImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                if (filePath != null) {
                    if (File(filePath).exists()) {
                        if (videoThumbnails.get(timeStampLong) == null) {
                            var c: Cursor? = null
                            try {
                                val videoOptions = BitmapFactory.Options()
                                videoOptions.inPreferredConfig = Bitmap.Config.RGB_565
                                videoOptions.inSampleSize = 2
                                val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                val requierddata = arrayOf(BaseColumns._ID)
                                c = context.contentResolver.query(videoUri, requierddata,
                                        MediaStore.MediaColumns.DATA + " like  \"" + baseMessage.metadata + "\"", null, null)
                                if (c != null && c.count != 0) {
                                    val bmp = MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver,
                                            c.getLong(0), MediaStore.Video.Thumbnails.MINI_KIND, videoOptions)
                                    leftImageVideoMessageHolder.binding.imageMessage.setImageBitmap(bmp)
                                    videoThumbnails.put(timeStampLong, bmp)
                                } else {
                                    val bmp = ThumbnailUtils.createVideoThumbnail(filePath,
                                            MediaStore.Video.Thumbnails.MINI_KIND)
                                    leftImageVideoMessageHolder.binding.imageMessage.setImageBitmap(bmp)
                                    videoThumbnails.put(timeStampLong, bmp)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                if (c != null && !c.isClosed) {
                                    c.close()
                                }
                            }
                        }
                    } else {
                        val requestOptions = RequestOptions()
                                .fitCenter()
                                .placeholder(R.drawable.ic_broken_image)
                        Glide.with(context)
                                .load(baseMessage.url)
                                .apply(requestOptions)
                                .into(leftImageVideoMessageHolder.binding.imageMessage)
                    }
                }
                leftImageVideoMessageHolder.binding.btnPlayVideo.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val imageIntent = Intent(context, ImageViewActivity::class.java)
                        imageIntent.putExtra(StringContract.IntentString.FILE_TYPE, baseMessage.type);
                        imageIntent.putExtra(StringContract.IntentString.URL, (baseMessage.url))
                        context.startActivity(imageIntent)
                    }

                })
            }

            StringContract.ViewType.CALL_MESSAGE -> {
                val listHeaderItem = p0 as TextHeaderHolder
                listHeaderItem.binding.txtMessageDate.setTextColor(StringContract.Color.black)

                message = "Call "+(baseMessage as Call).callStatus

                listHeaderItem.binding.txtMessageDate.text=message
            }

            StringContract.ViewType.RIGHT_VIDEO_MESSAGE -> {
                val rightImageVideoMessageHolder = p0 as RightImageVideoMessageHolder
                rightImageVideoMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightImageVideoMessageHolder.binding.timeStamp.typeface = StringContract.Font.status

                if (filePath != null) {
                    if (File(filePath).exists()) {
                        if (videoThumbnails.get(timeStampLong) == null) {
                            var c: Cursor? = null
                            try {
                                val videoOptions = BitmapFactory.Options()
                                videoOptions.inPreferredConfig = Bitmap.Config.RGB_565
                                videoOptions.inSampleSize = 2
                                val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                val requierddata = arrayOf(BaseColumns._ID)
                                c = context.contentResolver.query(videoUri, requierddata,
                                        MediaStore.MediaColumns.DATA + " like  \"" + baseMessage.metadata + "\"", null, null)
                                if (c != null && c.count != 0) {
                                    val bmp = MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver,
                                            c.getLong(0), MediaStore.Video.Thumbnails.MINI_KIND, videoOptions)
                                    rightImageVideoMessageHolder.binding.imageMessage.setImageBitmap(bmp)
                                    videoThumbnails.put(timeStampLong, bmp)
                                } else {
                                    val bmp = ThumbnailUtils.createVideoThumbnail(filePath,
                                            MediaStore.Video.Thumbnails.MINI_KIND)
                                    rightImageVideoMessageHolder.binding.imageMessage.setImageBitmap(bmp)
                                    videoThumbnails.put(timeStampLong, bmp)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                if (c != null && !c.isClosed) {
                                    c.close()
                                }
                            }
                        }
                    } else {
                        val requestOptions = RequestOptions()
                                .fitCenter()
                                .placeholder(R.drawable.ic_broken_image)
                        Glide.with(context)
                                .load(baseMessage.url)
                                .apply(requestOptions)
                                .into(rightImageVideoMessageHolder.binding.imageMessage)
                    }
                }

                rightImageVideoMessageHolder.binding.btnPlayVideo.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val imageIntent = Intent(context, ImageViewActivity::class.java)
                        imageIntent.putExtra(StringContract.IntentString.FILE_TYPE, baseMessage.type);
                        imageIntent.putExtra(StringContract.IntentString.URL, (baseMessage.url))
                        context.startActivity(imageIntent)
                    }

                })
            }


            StringContract.ViewType.RIGHT_AUDIO_MESSAGE -> {
                val rightAudioMessageHolder = p0 as RightAudioMessageHolder
                rightAudioMessageHolder.binding.message = (baseMessage as MediaMessage)
                rightAudioMessageHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.rightMessageColor, PorterDuff.Mode.SRC_ATOP)
                rightAudioMessageHolder.binding.audioSeekBar.progress = 0
                rightAudioMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                rightAudioMessageHolder.binding.audioLength.typeface = StringContract.Font.status
                try {

                    if (audioDurations.get(timeStampLong) == null) {
                        player?.reset()
                        try {
                            player?.setDataSource(filePath)
                            player?.prepare()
                           
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val duration = player?.duration
                        audioDurations.put(timeStampLong, duration)
                        rightAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })

                    } else {
                        val duration = audioDurations.get(timeStampLong)
                        rightAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }


                rightAudioMessageHolder.binding.playButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {

                        if (!TextUtils.isEmpty(filePath)) {

                            try {
                                if (baseMessage.sentAt == currentlyPlayingId) {
                                    currentPlayingSong = ""

                                    try {
                                        if (player?.isPlaying()!!) {
                                            player?.pause()
                                            rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow)
                                        } else {
                                            //                                                player.setDataSource(message);
                                            //                                                player.prepare();
                                            player?.getCurrentPosition()?.let { player?.seekTo(it) }
                                            player?.getCurrentPosition()?.let { rightAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                            rightAudioMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                            player?.getDuration()?.let { rightAudioMessageHolder.binding.audioSeekBar.setMax(it) }
                                            rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                            timerRunnable = object : Runnable {
                                                override fun run() {

                                                    val pos = player?.getCurrentPosition()
                                                    pos?.let { rightAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                    if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                        rightAudioMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                        seekHandler.postDelayed(this, 250)
                                                    } else {
                                                        seekHandler
                                                                .removeCallbacks(timerRunnable)
                                                        timerRunnable = null
                                                    }
                                                }

                                            }
                                            seekHandler.postDelayed(timerRunnable, 100)
                                            notifyDataSetChanged()
                                            player!!.start()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                } else {
                                    rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                    player?.let {
                                        playAudio(filePath, timeStampLong, it, rightAudioMessageHolder.binding.playButton,
                                                rightAudioMessageHolder.binding.audioLength, rightAudioMessageHolder.binding.audioSeekBar)
                                    }
                                }

                                rightAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                })

                filePath?.let { Log.d("metaData", it) }

            }

            StringContract.ViewType.LEFT_AUDIO_MESSAGE -> {
                val leftAudioMessageHolder = p0 as LeftAudioMessageHolder
                leftAudioMessageHolder.binding.message = (baseMessage as MediaMessage)
                leftAudioMessageHolder.binding.fileContainer.background.setColorFilter(StringContract.Color.leftMessageColor, PorterDuff.Mode.SRC_ATOP)
                leftAudioMessageHolder.binding.audioSeekBar.progress = 0
                leftAudioMessageHolder.binding.audioLength.typeface = StringContract.Font.status
                leftAudioMessageHolder.binding.timeStamp.typeface = StringContract.Font.status
                try {

                    if (audioDurations.get(timeStampLong) == null) {
                        player?.reset()
                        try {
                            player?.setDataSource(baseMessage.url)
                            player?.prepare()

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val duration = player?.duration
                        audioDurations.put(timeStampLong, duration)
                        leftAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })


                    } else {
                        val duration = audioDurations.get(timeStampLong)
                        leftAudioMessageHolder.binding.audioLength.setText(duration?.toLong()?.let
                        { DateUtil.convertTimeStampToDurationTime(it) })

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }


                leftAudioMessageHolder.binding.playButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {

                        if (!TextUtils.isEmpty(baseMessage.url)) {

                            try {
                                if (baseMessage.sentAt == currentlyPlayingId) {
                                    currentPlayingSong = ""

                                    try {
                                        if (player?.isPlaying()!!) {
                                            player?.pause()

                                            leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_play_arrow)
                                        } else {
                                            //                                                player.setDataSource(message);
                                            //                                                player.prepare();
                                            player?.getCurrentPosition()?.let { player?.seekTo(it) }
                                            player?.getCurrentPosition()?.let { leftAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                            leftAudioMessageHolder.binding.audioLength.setText(player?.getDuration()?.toLong()?.let { DateUtil.convertTimeStampToDurationTime(it) })
                                            player?.getDuration()?.let { leftAudioMessageHolder.binding.audioSeekBar.setMax(it) }
                                            leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                            timerRunnable = object : Runnable {
                                                override fun run() {

                                                    val pos = player?.getCurrentPosition()
                                                    pos?.let { leftAudioMessageHolder.binding.audioSeekBar.setProgress(it) }
                                                    if (player?.isPlaying()!! && pos!! < player?.getDuration()!!) {
                                                        leftAudioMessageHolder.binding.audioLength.setText(DateUtil.convertTimeStampToDurationTime(player?.getCurrentPosition()!!.toLong()))
                                                        seekHandler.postDelayed(this, 250)
                                                    } else {
                                                        seekHandler
                                                                .removeCallbacks(timerRunnable)
                                                        timerRunnable = null
                                                    }
                                                }

                                            }
                                            seekHandler.postDelayed(timerRunnable, 100)
                                            notifyDataSetChanged()
                                            player!!.start()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                } else {
                                    leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)
                                    player?.let {
                                        playAudio(baseMessage.url, timeStampLong, it, leftAudioMessageHolder.binding.playButton,
                                                leftAudioMessageHolder.binding.audioLength, leftAudioMessageHolder.binding.audioSeekBar)
                                    }
                                }

                                leftAudioMessageHolder.binding.playButton.setImageResource(R.drawable.ic_pause_white_24dp)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                })

                filePath?.let { Log.d("metaData", it) }


            }
        }
    }

    fun playAudio(message: String?, sentTimeStamp: Long, player: MediaPlayer, playButton: ImageView, audioLength: TextView, audioSeekBar: SeekBar) {
        try {
            currentPlayingSong = message
            currentlyPlayingId = sentTimeStamp
            if (timerRunnable != null) {
                seekHandler.removeCallbacks(timerRunnable)
                timerRunnable = null
            }
            //            setBtnColor(viewtype, playBtn, false);
            player.reset()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.setDataSource(currentPlayingSong)
            player.prepare()
            player.start()

            val duration = player.duration
            audioSeekBar.max = duration
            timerRunnable = object : Runnable {
                override fun run() {

                    val pos = player.currentPosition
                    audioSeekBar.progress = pos

                    if (player.isPlaying && pos < duration) {
                        audioLength.setText(DateUtil.convertTimeStampToDurationTime(player.currentPosition.toLong()))
                        seekHandler.postDelayed(this, 250)
                    } else {
                        seekHandler.removeCallbacks(timerRunnable)
                        timerRunnable = null
                    }
                }

            }
            seekHandler.postDelayed(timerRunnable, 100)
            notifyDataSetChanged()

            player.setOnCompletionListener { mp ->
                currentPlayingSong = ""
                currentlyPlayingId = 0L
                //                    setBtnColor(viewtype, playBtn, true);
                seekHandler
                        .removeCallbacks(timerRunnable)
                timerRunnable = null
                mp.stop()
                audioLength.setText(DateUtil.convertTimeStampToDurationTime(duration.toLong()))
                audioSeekBar.progress = 0
                playButton.setImageResource(R.drawable.ic_play_arrow)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
