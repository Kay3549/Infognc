package com.github.arekolek.phone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main_click.*
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
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data)
        list.setAdapter(adapter)

        adapter.addAll(data)
        adapter.notifyDataSetChanged()

        Log.d("sqlCall","적재성공")
        
        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var intent = Intent(this, MainActivity_click::class.java)
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

                while (resultSet.next()) {

                var counStep:String? = resultSet.getString(2)


                Log.d("sqlDB", "counStep: " + counStep)

                counStep = when (resultSet.getString(2)){
                    "00"-> "미접촉"
                    "01" -> "거부"
                    "02" -> "거부"
                    "03" -> "거부"
                    "04" -> "거부"
                    "05" -> "거부"
                    "06" -> "거부"
                    "30" -> "가입완료"
                    else -> "   "
                }
                    var str = resultSet.getString(1) + " | " + resultSet.getString(4) + " | " + counStep + " | " + resultSet.getString(3)
                    data.add(str)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    /*override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var list = findViewById<ListView>(R.id.listview)
        var intent = Intent(this, MainActivity_clicklist::class.java)
        intent.putExtra("custNum", data[position])
        startActivity(intent)
    }*/

}