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
import com.github.arekolek.phone.OngoingCall.state
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class MyAccessibilityService() : AccessibilityService()  {

    var windowManager: WindowManager? = null
    var recorder = MediaRecorder()
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
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //==============================Record Audio while  Call received===============//
//        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//        val layout = FrameLayout(this)
//        val params = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
//                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//            PixelFormat.TRANSLUCENT
//        )
//        params.gravity = Gravity.TOP
//        windowManager.addView(layout, params)
//        layout.setOnTouchListener { _, _ -> //You can either get the information here or on onAccessibilityEvent
//            Timber.e("Window view touched........:")
//            Timber.e("Window view touched........:")
//            true
//        }
//
//        //==============To Record Audio wile Call received=================
//
//        val info = AccessibilityServiceInfo()
//        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
//        info.notificationTimeout = 100
//        info.packageNames
//        serviceInfo = info
//        Toast.makeText(this,"녹음 준비 완료",Toast.LENGTH_SHORT).show()
//        try {
//            Toast.makeText(this,"들어왔습니다.",Toast.LENGTH_SHORT).show()
//            startRecordingA()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    override fun onInterrupt() {
//        val info = AccessibilityServiceInfo()
//        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
//        info.notificationTimeout = 100
//        info.packageNames
//        serviceInfo = info
//        Toast.makeText(this,"녹음 종료 준비 완료",Toast.LENGTH_SHORT).show()
//        try {
//            stopRecordingA()
//            Toast.makeText(this,"녹음 종료합니다.",Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {

    }

    private fun startRecordingA() {
        // This must be needed sourcea
        Toast.makeText(this, "녹음시작", Toast.LENGTH_SHORT).show()
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss")
        val formatted = current.format(formatter)

        val fileName ="$formatted.m4a"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

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

