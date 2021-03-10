package com.github.arekolek.phone

import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

import com.github.arekolek.phone.Permission

/**
 * 테스트 URL : http://192.168.1.206/login.asp?userName=agent1&password=1111
 * @author infognc 사원 이승환
 * @since 2021-02-23
 * */
class Login : AppCompatActivity() {



    // 멀티퍼미션 추가
    private var permission:Permission = Permission()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //퍼미션 확인
        permissionCheck()

        var inputId: EditText = findViewById(R.id.et_id)
        var inputPw: EditText = findViewById(R.id.et_pass)
        var loginBtn: Button = findViewById(R.id.btn_login)

        cookieStart()

        loginBtn.setOnClickListener() {
            loginConnection(inputId,inputPw)
        }
    }


    /**
     * 로그인 성공시 성공 메시지와 함께 다음 ACTIVITY 로 이동후 FINISH ,로그인 실패시 실패 메시지만 날림
     * */
    private fun successLogin(result: String, inputId:String, inputPw: String) {
        var msg = "Login Fail"
        if (result == "OK") {
            val secondIntent = Intent(this, Logindetail::class.java)
            secondIntent.putExtra("id", inputId)
            secondIntent.putExtra("pw", inputPw)
            startActivity(secondIntent)
            finish()
            msg = "Login Success"
        }
        toast(msg)
    }


    /**
     * 쿠키매니저 실행
     * */
    private fun cookieStart() {
        val cookieManager = CookieManager()
        CookieHandler.setDefault(cookieManager)
    }


    /**
     * 하단에 토스트 메시지 생성
     * */
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    /**
     * ID 와 PW 입력받고 로그인 버튼 입력시 OK를 받을때 서버쪽 세션을 받아와 쿠키매니저에 저장
     * */
    private fun loginConnection(inputId: EditText, inputPw: EditText) {
        thread(true) {
            val inputUrl: String = " http://192.168.1.206/login.asp?userName=${inputId.text}&password=${inputPw.text}"
            val url = URL(inputUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            val streamReader = InputStreamReader(urlConnection.inputStream)
            val buffered = BufferedReader(streamReader)
            val content = StringBuilder()

            try {
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    while (true) {
                        val line = buffered.readLine() ?: break
                        content.append(line)
                    }
                    buffered.close()
                    urlConnection.disconnect()
                    runOnUiThread {
                        successLogin(content.toString(),inputId.text.toString(),inputPw.text.toString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // 퍼미션 권한 체크
    private fun permissionCheck(){
        // SDK 23버전 이하 버전에서는 Permission이 필요없음
        if(Build.VERSION.SDK_INT >= 23){
            permission.PermissionSupport(this,this)

            if(!permission.checkPermission()){
                permission.requestPermission()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray){
        if(!permission.permissionResult(requestCode, permissions, grantResults)){
            permission.requestPermission()
        }
    }

}

