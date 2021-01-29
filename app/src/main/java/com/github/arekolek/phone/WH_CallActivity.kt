package com.github.arekolek.phone
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.view.View
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.wh_activity_call.*
import java.util.concurrent.TimeUnit

class WH_CallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()
    private lateinit var number: String
    private var timerDisposable = Disposables.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wh_activity_call)

        number = intent.data.schemeSpecificPart

    }

    override fun onStart() {
        super.onStart()

        buttonAnswer.setOnClickListener {
            WH_OngoingCall.answer()
        }

        buttonHangup.setOnClickListener {
            WH_OngoingCall.hangup()
        }

        WH_OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        WH_OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)

    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(state: Int) {
        textStatus.text =
            "${state.asString().toLowerCase().capitalize()}\n$number"   //통화 상태와 상대방의 번호를 나타내 주는 부분.

        when (state.asString()) {
            "ACTIVE" -> startTimer()
            "DISCONNECTED" -> stopTimer()
            else -> Unit
        }

        buttonHangup.visibility = when (state.asString()) {
            "DISCONNECTED" -> View.GONE
            else -> VISIBLE
        }

        buttonAnswer.visibility = when (state.asString()) {
            "RINGING" -> VISIBLE
            else -> View.GONE
        }
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

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, WH_CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

    private fun Long.toDurationString() =
        String.format("%02d:%02d:%02d", this / 3600, (this % 3600) / 60, (this % 60))
}