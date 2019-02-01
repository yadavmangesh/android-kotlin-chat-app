package com.inscripts.cometchatpulse

import android.graphics.Typeface
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Utils.Appearance

class StringContract {

    class AppDetails {

        companion object {

            const val APP_ID: String = "XXXXXXXXXXXXX"

            const val API_KEY: String = "XXXXXXXXXXXXX"

            lateinit var theme: Appearance.AppTheme

        }
    }

    class IntentString {

        companion object {

            val USER_ID: String = "user_id"

            val USER_NAME: String = "user_name"

            val USER_AVATAR: String = "user_avatar"

            val USER_STATUS: String = "user_status"

            val LAST_ACTIVE: String = "last_user"

            val GROUP_ID: String = "group_id"

            val GROUP_NAME: String = "group_name"

            val GROUP_ICON: String = "group_icon"

            val GROUP_OWNER: String = "group_owner"

            val IMAGE_TYPE = "image/*"

            val AUDIO_TYPE = "audio/*"

            val DOCUMENT_TYPE = arrayOf("*/*")

            val EXTRA_MIME_TYPE = arrayOf("image/*", "video/*")

            val EXTRA_MIME_DOC = arrayOf("text/plane", "text/html", "application/pdf", "application/msword",
                    "application/vnd.ms.excel", "application/mspowerpoint", "application/zip")

            val TITLE: String = "title"

            val POSITION: String = "position"

            val SESSION_ID: String = "session_id"

            val OUTGOING: String = "outgoing"

            val INCOMING: String = "incoming"

            val RECIVER_TYPE: String = "receiver_type"

            val URL: String = "image"

            val FILE_TYPE: String = "file_type"

            val ID: String = "id"

            val GROUP_DESCRIPTION:String="description"


        }
    }

    class ViewType {

        companion object {

            val RIGHT_TEXT_MESSAGE = 334

            val LEFT_TEXT_MESSAGE = 734

            val LEFT_IMAGE_MESSAGE = 528

            val RIGHT_IMAGE_MESSAGE = 834

            val LEFT_VIDEO_MESSAGE = 580

            val RIGHT_VIDEO_MESSAGE = 797

            val RIGHT_AUDIO_MESSAGE = 70

            val LEFT_AUDIO_MESSAGE = 79

            val LEFT_FILE_MESSAGE = 24

            val RIGHT_FILE_MESSAGE = 55

            val CALL_MESSAGE = 84

            val ACTION_MESSAGE = 99

            val RIGHT_LOCATION_MESSAGE = 58

            val LEFT_LOCATION_MESSAGE = 59
        }
    }


    class Font {


        companion object {

            lateinit var title: Typeface

            lateinit var name: Typeface

            lateinit var status: Typeface

            lateinit var message: Typeface

        }

    }

    class Color {

        companion object {
            var primaryColor: Int = 0;

            var primaryDarkColor: Int = 0

            var accentColor: Int = 0

            var rightMessageColor = 0

            var leftMessageColor = 0

            var iconTint = 0

            var white: Int = android.graphics.Color.parseColor("#ffffff")

            var black: Int = android.graphics.Color.parseColor("#000000")

            var grey: Int = android.graphics.Color.parseColor("#CACACC")

            var inactiveColor = android.graphics.Color.parseColor("#9e9e9e");

        }

    }

    class Dimensions {

        companion object {

            var marginStart: Int = 16

            var marginEnd: Int = 16

            var cardViewCorner: Float = 24f

            var cardViewElevation: Float = 8f
        }
    }

    class ListenerName {

        companion object {

            val MESSAGE_LISTENER = "message_listener"

            val USER_LISTENER = "user_listener"

            val GROUP_EVENT_LISTENER = "group_event_listener"

            val CALL_EVENT_LISTENER = "call_event_listener"
        }


    }


    class RequestPermission {

        companion object {
            val RECORD_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO,
                    CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

            val CAMERA_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA,
                    CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

            val VIDEO_CALL_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_CAMERA,
                    CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO)

            val STORAGE_PERMISSION = arrayOf(CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)

//            val LOCATION_PERMISSION= arrayOf(CCPermissionHelper.L)
        }
    }

    class RequestCode {

        companion object {

            val ADD_GALLERY = 1

            val ADD_DOCUMENT = 2

            val ADD_SOUND = 3

            val TAKE_PHOTO = 5

            val LOCATION = 15

            val TAKE_VIDEO = 7

            val LEFT = 8

            val RECORD_CODE = 10

            val VIDEO_CALL = 12

            val VOICE_CALL = 24
        }
    }


}