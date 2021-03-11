package com.github.arekolek.phone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
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
    private var array = ArrayList<String>()
    private var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logindetail)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        if (id != null) {
            Log.e("TESTACTIVITY:", " 아이디:$id 패스워드:$pw")

        }

        // 본인휴대폰 가져오기
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                1
            )
        }
        //사용자 핸드폰 번호 가져오는 구간
        val msg = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var PhoneNum = msg.line1Number
        Data.setagentPhone(PhoneNum)
        if (PhoneNum.startsWith("+82")) {
            PhoneNum = PhoneNum.replace("+82", "0")
            //Data.userPhoneNum = PhoneNum
            Data.setagentPhone(PhoneNum)
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

        var agentName = Data.retunagentName()
        var name = findViewById<TextView>(R.id.custName)
        name.setText(agentName)

        sqlDB2()

        Log.d("ARRAY", "array: " + array.toString())

        var a = count?.split("|")
        Log.d("a", "a : " + a)

        var toda = findViewById<TextView>(R.id.allcallser)
        toda.setText(a?.get(1))
        var pastda = findViewById<TextView>(R.id.pastday)
        pastda.setText(a?.get(2))

        var Noncontac = findViewById<TextView>(R.id.Noncontact)
        Noncontac.setText(a?.get(3))
        var Proceedin = findViewById<TextView>(R.id.Proceeding)
        Proceedin.setText(a?.get(4))
        var Subscriptio = findViewById<TextView>(R.id.Subscription)
        Subscriptio.setText(a?.get(5))

        var Contactrat = findViewById<TextView>(R.id.Contactrate)
        Contactrat.setText(a?.get(6))


        // 액션바 실행
        var susin = findViewById<Button>(R.id.recv)
        susin.isInvisible=true

        var action1 = findViewById<Button>(R.id.coun)
        action1.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
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

    fun sqlDB() {

        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql = "select agentNum,agentID,part,level,status,password,agentName,regDate,lastDate,pwDate from account_list"

                val resultSet = statement.executeQuery(sql) // DB

                Log.d("list", "list: " + sql)
                while (resultSet.next()) {
                    Data.setagentNum(resultSet.getString(1))
                    Data.setagentID(resultSet.getString(2))
                    Data.setpart(resultSet.getString(3))
                    Data.setlevel(resultSet.getString(4))
                    Data.setagentName(resultSet.getString(7))
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

            var agentNum = Data.retunagentNum()

            try {
                statement = connection!!.createStatement()

                var now = LocalDate.now()

                var Strnow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                var datenow = LocalDate.parse(Strnow, DateTimeFormatter.ISO_DATE)

                val sql2 =
                    "select counstep, count(case when alocdate>'" + datenow + "'  then 'today' end) as " +
                            "'today',count(case when alocdate<'" + datenow + "' then 'pastday' end) " +
                            "as'pastday' from customer_db where agentNum = '"+agentNum+"' group by counstep"

                Log.e("sql2",sql2)

                val resultSet2 = statement.executeQuery(sql2)

                var sum1 = 0
                var sum3 = 0
                var sum4 = 0
                var sum5: Double = 0.0 //04의 갯수
                var sum6: Double = 0.0
                var sum11=0
                var sum12 = 0
                var sum13 = 0
                var sum15=0
                var sum16=0
                var sum17 =0
                var sum19=0


                var sum20=0

                while (resultSet2.next()) {
                    var countstep = resultSet2.getString(1)
                    Log.d("name", "name: " + countstep)

                    var str = resultSet2.getString(1)+ "," + resultSet2.getString(2) + "," + resultSet2.getString(3)
                    array.add(str)
                    if (countstep == "00"||countstep == "01"||countstep == "02"||countstep == "03"||countstep == "04"||countstep == "05"||
                        countstep == "06"||countstep == "10"||countstep == "11"||countstep == "20"||countstep == "30") {
                    }


                    var allcallser = resultSet2.getString(2)
                    sum1 = sum1 + allcallser.toInt()


                    sum17=  allcallser.toInt()
                    Log.d("sum17", "sum17--- : " + sum17)

                    Log.d("Contactrate", "Contact rate: " + sum13)

                    Log.d("sum1", "sum1 " + sum1)
                    var pastday = resultSet2.getString(3)
                    var today = resultSet2.getString(2)
                    Log.d("today", "today: " + today)
                    Log.d("pastday", "pastday: " + pastday)

                    Log.d("count", "count : " + count)

                    if (today!=null&&countstep == "00"||countstep == "01"||countstep == "02"||countstep == "03"||countstep == "04"||countstep == "05"||
                        countstep == "06"||countstep == "10"||countstep == "11"||countstep == "20"||countstep == "30") {
                        if(today != "0") {
                            sum12 = sum12 + today.toInt()
                        } //sum12 총
                    }

                    if (today != null && countstep == "00") {
                        Log.d("today","today = null")
                        if(today != "0"){

                            sum11=sum11+today.toInt() //
//                            sum11= sum11 + 1
                        }
                        //sum11 = sum11 + allcallser.toInt()

                    }//sum11 미접촉

                    if (today!=null&&countstep == "05") {
                        if(today != "0") {
                            var sum9 = allcallser.toInt()
                            sum3 = sum3 + sum9
                        }
                    }//sum3 진행중

                    sum16=sum16+pastday.toInt()

                    if (today!=null&&countstep == "10") {
                        if(today != "0") {
                            var sum10 = allcallser.toInt() + pastday.toInt()
                            sum4 = sum4 + sum10
                        }
                    }

                    if (today!=null&&countstep == "04") {
                        if(today != "0") {
                            sum5 = sum5 + allcallser.toInt()
                        }
                    }
                    if(pastday!=null&&countstep == "00"||countstep == "04"||countstep == "05"||countstep == "06"||countstep == "10"||countstep == "11"||countstep == "20")
                    {
                        if(pastday != "0") {
                            sum19 = sum19 + pastday.toInt() //
                        }//sum19 db미접촉
                    }
                    if(today!=null&&countstep == "04"||countstep == "05"||countstep == "06"||countstep == "10"||countstep == "20")
                    {
                        if(today != "0") {
                            sum15 =sum15 + today.toInt()
//                            sum15 = sum15 + 1
                        }//sum15 가입건
                    }

                    if(today!=null&&countstep == "00"|| countstep == "04"||countstep == "05"||countstep == "06"||countstep == "10"||countstep == "11"||countstep == "20")
                    {//접촉률 01 02 03 30 을 제외한 나머지.
                        if(today != "0") {
                            sum20 = sum20 + today.toInt()
                        }
                    }

                    sum6 = (((sum12.toDouble()- sum20.toDouble()) / sum12.toDouble()) * 100)
                    sum13 = sum6.toInt()
                    //sum13 접촉률

                    Log.d("sum12", "sum12--- : " + sum12)
                    Log.d("sum20", "sum20--- : " + sum20)
                    count =countstep + "|" + sum12 +"|" + sum19+ "|" + sum11+ "|" + sum3 + "|" + sum15 + "|" + sum13
                    //1번째 countstep 2번째 총 3번쨰 지난 db 4번째 미접촉 5번째 진행중 6번째 가입건 7번째 접촉률
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }
}