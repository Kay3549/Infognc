package com.github.arekolek.phone

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class MainActivity_list : AppCompatActivity() {

    // 통화시작시간: startTime , 고객이름: custName, 진행단계: counStep

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var data = ArrayList<String>()
    //커스텀 리스트뷰를 위해 추가
    private var CodeItem = ArrayList<String>()
    private var CodeName = ArrayList<String>()
    private var num = ""
    private var startcall = ""
    private var gogeak = ""
    private var count = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)
        // 위에 상단바 고정
        supportActionBar?.setTitle("Infognc")

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
        var list = findViewById<ListView>(R.id.listview_sangdam)

        // 리스트뷰 헤더 추가
        var header: View = layoutInflater.inflate(R.layout.listview_header2, null, false)
        list.addHeaderView(header,null,false)

        var adapter = Viewer1()

        var datalen = data
        Log.d("DATA", "DATA: " + datalen)

        for(i in datalen.indices){
            var data1 = datalen.get(i).split(",")
            Log.d("data1","DATA1:" + data1 )
            for(j in data1.indices){
                num = data1.get(0)
                startcall = data1.get(2)
                gogeak = data1.get(3)
                count = data1.get(4)

                Log.d("data1","num:" + num+", startcall:"+startcall+", gogeak:"+gogeak+", count:"+count)
            }
            adapter.addItem(num,startcall,gogeak,count)
        }

        list.setAdapter(adapter)

        Log.d("sqlCall","적재성공")

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, MainActivity_history::class.java)
            intent.putExtra("DB", data[position-1])
            startActivity(intent)
        }
    }

    fun sqlDB(){
        // step() 함수 사용
        step()
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select coun.custNum,cal.startTime,coun.custName,coun.counStep from counsel_list as coun \n" +
                            "inner join call_list as cal\n" +
                            "on coun.recNum = cal.recNum\n" +
                            "where coun.agentNum = '1'"
                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)

                var count = 0
                while (resultSet.next()) {
                    var counStep:String? = resultSet.getString(4)
                    Log.d("sqlDB", "counStep: " + counStep)
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
                    count = count+1
                    var date = resultSet.getString(2)?.split(".")
                    var str = count.toString() + "," + resultSet.getString(1) + "," + date?.get(0) + "," + resultSet.getString(3) + "," + step
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
    // 함수추가
    fun step(){
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


    //액션바
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //액션바 클릭시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
        // 상담
        if (id == R.id.action_btn1) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}