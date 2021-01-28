package com.github.arekolek.phone

import android.telecom.Call
import android.telecom.InCallService

class WH_CallService : InCallService() {

    override fun onCallAdded(call: Call) {
        WH_OngoingCall.call = call
        //DialerActivity.start(this, call)
    }

    override fun onCallRemoved(call: Call) {
        WH_OngoingCall.call = null
    }
}