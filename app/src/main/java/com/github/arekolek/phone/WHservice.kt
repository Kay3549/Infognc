package com.github.arekolek.phone

import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.Binder
import android.os.IBinder
import android.provider.CallLog
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WHservice : Service() {

    override fun onCreate() {

        super.onCreate()
        var managedCusor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            null,
            null
        )
        managedCusor?.moveToNext()
        var callList: Int? = managedCusor?.getColumnIndex(CallLog.Calls.NUMBER) // 전화번호
        var date: Int? = managedCusor?.getColumnIndex(CallLog.Calls.DATE) // 전화 시작시간
        var duration: Int? = managedCusor?.getColumnIndex(CallLog.Calls.DURATION) // 통화 얼마나 했는지
        var type: Int? = managedCusor?.getColumnIndex(CallLog.Calls.TYPE) // 콜타입


        val AcallList = callList?.let { managedCusor?.getString(it) } //전화번호
        if (AcallList != null) {
            passdata(AcallList)
        }


        val Adate = date?.let { managedCusor?.getString(it) } //전화 시작시간
        if (Adate != null) {
            RingTime(Adate)
        }


        val Atype = type?.let { managedCusor?.getString(it) } //콜타입
        val Aduration = duration?.let { managedCusor?.getString(it) } //얼마나 통화했는지


//        val callDate: String? = date?.let { managedCusor?.getString(it) }
//        val callDayTime = Date(Long.valueOf(callDate))
//        val formatter = SimpleDateFormat("yy-MM-dd_HH:mm:ss.SSS")
//        val dateString = formatter.format(callDayTime)
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    private fun RingTime(Aduration : String){

        val start : Long = Data.callStartTime/1000
        val end : Long = Data.callEndTime/1000
        val duration :Long =Aduration.toLong()

        Data.ringtime = ((end - duration)-start)/1000

        Log.e("=================링타임", start.toString())
        Log.e("=================링타임", end.toString())
        Log.e("=================링타임", Data.ringtime.toString())

    }


    private fun passdata(number: String) {
        Data.phonenumber = number
        var k = number.length
        var phoneNum: String? = ""
        phoneNum = if (k <= 15) {
            number
        } else {
            var ran = IntRange(0, 14)
            var temp = number.slice(ran)
            temp
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS_$phoneNum")
        val formatted = current.format(formatter) // 녹취키

        Data.setdata(formatted)

    }
}