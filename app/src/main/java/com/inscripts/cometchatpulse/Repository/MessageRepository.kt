package com.inscripts.cometchatpulse.Repository

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.WorkerThread
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.*
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.CallActivity
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Utils.CommonUtil

class MessageRepository {

    var ownerId: String

    var messageRequest: MessagesRequest? = null

    var groupMessageRequest: MessagesRequest? = null

    var user:MutableLiveData<User> = MutableLiveData()

    init {
        ownerId = CometChat.getLoggedInUser().uid
    }

    var onetoOneMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var groupMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var mutableOneToOneMessageList = mutableListOf<BaseMessage>()

    var mutableGroupMessageList = mutableListOf<BaseMessage>()

    @WorkerThread
    fun fetchMessage(LIMIT: Int, userId: String) {


            if (messageRequest == null) {
                messageRequest = MessagesRequest.MessagesRequestBuilder().setUID(userId).setLimit(LIMIT).build()

                messageRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
                    override fun onSuccess(p0: List<BaseMessage>?) {

                        p0?.let { mutableOneToOneMessageList.addAll(0,it) }
                        onetoOneMessageList.value = mutableOneToOneMessageList

                        for (baseMessage in p0!!){
                            Log.d("messageRequest", "  " +baseMessage.id)
                        }
                    }

                    override fun onError(p0: CometChatException?) {
                    }


                })

            } else {
                messageRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
                    override fun onError(p0: CometChatException?) {

                    }


                    override fun onSuccess(p0: List<BaseMessage>?) {
                        for (baseMessage in p0!!){
                            Log.d("messageRequest", "  " +baseMessage.id)
                        }
                        p0?.let { mutableOneToOneMessageList.addAll(0,it) }
                        onetoOneMessageList.value = mutableOneToOneMessageList

                    }

                })
            }

    }

    @WorkerThread
    fun fetchGroupMessage(guid: String, LIMIT: Int) {
            if (groupMessageRequest == null) {

                groupMessageRequest = MessagesRequest.MessagesRequestBuilder().setGUID(guid).setLimit(LIMIT).build()

                groupMessageRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
                    override fun onSuccess(p0: List<BaseMessage>?) {
                        p0?.let { mutableGroupMessageList.addAll(0,it) }
                        groupMessageList.value = mutableGroupMessageList

                    }

                    override fun onError(p0: CometChatException?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }


                })
            } else {

                groupMessageRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
                    override fun onSuccess(p0: List<BaseMessage>?) {
                        p0?.let { mutableGroupMessageList.addAll(0,it) }
                        groupMessageList.value = mutableGroupMessageList

                    }

                    override fun onError(p0: CometChatException?) {

                    }
                })
            }
    }

    @WorkerThread
    fun sendTextMessage(textMessage: TextMessage,context: Context?=null) {


            CometChat.sendMessage(textMessage, object : CometChat.CallbackListener<TextMessage>() {
                override fun onSuccess(p0: TextMessage?) {
                     if (p0!=null) {
                         Log.d("messageDao", "  " + textMessage.toString())
                         if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                              mutableOneToOneMessageList.add(p0)
                             onetoOneMessageList.value = mutableOneToOneMessageList

                         }
                         else{
                            mutableGroupMessageList.add(p0)
                             groupMessageList.value = mutableGroupMessageList

                         }

                         if (context!=null){
                             if (context is LocationActivity)
                                 context.finish()
                         }

                     }

                }

                override fun onError(p0: CometChatException?) {
                    Toast.makeText(CometChatPro.applicationContext(), p0?.details, Toast.LENGTH_SHORT).show()

                }


            })
    }





    fun addGroupListener(group_event_listener: String) {
            CometChat.addGroupListener(group_event_listener,object :CometChat.GroupListener(){
                override fun onGroupMemberKicked(action: Action?, kickedUser: User?, kickedBy: User?, kickedFrom: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList


                }

                override fun onGroupMemberScopeChanged(action: Action?, user: User?, scopeChangedTo: String?, scopeChangedFrom: String?, group: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList

                }

                override fun onGroupMemberUnbanned(action: Action?, unbannedUser: User?, unbannedBy: User?, unbannedFrom: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList

                }

                override fun onGroupMemberBanned(action: Action?, bannedUser: User?, bannedBy: User?, bannedFrom: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList

                }

                override fun onGroupMemberLeft(action: Action?, joinedUser: User?, joinedGroup: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList

                }

                override fun onGroupMemberJoined(action: Action?, joinedUser: User?, joinedGroup: Group?) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList

                }

            })
    }




    @WorkerThread
    fun messageReceiveListener(listener: String, ownerId: String) {

        CometChat.addMessageListener(listener, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(p0: TextMessage?) {
                if (p0 != null) {

                    if (!ownerId.equals(p0.sender.uid)) {

                        if (!p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                            mutableOneToOneMessageList.add(p0)
                            onetoOneMessageList.value = mutableOneToOneMessageList

                        }
                        else {
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }
                    }


                }
            }

            override fun onMediaMessageReceived(p0: MediaMessage?) {
                if (p0 != null) {

                    if (!ownerId.equals(p0.sender.uid)) {

                        if (!p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {

                            mutableOneToOneMessageList.add(p0)
                            onetoOneMessageList.value = mutableOneToOneMessageList

                        }
                        else {
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }
                    }
                }
            }

        })
    }

    @WorkerThread
    fun removeMessageListener(listener: String) {
        CometChat.removeMessageListener(listener)

    }

    @WorkerThread
    fun addPresenceListener(listener: String) {

        CometChat.addUserListener(listener, object : CometChat.UserListener() {
            override fun onUserOffline(p0: com.cometchat.pro.models.User?) {

                if (p0 != null) {
                   user.value=p0
                }
            }

            override fun onUserOnline(p0: com.cometchat.pro.models.User?) {
                if (p0 != null) {
                    user.value=p0
                }
            }

        })
    }


    @WorkerThread
    fun removePresenceListener(listener: String) {

        CometChat.removeUserListener(listener)

    }

    @WorkerThread
    fun sendMediaMessage(mediaMessage: MediaMessage) {

        CometChat.sendMediaMessage(mediaMessage, object : CometChat.CallbackListener<MediaMessage>() {
            override fun onSuccess(p0: MediaMessage?) {

                if (p0!=null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                         mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    }
                    else{
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }

                }
                Log.d("MediaMessage", "baseMessage")
            }

            override fun onError(p0: CometChatException?) {
                p0?.printStackTrace()
            }

        })
    }



    fun removeGroupListener(group_event_listener: String) {
        CometChat.removeGroupListener(group_event_listener)
    }

    fun addCallListener(context:Context,call_event_listener: String,view: RelativeLayout?) {

            CometChat.addCallListener(call_event_listener,object :CometChat.CallListener(){

                override fun onIncomingCallCancelled(p0: Call?) {
                    Log.d("  ","onCallEnded "+"onIncomingCallCancelled "+p0.toString())

                    if (context is CallActivity){

                        context.finish()
                    }
                }

                override fun onOutgoingCallAccepted(p0: Call?) {

                    p0?.sessionId?.let {
                        if (view != null) {
                            CometChat.startCall(context as Activity, it,view,object:CometChat.OngoingCallListener{

                                override fun onUserJoined(p0: User?) {
                                }

                                override fun onUserLeft(p0: User?) {

                                }

                                override fun onError(p0: CometChatException?) {
                                }

                                override fun onCallEnded(p0: Call?) {

                                    Log.d("  ","onCallEnded "+"onOutgoingCallAccepted "+p0.toString())
                                    if (context is CallActivity){
                                        context.finish()
                                    }
                                }

                            })
                        }
                    }
                }

                override fun onIncomingCallReceived(p0: Call?) {

                    Log.d("  ","onCallEnded "+"onIncomingCallReceived "+p0.toString())
                    if (p0 != null) {
                        if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {

                            CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_USER,context,
                                    p0.callInitiator as User, p0.type, false, p0.sessionId)

                        } else if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)){
                            CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_GROUP,context,
                                    p0.callReceiver as Group, p0.type, false, p0.sessionId)
                        }
                    }
                }

                override fun onOutgoingCallRejected(p0: Call?) {

                    Log.d("  ","onCallEnded "+"onOutgoingCallRejected "+p0.toString())
                    if (context is CallActivity){

                        context.finish()
                    }
                }

            })

    }

    fun removeCallListener(call_event_listener: String) {
        CometChat.removeCallListener(call_event_listener)
    }

    fun acceptCall(sessionID: String,view:RelativeLayout,activity: Activity) {

             CometChat.acceptCall(sessionID,object:CometChat.CallbackListener<Call>(){
                 override fun onSuccess(p0: Call?) {

                     p0?.sessionId?.let {
                         CometChat.startCall(activity, it,view,object:CometChat.OngoingCallListener{
                         override fun onUserJoined(p0: User?) {

                         }

                         override fun onUserLeft(p0: User?) {

                         }

                         override fun onError(p0: CometChatException?) {
                         }

                         override fun onCallEnded(p0: Call?) {
                             Log.d("  ","onCallEnded "+"startCall "+p0.toString())
                         }

                     }) }
                 }

                 override fun onError(p0: CometChatException?) {

                 }

             })
    }

    fun rejectCall(sessionID: String, call_status_rejected: String,activity: Activity) {
            CometChat.rejectCall(sessionID,call_status_rejected,object:CometChat.CallbackListener<Call>(){
                override fun onSuccess(p0: Call?) {
                    (activity as CallActivity).finish()
                }

                override fun onError(p0: CometChatException?) {

                }

            })

    }

    fun initiateCall(call:Call,context: Context) {
        CometChat.initiateCall(call,object :CometChat.CallbackListener<Call>(){
            override fun onSuccess(p0: Call?) {
                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_USER,context, p0.callReceiver as User, p0.type, true, p0.sessionId)
                    } else if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)){
                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_GROUP,context, p0.callReceiver as Group, p0.type, true, p0.sessionId)
                    }
                }

            }

            override fun onError(p0: CometChatException?) {

            }

        })
    }

}