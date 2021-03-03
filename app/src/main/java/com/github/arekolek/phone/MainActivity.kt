package com.github.arekolek.phone

import android.Manifest
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.CallLog
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement


class MainActivity : AppCompatActivity()  {
    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var data = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //checkDefaultDialer()



        val br : BroadcastReceiver = BCallService()
        val filter = IntentFilter().apply {
            addAction("android.intent.action.PHONE_STATE")
            //addAction("com.github.arekolek.phone")
        }
        registerReceiver(br, filter)


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                1
            )
        }




//        if (
//        // 엑티비티 실행에만 사용할 예정이라서 이슈가 있는 10 버전인지 확인하기 위함
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//            &&
//            // 다른 화면 위에 그리기 권한이 잇는지 확인
//            !Settings.canDrawOverlays(this)
//        ) {
//            // 사용자에게 이 권한이 왜 필요한지에 대해 설명하기 위한 다이얼로그
//            val builder = AlertDialog.Builder(this).apply {
//                setMessage("다른 화면 위에 표시하는 권한이 필요합니다.\n수락 하시겠습니까?")
//                setCancelable(false)
//                setNegativeButton("취소") { dialog, _ ->
//                    // 취소 버튼 터치
//                    dialog.dismiss();
//                }
//                    .setPositiveButton("수락") { dialog, _ ->
//                        val intent = Intent(
//                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:$packageName")
//                        )
//                        startActivityForResult(intent, WH_DialerActivity.resultCode)
//                        dialog.dismiss();
//                    }
//            }
//            builder.show();
//
//        } else {
//            // 기능 실행하기
//        }

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
        sqlDB()
        var list = findViewById<ListView>(R.id.listview)

        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        list.adapter = adapter

        adapter.addAll(data)
        adapter.notifyDataSetChanged()

        Log.d("sqlCall", "적재성공")

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, WH_DialerActivity::class.java)
            intent.putExtra("DB", data[position])
            startActivity(intent)
        }
    }
    fun sqlDB(){
        data = ArrayList<String>()
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n where agentNum = '1'"
                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)
                while (resultSet.next()) {

                    var counStep:String? = resultSet.getString(2)


                    Log.d("sqlDB", "counStep: " + counStep)

                    counStep = when (resultSet.getString(2)){
                        "00" -> "미접촉   "
                        "01" -> "거부     "
                        "02" -> "수신거부"
                        "03" -> "결번     "
                        "04" -> "부재중   "
                        "05" -> "진행     "
                        "06" -> "예약     "
                        "30" -> "가입완료"
                        else -> "   "
                    }

                    var date = resultSet.getString(3)?.split(" ")

                    var str = resultSet.getString(1) + "  |   " +resultSet.getString(4) + "      |      " + counStep + "        |     " + date?.get(0)
                    Log.d("str", "STR: " + str)
                    data.add(str)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    //액션바
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //액션바 클릭시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        // 상담
        //이력
        if (id == R.id.action_btn2) {
            val intent = Intent(applicationContext, MainActivity_list::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkDefaultDialer() {
        val mRoleManager = getSystemService(RoleManager::class.java)
        val mRoleIntent = mRoleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        startActivityForResult(mRoleIntent, WH_DialerActivity.ROLE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WH_DialerActivity.ROLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            } else {
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//        val filter = IntentFilter().apply {
//            addAction("android.intent.action.PHONE_STATE")
//            addAction("com.github.arekolek.phone")
//        }
//        registerReceiver(br, filter)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(br)
//    }

}