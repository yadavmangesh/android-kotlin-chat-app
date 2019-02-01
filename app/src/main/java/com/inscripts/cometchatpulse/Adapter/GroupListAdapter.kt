package com.inscripts.cometchatpulse.Adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cometchat.pro.models.Group
import com.inscripts.cometchatpulse.Helpers.ChildClickListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.databinding.GroupListItemBinding



class GroupListAdapter(val context: Context?) : RecyclerView.Adapter<GroupListAdapter.GroupHolder>() {

    private var groupList: MutableList<Group> = arrayListOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GroupHolder {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val binding: GroupListItemBinding = DataBindingUtil.inflate(layoutInflater,R.layout.group_list_item,p0,false)

        return GroupHolder(binding)

    }

    override fun getItemCount(): Int {

        return groupList.size
    }

    override fun onBindViewHolder(groupHolder: GroupHolder, p1: Int) {

        val group = groupList[p1]

        groupHolder.binding.group=group
        groupHolder.binding.childClick=context as ChildClickListener
        groupHolder.binding.textViewGroupName.typeface=StringContract.Font.name
        groupHolder.binding.textViewUsersOnlineMessage.typeface=StringContract.Font.status
        groupHolder.binding.executePendingBindings()
        groupHolder.binding.root.setTag(R.string.group,group)

    }

    fun setGroup(it: MutableList<Group>) {
        this.groupList = it
        notifyDataSetChanged()
    }


    class GroupHolder(val binding:GroupListItemBinding) : RecyclerView.ViewHolder(binding.root)

}