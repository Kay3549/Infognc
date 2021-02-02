package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import com.mbarrben.dialer.CallManager
import com.uber.rxdogtag.RxDogTag
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.wh_activity_dialer.*
import kotlinx.android.synthetic.main.wh_activity_main_click.*
import timber.log.Timber
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.TimeUnit


class WH_DialerActivity : AppCompatActivity() {

    private var updatesDisposable = Disposables.empty()
    private var callbutton = 0
    private var connect = 0
    private var number =""

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var sum:String? = null

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
        val resultCode = 12345

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wh_activity_main_click)
        checkDefaultDialer()


        if (
        // 엑티비티 실행에만 사용할 예정이라서 이슈가 있는 10 버전인지 확인하기 위함
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            &&
            // 다른 화면 위에 그리기 권한이 잇는지 확인
            !Settings.canDrawOverlays(this)
        ) {
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

        } else {
            // 기능 실행하기
        }
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(Classes)
            connection = DriverManager.getConnection(url, username, password)
            Log.d("sqlDB", "SUCCCESS")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.d("sqlDB", "ERROR")
        } catch (e: SQLException) {
            e.printStackTrace()
            Log.d("sqlDB", "ERROR")
        }

        val counStepsp = findViewById<Spinner>(R.id.counStep)
        counStepsp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                parent.getItemAtPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val contTypesp = findViewById<Spinner>(R.id.contType)
        contTypesp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                parent.getItemAtPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val intent: Intent = intent

        val a = intent.getStringExtra("DB")
        Log.d("DB", "DB: " + a)
        val b = a?.split(" | ")
        Log.d("B", "B: " + b)

        val custkey = b?.get(0)
        if (custkey != null) {
            sqlDB(custkey)
            val c = sum?.split("|")
            Log.d("DB", "C: " + c)
            val custNum = findViewById<TextView>(R.id.custNum)
            custNum.text = c?.get(0)
            val custBirth = findViewById<TextView>(R.id.custBirth)
            custBirth.text = c?.get(5)
            val custName = findViewById<TextView>(R.id.custName)
            custName.text = c?.get(3)
            val custSex = findViewById<TextView>(R.id.custSex)
            custSex.text = c?.get(4)
            val agreeDate = findViewById<TextView>(R.id.agreeDate)
            agreeDate.text = c?.get(6)
            val agreeType = findViewById<TextView>(R.id.agreeType)
            agreeType.text = c?.get(7)
            val cellNum = findViewById<TextView>(R.id.phoneNum)
            cellNum.text = c?.get(8)
            val tellNum = findViewById<TextView>(R.id.callNum)
            tellNum.text = c?.get(9)
        }
        call1.text = "연결"
        call2.text = "연결"
        call3.text = "연결"
    }

    override fun onStart() {

        super.onStart()

        call1.setOnClickListener {

            if(connect==0){
                call2.visibility = View.GONE
                call3.visibility = View.GONE
                callbutton = 1
                makeCall(phoneNum.text as String)
                number= phoneNum.text as String
                call1.text="끊기"
                connect=1

            }else{
                CallManager.cancelCall()
                connect=0
                call2.visibility = View.VISIBLE
                call3.visibility = View.VISIBLE
                call1.text ="통화"
            }

        }
        call2.setOnClickListener {
            if(connect==0){
                call1.visibility = View.GONE
                call3.visibility = View.GONE
                makeCall(callNum.text as String)
                callbutton = 2
                number= phoneNum.text as String
                call2.text="끊기"
                connect=1
            }else{
                CallManager.cancelCall()
                call1.visibility = View.VISIBLE
                call3.visibility = View.VISIBLE
                connect=0
                call2.text ="통화"
            }

        }
        call3.setOnClickListener {
            if(connect==0){
                call2.visibility = View.GONE
                call3.visibility = View.GONE
                makeCall(dirctNum.text as String)
                callbutton = 3
                number= phoneNum.text as String
                call3.text="끊기"
                connect=1
            }else{
                CallManager.cancelCall()
                call2.visibility = View.VISIBLE
                call3.visibility = View.VISIBLE
                connect=0
                call3.text ="통화"
            }

        }
    }

    override fun onResume() {
        super.onResume()
        updatesDisposable = CallManager.updates()

            .doOnEach { Timber.e("$it") }
            .doOnError { Timber.e("Error processing call") }
            .subscribe { updateView(it) }
    }

    private fun updateView(gsmCall: GsmCall) {

        if(gsmCall.status==GsmCall.Status.DIALING){
            //passdata()
        }
    }

    private fun makeCall(number:String) {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val uri = "tel:${number}".toUri()
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    private fun checkDefaultDialer() {
        val mRoleManager = getSystemService(RoleManager::class.java)
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

    private fun passdata() {
        val intent = Intent(applicationContext, WH_MyAccessibilityService::class.java)
        // val intent1 = Intent(applicationContext, WH_test::class.java)
        intent.putExtra("data", number)
        // intent1.putExtra("date","${number.text}")
        startService(intent)
        //startActivity(intent1)
    }

    private fun sqlDB(custkey: String){
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName,info.custSex,info.custBirth,info.agreeDate,info.agreeType,info.cellNum,info.tellNum \n" +
                            "from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n" +
                            "where db.agentNum = '1' and db.custNum = '"+custkey+"'\n"
                val resultSet = statement.executeQuery(sql) // DB


                while (resultSet.next()) {
                    val custnum = resultSet.getString(1)
                    val counStep = resultSet.getString(2)
                    val alocdate = resultSet.getString(3)
                    val custName = resultSet.getString(4)
                    val custSex = resultSet.getString(5)
                    val custBirth = resultSet.getString(6)
                    val agreeDate = resultSet.getString(7)
                    val agreeType = resultSet.getString(8)
                    val cellNum = resultSet.getString(9)
                    val tellNum = resultSet.getString(10)

                    sum =
                        "$custnum|$counStep|$alocdate|$custName|$custSex|$custBirth|$agreeDate|$agreeType|$cellNum|$tellNum"

                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }

    }
}
