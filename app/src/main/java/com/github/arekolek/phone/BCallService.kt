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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BCallService : BroadcastReceiver() {

    private var mLastState: String = ""
    private var rectitle: String? = ""
    private var datetemp: kotlin.Long = 0
    private var path = "/storage/emulated/0/Call"

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

//            connect()
//            sqlDB("ACTIVE")

        //통화중이 아닐 때
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Data.callEndTime = System.currentTimeMillis()

            Log.d("===============","Ddata: " + Data.Ddata)
            Log.d("===============","callEndTime: " + Data.callEndTime)
            Log.d("===============","callStartTime: " + Data.callStartTime)
            Log.d("===============","ringtime: " + Data.ringtime)
            Log.d("===============","phonenumber: " + Data.phonenumber)
            val intent = Intent(context,WHservice::class.java)
            context.startService(intent)
            Toast.makeText(context,"asdf",Toast.LENGTH_SHORT).show()
            rectitle = Data.retundata() // 녹취 키 가져오기
            context.stopService(intent)

//            connect()     //db connect
//            sqlDB("DISCONNECTED")  // db 적재
//            connFtp() //ftp 올리기
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
        val agentNum = "1"
        val recNum = rectitle
        val callNum = phoneNumber
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val formatted = current.format(formatter)
        if (connection != null) {
            var statement: Statement? = null
            try {
                if (gubun == "ACTIVE") {
                    statement = connection!!.createStatement()
                    datetemp = System.currentTimeMillis()
                    val sql =
                        "insert into [smart_DB].[dbo].[call_list] (agentNum, recNum,callType,callNum,startTime,endTime,duration) values ('$agentNum','$recNum','0','$callNum','$formatted','','')"
                    statement.executeQuery(sql) // DB에 정보 넣기

                } else if (gubun == "DISCONNECTED") {
                    statement = connection!!.createStatement()
                    val durations = (System.currentTimeMillis() - datetemp) / 1000
                    val sql =
                        "update call_list set endTime='$formatted', duration='$durations' where recNum='$recNum'"
                    statement.executeQuery(sql) // DB에 정보 넣기

                    Log.d("update", "UPDATE: " + sql)
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

}