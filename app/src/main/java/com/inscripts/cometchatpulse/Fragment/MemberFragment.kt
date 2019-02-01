package com.inscripts.cometchatpulse.Fragment


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Adapter.MemberListAdapter
import com.inscripts.cometchatpulse.Helpers.OnClickEvent
import com.inscripts.cometchatpulse.Helpers.RecyclerviewTouchListener

import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import kotlinx.android.synthetic.main.fragment_ban_member.view.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.*
import java.lang.Exception
import android.R.attr.button
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import com.inscripts.cometchatpulse.Activities.MainActivity
import com.inscripts.cometchatpulse.Utils.Appearance


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class MemberFragment : Fragment() {


    private lateinit var guid: String

    private lateinit var ownerId: String

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var groupChatViewModel: GroupChatViewModel

    private lateinit var memberListAdapter: MemberListAdapter

    private lateinit var member: User

    private lateinit var myView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_home, container, false)


        guid = arguments?.getString(StringContract.IntentString.GROUP_ID).toString()

        ownerId = arguments?.getString(StringContract.IntentString.USER_ID).toString()

        (activity as AppCompatActivity).setSupportActionBar(myView.member_toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        myView.member_toolbar.title = "Group Members"

        myView.member_toolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,
                PorterDuff.Mode.SRC_ATOP)

        myView.member_toolbar.setBackgroundColor(StringContract.Color.primaryColor)


        if (StringContract.AppDetails.theme== Appearance.AppTheme.AZURE_RADIANCE){
            myView.member_toolbar.setTitleTextColor(StringContract.Color.black)
        }
        else{
            myView.member_toolbar.setTitleTextColor(StringContract.Color.white)
        }

        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)

        linearLayoutManager = LinearLayoutManager(context)
        myView.rv_member.layoutManager = linearLayoutManager
        try {

            memberListAdapter = MemberListAdapter(R.layout.group_member_item, object : OnClickEvent {
                override fun onClickRl(item: View, user: User) {
                    member = user
                    if (ownerId != user.getUid()) {

                        val popup = context?.let { PopupMenu(it, item) }
                        //Inflating the Popup using xml file
                        popup?.getMenuInflater()?.inflate(R.menu.member_menu, popup?.getMenu())

                        popup?.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                            override fun onMenuItemClick(p0: MenuItem?): Boolean {

                                when (p0!!.itemId) {
                                    R.id.menu_item_outcast -> {
                                        groupChatViewModel.banMember(user.uid, guid)
                                    }
                                    R.id.menu_item_kick -> {
                                        groupChatViewModel.kickMember(user.uid, guid)
                                    }

                                }

                                return true
                            }

                        })

                        popup?.show()
//
                    }
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        myView.rv_member.adapter = memberListAdapter
        registerForContextMenu(myView.rv_member)
        groupChatViewModel.getMembers(guid, LIMIT = 10)


        groupChatViewModel.groupMemberList.observe(this, Observer { groupMember ->
            groupMember?.let {
                memberListAdapter.setMemberList(it)
            }
        })


        myView.rv_member.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    groupChatViewModel.getMembers(guid, LIMIT = 10)
                }
            }

        })

        return myView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){

            android.R.id.home->{
                activity?.onBackPressed()
            }
        }

        return true
    }
}
