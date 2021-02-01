package com.github.arekolek.phone

import android.os.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null

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

    }

    private fun sqlDB(){
        var data = ArrayList<String>()
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n where agentNum = '1'"
                val resultSet = statement.executeQuery(sql) // DB


                while (resultSet.next()) {
                    var str = resultSet.getString(1) + " " + resultSet.getString(4) + " " + resultSet.getString(2)+ " " + resultSet.getString(3)
                    data.add(str)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }

        var list = findViewById<ListView>(R.id.listview)

        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data)
        list.setAdapter(adapter)

        adapter.addAll(data)
        adapter.notifyDataSetChanged()

        list.setOnItemClickListener(AdapterView.OnItemClickListener() {
                adapterView: AdapterView<*>, view: View, position: Int, l: Long ->
            fun OnItemClick(){
                var choise:String = adapterView.getItemAtPosition(position) as String
            }
        })
    }
}
