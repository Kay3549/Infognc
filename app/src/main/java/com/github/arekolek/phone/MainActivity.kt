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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
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
    private var CodeItem = ArrayList<String>()
    private var CodeName = ArrayList<String>()
    private var num = ""
    private var gogeak = ""
    private var count = ""
    private var db = ""


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

        // 리스트뷰 헤더 추가
        var header: View = layoutInflater.inflate(R.layout.listview_header, null, false)
        list.addHeaderView(header,null,false)

        var adapter = Viewer()

        var datalen = data
        Log.d("DATA", "DATA: " + datalen)

        for(i in datalen.indices){
            var data1 = datalen.get(i).split(",")
            Log.d("data1","DATA1:" + data1 )
            for(j in data1.indices){
                num = data1.get(0)
                gogeak = data1.get(2)
                count = data1.get(3)
                db = data1.get(4)

                Log.d("data1","num:" + num+", gogeak:"+gogeak+", count:"+count+", db:"+db)
            }
            adapter.addItem(num,gogeak,count,db)
        }
        adapter.notifyDataSetChanged()
        list.setAdapter(adapter)

        Log.d("sqlCall", "적재성공")

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, WH_DialerActivity::class.java)
            intent.putExtra("DB", data[position-1])
            startActivity(intent)
        }

        // 액션바 실행
        var susin = findViewById<Button>(R.id.recv)
        susin.isInvisible=true

        var action1 = findViewById<Button>(R.id.coun)
        action1.isEnabled = false

        var action2 = findViewById<Button>(R.id.history)
        action2.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity_list::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 뒤로가기 2번
    private var backPressedTime : Long = 0
    override fun onBackPressed() {
        Log.d("TAG", "뒤로가기")

        // 2초내 다시 클릭하면 앱 종료
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
            return
        }

        // 처음 클릭 메시지
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }

    fun step(){
        if(connection != null){
            var statement: Statement?
            try{
                statement = connection!!.createStatement()
                val sql =
                    "select codeItem,codeName from code_manager where codePart='00' and delFlag='N'"

                val resultSet = statement.executeQuery(sql)

                Log.d("Spinner", "Spinner")

                while(resultSet.next()){
                    var codeItem = resultSet.getString(1)
                    var codeName = resultSet.getString(2)
                    CodeName.add(codeName)
                    CodeItem.add(codeItem)
                }
            } catch (e:SQLException){
                e.printStackTrace()
            }
        } else{
            Log.d("sqlDB", "Connection is null")
        }
    }


    fun sqlDB(){
        step()
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n where agentNum = '1'"
                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)

                var count = 0

                while (resultSet.next()) {
                    var counStep:String? = resultSet.getString(2)
                    var step = ""
                    var codeitem = CodeItem
                    var codename = CodeName
                    for(i in codeitem.indices){
                        var cd = codeitem.get(i)
                        var cd2 = codename.get(i)
                        if(cd == counStep){
                            step = cd2
                        }
                    }

                    var date = resultSet.getString(3)?.split(" ")
                    count = (count+1)
                    Log.d("count", "count: " + count)

                    var str = count.toString()+","+ resultSet.getString(1)+"," + resultSet.getString(4)+"," + step+"," +  date?.get(0).toString()
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
}