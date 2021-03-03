package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.wh_activity_dialer.*
import kotlinx.android.synthetic.main.wh_activity_main_click.*
import timber.log.Timber
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WH_DialerActivity : AppCompatActivity() {

    private var updatesDisposable = Disposables.empty()
    private var callbutton = 0
    private var connect = 0
    private var number = ""
    private var formatted = ""

    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var sum: String? = null
    private var idxCounDB: String? = null

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
        val resultCode = 12345

    }

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

        var counStepsp = findViewById<Spinner>(R.id.counStep)
        counStepsp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                parent.getItemAtPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var contTypesp = findViewById<Spinner>(R.id.contType)
        contTypesp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                parent.getItemAtPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        var intent: Intent = intent

        var a = intent.getStringExtra("DB")
        Log.d("DB", "DB: " + a)
        var b = a?.split(" | ")
        Log.d("B", "B: " + b)

        var custkey = b?.get(0)
        if (custkey != null) {
            sqlDB(custkey)
            var c = sum?.split("|")
            Log.d("DB", "C: " + c)
            var custNum = findViewById<TextView>(R.id.custNum)
            custNum.setText(c?.get(0))
            var custBirth = findViewById<TextView>(R.id.custBirth)
            custBirth.setText(c?.get(5))
            var custName = findViewById<TextView>(R.id.custName)
            custName.setText(c?.get(3))
            var custSex = findViewById<TextView>(R.id.custSex)
            custSex.setText(c?.get(4))
            var agreeDate = findViewById<TextView>(R.id.agreeDate)
            agreeDate.setText(c?.get(6))
            var agreeType = findViewById<TextView>(R.id.agreeType)
            agreeType.setText(c?.get(7))
            var phoneNum = findViewById<TextView>(R.id.phoneNum)
            phoneNum.setText(c?.get(8))
            var callNum = findViewById<TextView>(R.id.callNum)
            callNum.setText(c?.get(9))
        }

        var insertbtn = findViewById<Button>(R.id.insertbtn)
        insertbtn.setOnClickListener {
            Log.d("click", "Click")
            insertDB()
            update()
            super.onBackPressed()
            finish()
        }

        call1.text = "통화"
        call2.text = "통화"
        call3.text = "통화"
    }

    override fun onStart() {

        super.onStart()

        call1.setOnClickListener {

            number = phoneNum.text as String
            passdata(number)
            makeCall(phoneNum.text as String)

//            if (connect == 0) {
//                call2.visibility = View.GONE
//                call3.visibility = View.GONE
//                callbutton = 1
//                makeCall(phoneNum.text as String)
//                number = phoneNum.text as String
//                call1.text = "끊기"
//                connect = 1
//
//            } else {
//                CallManager.cancelCall()
//                connect = 0
//                call2.visibility = View.VISIBLE
//                call3.visibility = View.VISIBLE
//                call1.text = "통화"
//            }

        }
        call2.setOnClickListener {

            number = callNum.text as String
            passdata(number)
            makeCall(callNum.text as String)
//            if (connect == 0) {
//                call1.visibility = View.GONE
//                call3.visibility = View.GONE
//                makeCall(callNum.text as String)
//                callbutton = 2
//                number = callNum.text as String
//                call2.text = "끊기"
//                connect = 1
//            } else {
//                CallManager.cancelCall()
//                call1.visibility = View.VISIBLE
//                call3.visibility = View.VISIBLE
//                connect = 0
//                call2.text = "통화"
//            }

        }
        call3.setOnClickListener {

            number = dirctNum.text.toString()
            passdata(number)
            makeCall(dirctNum.text.toString())

//            if (connect == 0) {
//                call1.visibility = View.GONE
//                call2.visibility = View.GONE
//                makeCall(dirctNum.text.toString())
//                callbutton = 3
//                number = dirctNum.text.toString()
//                call3.text = "끊기"
//                connect = 1
//            } else {
//                CallManager.cancelCall()
//                call2.visibility = View.VISIBLE
//                call1.visibility = View.VISIBLE
//                connect = 0
//                call3.text = "통화"
//            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        updatesDisposable = CallManager.updates()
//
//            .doOnEach { Timber.e("$it") }
//            .doOnError { Timber.e("Error processing call") }
//            .subscribe { updateView(it) }
//    }

//    private fun updateView(gsmCall: GsmCall) {
//
//        if (gsmCall.status == GsmCall.Status.DIALING) {
//            passdata(number)
//        }
//    }


    private fun makeCall(number: String) {

        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val uri = "tel:${number}".toUri()
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    private fun passdata(number: String) {

        var k = number.length
        var phoneNum: String? = ""
        phoneNum = if (k <= 15) {
            number
        } else {
            var ran = IntRange(0, 14)
            var temp = number.slice(ran)
            temp
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS_$phoneNum")
        formatted = current.format(formatter) // 녹취키

        Data.setdata(formatted)


    }

    fun sqlDB(custkey: String) {
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName,info.custSex,info.custBirth,info.agreeDate,info.agreeType,info.cellNum,info.tellNum,db.idxCounDB \n" +
                            "from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n" +
                            "where db.agentNum = '1' and db.custNum = '" + custkey + "'\n"
                val resultSet = statement.executeQuery(sql) // DB




                while (resultSet.next()) {
                    var custSex = resultSet.getString(5)
                    custSex = when (resultSet.getString(5)) {
                        "0" -> "여자"
                        "1" -> "남자"
                        else -> "  "
                    }
                    var custnum = resultSet.getString(1)
                    var counStep = resultSet.getString(2)
                    var alocdate = resultSet.getString(3)
                    var custName = resultSet.getString(4)

                    var custBirth = resultSet.getString(6)
                    var agreeDate = resultSet.getString(7)
                    var agreeType = resultSet.getString(8)
                    var cellNum = resultSet.getString(9)
                    var tellNum = resultSet.getString(10)
                    idxCounDB = resultSet.getString(11)

                    sum =
                        custnum + "|" + counStep + "|" + alocdate + "|" + custName + "|" + custSex + "|" + custBirth + "|" + agreeDate + "|" + agreeType + "|" + cellNum + "|" + tellNum + "|" + idxCounDB

                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    fun insertDB() {

        var custNum = findViewById<TextView>(R.id.custNum)
        var num = custNum.text.toString()
        var custName = findViewById<TextView>(R.id.custName)
        var name = custName.text.toString()
        var counStepsp = findViewById<Spinner>(R.id.counStep)
        var coun = counStepsp.selectedItem.toString()
        var step = when (coun) {
            "미접촉" -> "00"
            "거부" -> "01"
            "수신거부" -> "02"
            "결번" -> "03"
            "부재중" -> "04"
            "진행" -> "05"
            "예약" -> "06"
            "가입완료" -> "30"
            else -> "   "
        }
        var counMemo = findViewById<EditText>(R.id.counMemo)
        var memo = counMemo.text.toString()


        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "insert into counsel_list (agentNum,recNum,custNum,idxCounDB,custName,counStep,counMemo)values('1','$formatted','" + num + "','" + idxCounDB + " ', '" + name + "','" + step + "','" + memo + "')"
                Log.d("sql", "SQL: " + sql)

                statement.executeQuery(sql) // DB

                Log.d("insertDB", "insert")

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    fun update() {
        var custNum = findViewById<TextView>(R.id.custNum)
        var num = custNum.text.toString()
        var counStepsp = findViewById<Spinner>(R.id.counStep)
        var coun = counStepsp.selectedItem.toString()
        var step = when (coun) {
            "미접촉   " -> "00"
            "거부     " -> "01"
            "수신거부" -> "02"
            "결번     " -> "03"
            "부재중   " -> "04"
            "진행     " -> "05"
            "예약     " -> "06"
            "가입완료" -> "30"
            else -> "   "
        }

        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "update customer_db set counStep='" + step + "' where custNum='" + num + "' and agentNum='1'"
                Log.d("sql", "SQL: " + sql)

                statement.executeQuery(sql) // DB

                Log.d("insertDB", "insert")

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
        //이력
        if (id == R.id.action_btn2) {
            val intent = Intent(applicationContext, MainActivity_list::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
