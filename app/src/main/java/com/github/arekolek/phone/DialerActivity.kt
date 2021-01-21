package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.dddd.*
import java.io.File
import java.io.IOException
import java.util.*


class DialerActivity : AppCompatActivity() {

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
        val resultCode = 12345
        private var mediaRecorder: MediaRecorder? = null
        private var state: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dddd)
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
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            , Uri.parse("package:$packageName"))
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
            val ran  = IntRange(0,len-2)
            val temp = number.text.slice(ran)
            number.text.clear()
            number.text = number.text.append(temp)
        }
        E.setOnClickListener{
            if (
                ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                requestPermissions(this, permissions, 0)
            } else {
            startRecording()
        }
        }
        button6.setOnClickListener{
            stopRecording()
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

    private fun startRecording() {
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

    private fun stopRecording() {
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
}
