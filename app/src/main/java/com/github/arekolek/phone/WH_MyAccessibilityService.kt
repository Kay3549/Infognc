package com.github.arekolek.phone

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.telecom.Call
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.github.arekolek.phone.WH_OngoingCall.state
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import java.io.IOException
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
            .subscribe {}
            .addTo(disposables)

        Toast.makeText(this, "onCreate 집입", Toast.LENGTH_SHORT).show()
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
}

