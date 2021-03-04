package com.github.arekolek.phone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Logindetail : AppCompatActivity() {

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var count = ""
    private var data = ArrayList<String>()
    private var custName:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logindetail)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        if (id != null) {
            Log.e("TESTACTIVITY:", " 아이디:$id 패스워드:$pw")

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
        var name=findViewById<TextView>(R.id.custName)
        name.text = custName

        sqlDB2()
        var a = count?.split("|")
        Log.d("a","a : " +a)

        var countste=findViewById<TextView>(R.id.countstep)
        countste.text = a?.get(0)
        var toda=findViewById<TextView>(R.id.allcallser)
        toda.text = a?.get(1)
        var pastda=findViewById<TextView>(R.id.pastday)
        pastda.text = a?.get(2)

        var Noncontac = findViewById<TextView>(R.id.Noncontact)
        Noncontac.setText(a?.get(3))
        var Proceedin = findViewById<TextView>(R.id.Proceeding)
        Proceedin.setText(a?.get(4))
        var Subscriptio = findViewById<TextView>(R.id.Subscription)
        Subscriptio.setText(a?.get(5))

        var Contactrat = findViewById<TextView>(R.id.Contactrate)
        Contactrat.setText(a?.get(6))
    }

    fun sqlDB() {
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql = "select agentNum,agentID,part,level,status,password,agentName,regDate,lastDate,pwDate from account_list"

                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)
                while (resultSet.next()) {
                    //var str = count.toString() + "," + resultSet.getString(1) + "," + resultSet.getString(4) + ","  + "," + date?.get(0).toString()
                    custName = resultSet.getString(7)
                    Log.d("name", "name: " + custName)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    fun sqlDB2() {
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()


                var now = LocalDate.now()

                var Strnow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                var datenow = LocalDate.parse(Strnow, DateTimeFormatter.ISO_DATE)

                val sql2 =
                        "select counstep, count(case when alocdate>'" + datenow + "'  then 'today' end) as " +
                                "'today',count(case when alocdate<'" + datenow + "' then 'pastday' end) " +
                                "as'pastday' from customer_db where alocdate>'2021-01-27' group by counstep"


                val resultSet2 = statement.executeQuery(sql2)

                var sum1 = 0
                var sum2: Double = 0.0
                var sum3 = 0
                var sum4 = 0

                var sum5: Double = 0.0 //04의 갯수
                var sum6: Double = 0.0

                var sum11 = 0

                var sum12 = 0
                var sum13 = 0
                var sum14 = 0

                while (resultSet2.next()) {
                    //var date = resultSet2
                    var countstep = resultSet2.getString(1)
                    Log.d("name", "name: " + countstep)
                    var allcallser = resultSet2.getString(2)
                    sum1 = sum1 + allcallser.toInt()

                    Log.d("Contactrate", "Contact rate: " + sum13)

                    Log.d("sum1", "sum1 " + sum1)
                    var pastday = resultSet2.getString(3)
                    Log.d("name", "name: " + pastday)


                    Log.d("count", "count : " + count)
                    var Noncontact = resultSet2.getString(1)

                    if (countstep == "00") {
                        sum11 = sum11+ 1 + allcallser.toInt()
                        sum2 = sum2 + pastday.toInt()

                    }

                    if (countstep == "05") {
                        var sum9 = allcallser.toInt()
                        sum3 = sum3 + sum9
                    }

                    if (countstep == "10") {
                        var sum10 = allcallser.toInt() + pastday.toInt()
                        sum4 = sum4 + sum10

                    }

                    if (countstep == "04") {
                        sum5 = sum5 + allcallser.toInt()
                        sum14 = sum14 + 1

                    }

                    val sum7 = sum6.toInt().toString() + "%"

                    val sum8 = sum2.toInt()

                    if (countstep == "00"||countstep == "01"||countstep == "02"||countstep == "03"||countstep == "04"||countstep == "05"||
                        countstep == "06"||countstep == "10"||countstep == "11"||countstep == "20"||countstep == "30") {
                        sum12 = sum12+1
                    }


                    count = countstep + "|" + sum1 + "|" + pastday + "|" + sum8 + "|" + sum3 + "|" + sum4 + "|" + sum7

                    //sum12 = sum12 + allcallser.toInt()


                    //sum13 = sum13.toInt()
                    sum14 = sum14.toInt()
                    //var sum9 = allcallser.toInt()

                    sum6 = ((sum12 - (sum5 + sum11)) / sum12) * 100
                    sum13 = sum6.toInt()
                    Log.d("sum6", "sum6 : " + sum6)

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
            startActivity(intent)}
        //상담이력
        if (id == R.id.action_btn2) {
            val intent = Intent(applicationContext, MainActivity_list::class.java)
            startActivity(intent)
            finish()
            return true
        }
        // 수신
//        if (id == R.id.action_btn3) {
//            val intent = Intent(applicationContext, MainActivity::class.java)
//            startActivity(intent)}
        return super.onOptionsItemSelected(item)
    }
}