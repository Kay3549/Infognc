package com.github.arekolek.phone

import android.R.attr.path
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.StrictMode
import android.telecom.Call
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.github.arekolek.phone.WH_OngoingCall.state
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class WH_MyAccessibilityService() : AccessibilityService() {

    var windowManager: WindowManager? = null
    var recorder = MediaRecorder()
    private var phoneNumber: String? = ""
    private val disposables = CompositeDisposable()
    private var fileName: String? = ""
    private var datetemp: Long = 0
    private var file: String? = ""

    @SuppressLint("RtlHardcoded")
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Timber.i("MyAccessibilityService")
        state
            .subscribe(::updateUi)
            .addTo(disposables)

        state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { }
            .addTo(disposables)

        Toast.makeText(this, "onCreate 집입", Toast.LENGTH_SHORT).show()
        connect()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }

    override fun onInterrupt() {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.extras!!.containsKey("data")) phoneNumber = intent.getStringExtra("data")
        Toast.makeText(this, "$phoneNumber" + "들어옴", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    private fun startRecordingA() {
        Toast.makeText(this, "녹음시작", Toast.LENGTH_SHORT).show()
        var k = phoneNumber.toString().length
        var phoneNum: String? = ""
        phoneNum = if (k <= 15) {
            phoneNumber
        } else {
            var ran = IntRange(0, 14)
            var temp = phoneNumber.toString().slice(ran)
            temp
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS_$phoneNum")
        val formatted = current.format(formatter)

        fileName = "$formatted.m4a"

        file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName).toString()
        Toast.makeText(this, "$file", Toast.LENGTH_SHORT).show()
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setOutputFile(file)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
        recorder.setAudioEncodingBitRate(48000)
        recorder.setAudioSamplingRate(16000)

        try {
            recorder.prepare()
        } catch (e: IOException) {
            Timber.e("prepare() failed")
        }
        recorder.start()
    }

    private fun stopRecordingA() {
        Toast.makeText(this, "녹음 종료", Toast.LENGTH_SHORT).show()
        Timber.e("stop recording")
        recorder.stop()
        recorder.release()
    }


    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"

    private var connection: Connection? = null

    fun connect() {
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
        val agentNum = "1"
        val recNum = fileName.toString().replace(".m4a", "")
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
                }


            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
        }
    }

    private fun connFtp1() {
        Toast.makeText(this, "ftp", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "ftp0", Toast.LENGTH_SHORT).show()
        val mFtpClient = FTPClient()
        val check = mFtpClient.connect("192.168.1.206")
        mFtpClient.login("administrator", ".Digital")
        Toast.makeText(this, "$check", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "ftp2", Toast.LENGTH_SHORT).show()
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun connFtp() {
        try {
            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.enterLocalPassiveMode() // important!
            con.setFileType(FTP.BINARY_FILE_TYPE)
            val destination = con.changeWorkingDirectory("192.168.1.206").toString()
            val data = "$file"
            val path : String = con.printWorkingDirectory()

            con.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE)
            con.setFileTransferMode(FTP.BINARY_FILE_TYPE)

            con.storeFile(fileName, FileInputStream(File(data)))
            FileInputStream(File(data)).close()
            con.logout()
            con.disconnect()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUi(state: Int) {
        when (state.asString()) {
            "ACTIVE" -> {
                startRecordingA()
                // sqlDB("ACTIVE")
            }
            "DISCONNECTED" -> {
                stopRecordingA()
                //connFtp()
                //sqlDB("DISCONNECTED")
            }
        }
    }

}



