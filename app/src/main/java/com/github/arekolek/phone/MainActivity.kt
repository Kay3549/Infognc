package com.github.arekolek.phone

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    // 커스텀 리스트뷰를 사용하기 위하여 추가
    private var CodeItem = ArrayList<String>()
    private var CodeName = ArrayList<String>()
    private var num = ""
    private var gogeak = ""
    private var count = ""
    private var db = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                        startActivityForResult(intent, WH_DialerActivity.resultCode)
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
        list.setAdapter(adapter)

        Log.d("sqlCall", "적재성공")

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, WH_DialerActivity::class.java)
            intent.putExtra("DB", data[position-1])
            startActivity(intent)
        }
    }

    fun step(){
        // code_manager 테이블 가져오기 위한 함수
        if(connection != null){
            var statement: Statement?
            try{
                statement = connection!!.createStatement()
                val sql =
                    "select codeItem,codeName from code_manager"

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
                // 앞에 카운터 추가
                var count = 0
                
                while (resultSet.next()) {
                    // 하드코딩 변경
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

    //액션바
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //액션바 클릭시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
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

}