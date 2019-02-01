package com.inscripts.cometchatpulse.Adapter


import android.databinding.DataBindingUtil
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cometchat.pro.models.GroupMember
import com.inscripts.cometchatpulse.Fragment.BanMemberFragment
import com.inscripts.cometchatpulse.Fragment.MemberFragment
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.ViewHolder.GroupMemberHolder
import com.inscripts.cometchatpulse.databinding.ContactItemBinding
import com.inscripts.cometchatpulse.databinding.GroupMemberItemBinding

class MemberListAdapter( val resId:Int,val listener: OnClickEvent): RecyclerView.Adapter<GroupMemberHolder>() {

    private var groupMemberList:MutableList<GroupMember> = mutableListOf()

    private  var onClickevent:OnClickEvent

    init {
        onClickevent=listener
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GroupMemberHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(p0.context)
        val binding: GroupMemberItemBinding = DataBindingUtil.inflate(layoutInflater,resId,p0,false)

        return GroupMemberHolder(binding)
    }

    override fun getItemCount(): Int {
     return groupMemberList.size
    }

    override fun onBindViewHolder(p0: GroupMemberHolder, p1: Int) {

       val groupMember=groupMemberList.get(p1)

        p0.binding.memeber=groupMember

        p0.binding.textviewUserName.typeface=StringContract.Font.name

        p0.binding.textviewUserStatus.typeface=StringContract.Font.status

        p0.binding.root.setTag(R.string.user,groupMember.user)

        p0.binding.executePendingBindings()



        p0.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p1: View) {
                onClickevent.onClickRl(p0.itemView,groupMember.user)
            }
        })

    }

    internal fun setMemberList(memberlist: MutableList<GroupMember>) {
       this.groupMemberList=memberlist
        notifyDataSetChanged()

    }



}