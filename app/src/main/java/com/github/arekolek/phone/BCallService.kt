package com.github.arekolek.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.StrictMode
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.delay
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class BCallService : BroadcastReceiver() {

    private var mLastState: String = ""
    private var rectitle: String? = ""
    private var datetemp: kotlin.Long = 0
    private var path = "/storage/emulated/0/Call"

    private var agentPhone = Data.returnagentPhone()
    private var agentnum = Data.retunagentNum()

    override fun onReceive(context: Context, intent: Intent) {


        // 시스템에서 broadcast Receiver를 계속 호출함. 그래서 계속 호출한은 것을 방지하는 부분
        val state: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state.equals(mLastState)) {
            return
        } else {
            if (state != null) {
                mLastState = state
            }
        }

        //통화가 시작되었을 때
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Data.callStartTime = System.currentTimeMillis()
            val time = Date(Data.callStartTime)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = formatter.format(time)
            Log.e("=============시작시간", formatted)
            //connect()
            // sqlDB("ACTIVE")

            //통화중이 아닐 때
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Thread.sleep(1100)
            Data.callEndTime = System.currentTimeMillis()
            val time = Date(Data.callEndTime)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = formatter.format(time)
            Log.e("=============종료시간", formatted)


            Log.e("======bbbbbb",Data.Ddata)
            when(Data.Ddata){
                "" -> {
                    Log.e("======bbbbbb","들어옴")
                    Getphonenum(context) // 최근 전화 목록에 값(전화번호, duration, 등등...)을 가져옴, 키 값도 생성.
                    rectitle = Data.retundata() // 녹취 키 가져오기
                    when (Data.duration.toString()) {

                        //녹취 파일이 생성되지 않았을 때
                        "0" -> {
                            Data.recYN = "N"
                            connect()     //db connect
                            sqlDB("DISCONNECTED")  // d 적재
                        }

                        //녹취 파일이 생성되었을 때
                        else -> {
                            Data.recYN = "Y"
                            connFtp() //ftp 올리기
                            connect()     //db connect
                            sqlDB("DISCONNECTED")  // d 적재
                        }
                    }
                }else -> {
                Log.e("======bbbbbb","아니되오")
                    return
                }
            }
        }
    }

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"

    private var connection: Connection? = null

    private fun connect() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            Class.forName(Classes)
            connection = DriverManager.getConnection(url, username, password)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun sqlDB(gubun: String) {

        val phoneNumber = rectitle?.split("_")?.get(1)
        //val agentNum = "1"
        val recNum = Data.retundata()
        Log.e("recnum", recNum)
        val callNum = phoneNumber


        val current = LocalDateTime.now()
        //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        //val formatted = current.format(formatter)

        var startTime = Data.callStartTime
        var endTime = Data.callEndTime

        var start = formatter.format(startTime)
        var end = formatter.format(endTime)

        if (connection != null) {
            var statement: Statement? = null
            try {
                if (gubun == "ACTIVE") {
                    statement = connection!!.createStatement()
                    datetemp = System.currentTimeMillis()
                    //val sql =  "insert into [smart_DB].[dbo].[call_list] (agentNum, callType,callNum,startTime,outCallnum,inCallnum) values ('$agentNum','0','$callNum','$formatted','$$outCallnum','$$inCallnum')"
                    //statement.executeQuery(sql) // DB에 정보 넣기

                } else if (gubun == "DISCONNECTED") {
                    statement = connection!!.createStatement()
                    //val durations = (System.currentTimeMillis() - datetemp) / 1000
                    //val sql = "update call_list set recNum='$recNum', endTime='$formatted', duration='$durations', ringDuration='$$ringDuration' ,outCallnum='$$outCallnum',inCallnum='$$inCallnum',recYn='$$recYn'where agentNum='$&agentNum' and startTime='$$startTime'"
                    val sql =
                        "insert into [smart_DB].[dbo].[call_list] (agentNum,recNum,callType,callNum,startTime,endTime,duration,ringDuration,outCallnum,inCallnum,recYn) values ('" + agentnum + "','$recNum','0','$callNum','" + start + "','" + end + "','" + Data.duration.toString() + "','" + Data.ringtime.toString() + "','" + Data.phonenumber + "','" + agentPhone + "','"+Data.recYN+"')"
                    Log.e("sql", sql)
                    statement.executeQuery(sql) // DB에 정보 넣기
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
        }
    }


    private fun connFtp() {

        try {

            val arrayList = ArrayList<String>()
            File(path).walkBottomUp().forEach {
                arrayList.add(it.name)
            }

            val len = arrayList.size - 2
            val filename = arrayList[len]

            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.changeWorkingDirectory("/202102")

            con.enterLocalPassiveMode() // important!
            con.setFileType(FTP.BINARY_FILE_TYPE)

            val data = File(path, filename).toString()

            con.storeFile("$rectitle.m4a", FileInputStream(File(data)))
            FileInputStream(File(data)).close()
            con.logout()
            con.disconnect()

            val file = File(path)
            file.deleteRecursively()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun Getphonenum(context: Context) {

        var managedCusor: Cursor? = context.contentResolver.query(
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

            Data.phonenumber = AcallList
            when(Data.Ddata){
                "" -> passdata(AcallList)
            }
        }

        val Aduration = duration?.let { managedCusor?.getString(it) } //얼마나 통화했는지
        if (Aduration != null) {

            Data.duration = Aduration.toLong()
            Log.e("==============DDDDDration",Data.duration.toString())
            RingTime(Data.duration)
        }

        val Adate = date?.let { managedCusor?.getString(it) } //전화시간
        var calldaytime = Date(java.lang.Long.valueOf(Adate))
        var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        var callDaydate = formatter.format(calldaytime)






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


    private fun RingTime(Aduration: Long) {

        val start: Long = Data.callStartTime
        val end: Long = Data.callEndTime
        val duration: Long = Aduration
        Data.ringtime = ((end - (duration * 1000)) - start) / 1000
        Log.e("==============RRRRRingtime",Data.ringtime.toString())
    }



}