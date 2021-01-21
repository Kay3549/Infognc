package com.github.arekolek.phone

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.util.*

class MyAccessibilityService : AccessibilityService() {

    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    @SuppressLint("RtlHardcoded")
    override fun onCreate() {
        super.onCreate()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        println("onServiceConnected")

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        info.notificationTimeout = 100
        info.packageNames = null
        serviceInfo = info
        try {
            startRecordingA()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Handler().postDelayed({ // This method will be executed once the timer is over
            stopRecordingA()
        }, 30000)
    }

    private fun startRecordingA() {
        val fileName: String = Date().getTime().toString() + ".m4a"
        val file = File(this.externalCacheDir!!.absolutePath, fileName)
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource((MediaRecorder.AudioSource.MIC))
        mediaRecorder?.setOutputFormat((MediaRecorder.OutputFormat.MPEG_4))
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(file)
        Toast.makeText(this, "$file", Toast.LENGTH_SHORT).show()

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "레코딩 시작되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecordingA() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "중지 되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "레코딩 상태가 아닙니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //=================================================Added code start==========

    // To detect the connected other device like headphone, wifi headphone, usb headphone etc
    @SuppressLint("WrongConstant")

    companion object {
        const val LOG_TAG_S = "MyService:"
        const val CHANNEL_ID = "MyAccessibilityService"

        //=================================End================================
    }
}
