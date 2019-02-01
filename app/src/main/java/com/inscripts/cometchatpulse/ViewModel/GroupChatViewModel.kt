package com.inscripts.cometchatpulse.ViewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Repository.GroupRepository
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.StringContract
import org.json.JSONObject
import java.io.File


class GroupChatViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository: GroupRepository

    private val messageRepository: MessageRepository

    val messageList: MutableLiveData<MutableList<BaseMessage>>

    val groupMemberList: MutableLiveData<MutableList<GroupMember>>

    val banMemberList: MutableLiveData<MutableList<GroupMember>>


    init {
        groupRepository = GroupRepository()
        messageRepository = MessageRepository()
        banMemberList = groupRepository.banMemberLiveData
        messageList = messageRepository.groupMessageList
        groupMemberList = groupRepository.groupMemberLiveData

    }

    fun fetchMessage(LIMIT: Int, guid: String) {
        messageRepository.fetchGroupMessage(guid, LIMIT)
    }

    override fun onCleared() {
        super.onCleared()

    }

    fun sendTextMessage(textMessage: TextMessage) {
        messageRepository.sendTextMessage(textMessage)
    }

    fun addGroupEventListener(group_event_listener: String) {
        messageRepository.addGroupListener(group_event_listener)
    }

    fun sendMediaMessage(path: String?, type: String?, guid: String) {
        val mediaMessage = MediaMessage(guid, File(path), type, CometChatConstants.RECEIVER_TYPE_GROUP)
        val jObject = JSONObject()
        jObject.put("path", path)
        mediaMessage.metadata = jObject;
        messageRepository.sendMediaMessage(mediaMessage)
    }

    fun removeGroupEventListener(group_event_listener: String) {
        messageRepository.removeGroupListener(group_event_listener)
    }



    fun getMembers(guid: String, LIMIT: Int) {
        groupRepository.getGroupMember(guid, LIMIT)
    }

    fun getBanedMember(guid: String, LIMIT: Int) {
        groupRepository.getBannedMember(guid, LIMIT)
    }

    fun banMember(uid: String?, guid: String) {
        uid?.let { groupRepository.banMember(it, guid) }
    }

    fun kickMember(uid: String?, guid: String) {
        uid?.let { groupRepository.kickMember(it, guid) }
    }

    fun unbanMember(uid: String?, guid: String) {
        uid?.let { groupRepository.unBanMember(guid, it) }
    }

    fun addGroupMessageListener(message_listener: String,ownerId:String) {
       messageRepository.messageReceiveListener(message_listener,ownerId)
    }

    fun removeMessageListener(messagE_LISTENER: String) {
        messageRepository.removeMessageListener(messagE_LISTENER)
    }

}