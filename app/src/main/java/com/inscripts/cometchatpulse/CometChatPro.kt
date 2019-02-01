package com.inscripts.cometchatpulse

import android.app.Application
import android.content.Context
import android.util.Log
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.inscripts.cometchatpulse.Utils.Appearance


class CometChatPro : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: CometChatPro? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        Appearance(Appearance.AppTheme.PERSIAN_BLUE)

        CometChat.init(applicationContext, StringContract.AppDetails.APP_ID, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d("INIT", "Initialization completed successfully");
            }

            override fun onError(p0: CometChatException?) {
                p0?.message
            }
        })


    }
}