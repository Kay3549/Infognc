package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.app.role.RoleManager
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
import android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.dddd.*


class DialerActivity : AppCompatActivity() {

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dddd)
        checkDefaultDialer()

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
            val ran  = IntRange(0,len-2)
            val temp = number.text.slice(ran)
            number.text.clear()
            number.text = number.text.append(temp)
        }
    }

    override fun onStart() {
        super.onStart()
        call.setOnClickListener{
            makeCall()
        }
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
        Log.d("TAG", "isRoleAvailable : ${isRoleAvailable}, isRoleHeld : ${isRoleHeld}" )
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
}
