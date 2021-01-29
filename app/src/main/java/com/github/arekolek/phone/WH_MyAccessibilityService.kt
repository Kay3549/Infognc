package com.github.arekolek.phone

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
import timber.log.Timber
import java.io.File
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
            .subscribe { System.exit(0) }
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
        var phoneNum :String?= ""
        phoneNum = if(k<=5){
            phoneNumber
        }else{
            var ran = IntRange(0, 4)
            var temp = phoneNumber.toString().slice(ran)
            temp
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssmmSSS_$phoneNum")
        val formatted = current.format(formatter)

        val fileName = "$formatted.m4a"

        val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
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

    private fun updateUi(state: Int) {
        when (state.asString()) {
            "ACTIVE" -> startRecordingA()
            "DISCONNECTED" -> stopRecordingA()
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

    fun connect(){
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

    fun sqlDB(){

        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "insert into [smart_DB].[dbo].[counsel_list] (agentNum, recNum, custNum, idxCounDB, custName, counStep, contType ,counMemo) values ('','','','','','','','$counMemo')"
                /* ResultSet resultSet = statement.executeQuery("SELECT [idxCounDB],[custNum] FROM [smart_DB].[dbo].[counsel]");*/ // 가져오기
                val resultSet = statement.executeQuery(sql) // DB에 정보 넣기
                Log.d("TABLE", "TABLE: $resultSet")
                while (resultSet.next()) {
                 // 프리페어스테이트먼트
                    /*etSql.setText(resultSet.getString(2));
                    Log.d("TABLE","TABLE: "+ resultSet);*/
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }
}

