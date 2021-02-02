package com.github.arekolek.phone

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class MainActivity_click : AppCompatActivity() {

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var sum:String? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wh_activity_main_click)

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
            custNum.setText(c?.get(0))
            val custBirth = findViewById<TextView>(R.id.custBirth)
            custBirth.setText(c?.get(5))
            val custName = findViewById<TextView>(R.id.custName)
            custName.setText(c?.get(3))
            val custSex = findViewById<TextView>(R.id.custSex)
            custSex.setText(c?.get(4))
            val agreeDate = findViewById<TextView>(R.id.agreeDate)
            agreeDate.setText(c?.get(6))
            val agreeType = findViewById<TextView>(R.id.agreeType)
            agreeType.setText(c?.get(7))
            val cellNum = findViewById<TextView>(R.id.phoneNum)
            cellNum.setText(c?.get(8))
            val tellNum = findViewById<TextView>(R.id.callNum)
            tellNum.setText(c?.get(9))
        }

    }

    fun sqlDB(custkey: String){
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

                   sum = custnum + "|" + counStep+"|"+alocdate+"|"+custName+"|"+custSex+"|"+custBirth+"|"+agreeDate+"|"+agreeType+"|"+cellNum+"|"+tellNum

                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }

    }
}