package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.wh_activity_dialer.*
import java.util.concurrent.TimeUnit


class WH_DialerActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
        val resultCode = 12345

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wh_activity_dialer)
        E.visibility = View.GONE
        checkDefaultDialer()
        if(
        // 엑티비티 실행에만 사용할 예정이라서 이슈가 있는 10 버전인지 확인하기 위함
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            &&
            // 다른 화면 위에 그리기 권한이 잇는지 확인
            !Settings.canDrawOverlays(this)
        ){
            // 사용자에게 이 권한이 왜 필요한지에 대해 설명하기 위한 다이얼로그
            val builder = AlertDialog.Builder(this).apply {
                setMessage("다른 화면 위에 표시하는 권한이 필요합니다.\n수락 하시겠습니까?")
                setCancelable(false)
                setNegativeButton("취소") { dialog, _ ->
                    // 취소 버튼 터치
                    dialog.dismiss();
                }
                    .setPositiveButton("수락") { dialog, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivityForResult(intent, resultCode)
                        dialog.dismiss();
                    }
            }
            builder.show();

        }else{
            // 기능 실행하기
        }

        num1.setOnClickListener {
            number.text = number.text.append("1")
        }

        num2.setOnClickListener {
            number.text = number.text.append("2")
        }

        num3.setOnClickListener {
            number.text = number.text.append("3")
        }

        num4.setOnClickListener {
            number.text = number.text.append("4")
        }

        num5.setOnClickListener {
            number.text = number.text.append("5")
        }

        num6.setOnClickListener {
            number.text = number.text.append("6")
        }

        num7.setOnClickListener {
            number.text = number.text.append("7")
        }

        num8.setOnClickListener {
            number.text = number.text.append("8")
        }

        num9.setOnClickListener {
            number.text = number.text.append("9")
        }

        num0.setOnClickListener {
            number.text = number.text.append("0")
        }

        delete.setOnClickListener{
            var len : Int = number.text.length
            val ran  = IntRange(0, len - 2)
            val temp = number.text.slice(ran)
            number.text.clear()
            number.text = number.text.append(temp)
        }
        E.setOnClickListener(){
            WH_OngoingCall.hangup()
        }
        move.setOnClickListener(){
            startActivity(Intent(this,WH_test::class.java))
        }
    }

    override fun onStart() {

        super.onStart()
        WH_OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        WH_OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe {}
            .addTo(disposables)

        call.setOnClickListener{
            makeCall()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateUi(state: Int) {

        E.visibility = when (state.asString()) {
            "DIALING" -> View.VISIBLE
            "ACTIVE" -> View.VISIBLE
            else -> View.GONE
        }
        
        if(state.asString() == "DIALING"){
            passdata()
        }

    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }


    private fun makeCall() {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val uri = "tel:${number.text}".toUri()
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    private fun checkDefaultDialer() {
        val mRoleManager = getSystemService(RoleManager::class.java)
        val isRoleAvailable = mRoleManager.isRoleAvailable(RoleManager.ROLE_DIALER)
        val isRoleHeld = mRoleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        Log.d("TAG", "isRoleAvailable : ${isRoleAvailable}, isRoleHeld : ${isRoleHeld}")
        val mRoleIntent = mRoleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        startActivityForResult(mRoleIntent, ROLE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun passdata(){
        val intent = Intent(applicationContext, WH_MyAccessibilityService::class.java)
        intent.putExtra("data", "${number.text}")
        startService(intent)
    }
}
