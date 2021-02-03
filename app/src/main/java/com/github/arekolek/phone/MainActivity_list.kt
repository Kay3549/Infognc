package com.github.arekolek.phone

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

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

        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        list.setAdapter(adapter)

        adapter.addAll(data)
        adapter.notifyDataSetChanged()

        Log.d("sqlCall","적재성공")

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, MainActivity_history::class.java)
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
                    "select coun.custNum,cal.startTime,coun.custName,coun.counStep from counsel_list as coun \n" +
                            "inner join call_list as cal\n" +
                            "on coun.recNum = cal.recNum\n" +
                            "where coun.agentNum = '1'"
                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)
                while (resultSet.next()) {
                    Log.d("select", "값: " + resultSet.getString(1))
                    Log.d("select", "값: " + resultSet.getString(2))
                    Log.d("select", "값: " + resultSet.getString(3))

                    var counStep:String? = resultSet.getString(4)
                    Log.d("sqlDB", "counStep: " + counStep)
                    counStep = when (resultSet.getString(4)){
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

                    var date = resultSet.getString(2)?.split(".")
                    var str = resultSet.getString(1) + "  | " + date?.get(0) + "  |   " + resultSet.getString(3) + "    |      " + counStep
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
        if (id == R.id.action_btn1) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}