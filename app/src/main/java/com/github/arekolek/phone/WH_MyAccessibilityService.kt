package com.github.arekolek.phone

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.mbarrben.dialer.CallManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
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


class WH_MyAccessibilityService() : AccessibilityService() {

    private var updatesDisposable = Disposables.empty()
    var windowManager: WindowManager? = null
    var recorder = MediaRecorder()
    private var rectitle: String? = ""
    private val disposables = CompositeDisposable()
    private var fileName: String? = ""
    private var datetemp: Long = 0
    private var file: String? = ""


    @SuppressLint("RtlHardcoded", "CheckResult")
    override fun onCreate() {

        super.onCreate()

        Timber.plant(Timber.DebugTree())
        updatesDisposable = CallManager.updates()

            .doOnEach { Timber.e("$it") }
            .doOnError { Timber.e("Error processing call") }
            .subscribe { updateView(it) }

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
        if (intent.extras!!.containsKey("data")) rectitle = intent.getStringExtra("data")
        return START_STICKY
    }

    private fun startRecordingA() {

        fileName = "$rectitle.m4a"

        file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName).toString()
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
        Log.d("err","recnum: "+ recNum)
        if (connection != null) {
            var statement: Statement? = null
            try {
                if (gubun == "ACTIVE") {
                    statement = connection!!.createStatement()
                    datetemp = System.currentTimeMillis()
                    val sql =
                        "insert into [smart_DB].[dbo].[call_list] (agentNum, recNum,callType,callNum,startTime,endTime,duration) values ( '"+agentNum+"','"+recNum+"','0','"+callNum+"','$formatted','','')"
                    statement.executeQuery(sql) // DB에 정보 넣기
                    Log.d("insert","insertDB:" + sql)

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


    private fun connFtp() {
        try {
            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.changeWorkingDirectory("/202102")

            con.enterLocalPassiveMode() // important!
            con.setFileType(FTP.BINARY_FILE_TYPE)
            val data = "$file"

            con.storeFile(fileName, FileInputStream(File(data)))
            FileInputStream(File(data)).close()
            con.logout()
            con.disconnect()



        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun updateView(gsmCall: GsmCall) {
        when (gsmCall.status) {
            GsmCall.Status.ACTIVE -> {
                startRecordingA()
                sqlDB("ACTIVE")
            }
            GsmCall.Status.DISCONNECTED -> {
                stopRecordingA()
                sqlDB("DISCONNECTED")
                connFtp()
                val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
                file.deleteRecursively()
            }
        }
    }
}



