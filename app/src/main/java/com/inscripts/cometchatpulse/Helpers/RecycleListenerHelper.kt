package com.inscripts.cometchatpulse.Helpers

import android.support.v7.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.inscripts.cometchatpulse.ViewHolder.LeftLocationViewHolder
import com.inscripts.cometchatpulse.ViewHolder.RightLocationViewHolder

class RecycleListenerHelper :RecyclerView.RecyclerListener {


    override fun onViewRecycled(p0: RecyclerView.ViewHolder) {

        if (p0 is RightLocationViewHolder){
            p0.googleMap?.clear()
            p0.googleMap?.mapType=GoogleMap.MAP_TYPE_NONE
        }
        if (p0 is LeftLocationViewHolder){
            p0.googleMap?.clear()
            p0.googleMap?.mapType=GoogleMap.MAP_TYPE_NONE
        }
    }
}