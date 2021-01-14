package com.github.arekolek.phone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.telecom.Call
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.abcd.*
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_call.answer
import kotlinx.android.synthetic.main.activity_call.callInfo
import kotlinx.android.synthetic.main.activity_call.hangup
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class CallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private lateinit var number: String
    private var timerDisposable = Disposables.empty()

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.abcd)
        hideBottomNavigationBar()
        number = intent.data.schemeSpecificPart
    }

    override fun onStart() {
        super.onStart()

        buttonAnswer.setOnClickListener {
            OngoingCall.answer()
        }

        buttonHangup.setOnClickListener {
            OngoingCall.hangup()
        }

        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(state: Int) {
        textStatus.text = "${state.asString().toLowerCase().capitalize()}\n$number"   //통화 상태와 상대방의 번호를 나타내 주는 부분.

        when (state.asString()) {
            "ACTIVE"      -> startTimer()
            "DISCONNECTED" -> stopTimer()
            else                        -> Unit
        }

        record.visibility = when (state.asString()) {
            "ACTIVE" ->  View.VISIBLE
            else                        -> View.GONE
        }

        EndRecord.visibility = when (state.asString()) {
            "ACTIVE" ->  View.VISIBLE
            else                        -> View.GONE
        }

        buttonHangup.visibility = when (state.asString()) {
            "DISCONNECTED" -> View.GONE
            else                        -> View.VISIBLE
        }

        buttonAnswer.visibility = when (state.asString()) {
            "RINGING" -> View.VISIBLE
            else                   -> View.GONE
        }
        record.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //Permission is not granted
                    val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permissions,0)
                } else {
                    startRecording()
                }
        }

        EndRecord.setOnClickListener {
            stopRecording()
        }
    }

    private fun hideBottomNavigationBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun startTimer() {
        timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textDuration.text = it.toDurationString() }
    }
    private fun stopTimer() {
        timerDisposable.dispose()
    }

    private fun startRecording(){
        //config and create MediaRecorder Object
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss")
        val formatted = current.format(formatter)
        val fileName: String = formatted.toString()
        output = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + fileName //내장메모리 밑에 위치
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource((MediaRecorder.AudioSource.MIC))
        mediaRecorder?.setOutputFormat((MediaRecorder.OutputFormat.MPEG_4))
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException){
            e.printStackTrace()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            state = false
        } else {
        }
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

    private fun Long.toDurationString() = String.format("%02d:%02d:%02d", this / 3600, (this % 3600) / 60, (this % 60))



}
