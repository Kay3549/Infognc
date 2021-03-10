package com.github.arekolek.phone

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isInvisible
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.activity_logindetail.*
import kotlinx.android.synthetic.main.listview_item.*
import kotlinx.android.synthetic.main.wh_activity_main_click.*
import kotlinx.coroutines.*
import net.khirr.library.foreground.Foreground
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class WH_DialerActivity : AppCompatActivity() {

    private var isCalling = false
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
    private var idxCounDB = ""
    private var CodeItem = ArrayList<String>()
    private var CodeName = ArrayList<String>()
    private var phnum = ""
    private var sfName = "data"
    private var counstep = ""

    companion object {
        const val ROLE_REQUEST_CODE = 2002
        const val REQUEST_PERMISSION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wh_activity_main_click)

        // 액션바 실행
        var susin = findViewById<Button>(R.id.recv)

        var action1 = findViewById<Button>(R.id.coun)
        action1.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        var action2 = findViewById<Button>(R.id.history)
        action2.isEnabled = false

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

        var telephoneManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephoneManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        //스피너
        var counStepsp = findViewById<Spinner>(R.id.counStep)
        spinner()
        var spinnerName = CodeName
        Log.d("spinnerName" , spinnerName.toString())
        var spinneradapter:ArrayAdapter<String>
        spinneradapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerName)
        counStepsp.setAdapter(spinneradapter)

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
        var b = a?.split(",")
        Log.d("B", "B: " + b)
        phnum = b?.get(1).toString()

        if (phnum != null) {
            sqlDB(phnum)
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

            counstep = c?.get(1).toString()
            idxCounDB = c?.get(10).toString()
        }

        // 스피너 데이터 선택 유지
        if(counstep.equals("00")){counStepsp.setSelection(0)}
        else if(counstep.equals("01")){counStepsp.setSelection(1)}
        else if(counstep.equals("02")){counStepsp.setSelection(2)}
        else if(counstep.equals("03")){counStepsp.setSelection(3)}
        else if(counstep.equals("04")){counStepsp.setSelection(4)}
        else if(counstep.equals("05")){counStepsp.setSelection(5)}
        else if(counstep.equals("06")){counStepsp.setSelection(6)}
        else if(counstep.equals("10")){counStepsp.setSelection(7)}
        else if(counstep.equals("11")){counStepsp.setSelection(8)}
        else if(counstep.equals("20")){counStepsp.setSelection(9)}
        else if(counstep.equals("30")){counStepsp.setSelection(10)}

        setTextView()

        var insertbtn = findViewById<Button>(R.id.insertbtn)
        insertbtn.setOnClickListener {
            Log.d("click", "Click")
            Log.d("data", "data: " + formatted)
            Log.d("data", "아아아아아: " + idxCounDB)
            Toast.makeText(this, "상담 저장 완료", Toast.LENGTH_SHORT).show()

            insertDB()
            update()
            super.onBackPressed()
            remove()
            finish()
            var intent = Intent(this, MainActivity::class.java).addFlags(FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        call1.text = "통화"
        call2.text = "통화"
        call3.text = "통화"
    }

    private fun remove(){
        var sf:SharedPreferences  = getSharedPreferences(sfName, 0)
        var editor:SharedPreferences.Editor = sf.edit()
        editor.remove("formatted")
        editor.commit()
    }

    override fun onStart() {

        super.onStart()

        call1.setOnClickListener {

            number = phoneNum.text as String
            makeCall()
        }
        call2.setOnClickListener {

            number = callNum.text as String
            makeCall()
        }
        call3.setOnClickListener {

            number = dirctNum.text.toString()
            passdata(number)
            makeCall()
        }
    }

    private fun makeCall() {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            connectionCall()
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

    private fun connectionCall() = runBlocking {
        val call = Intent(ACTION_CALL, Uri.parse("tel:${number}"))
            launch {
                if (Foreground.isBackground()) {
                    Log.e("tag", Foreground.isBackground().toString())
                    delay(1100)
                    reCreateMainActivity()
                }
        }
        startActivity(call)
    }

    private fun setTextView() {

        var sf:SharedPreferences  = getSharedPreferences(sfName, 0)
        var fomet = sf.getString("formatted", "")
        if (fomet != null) {
            formatted = fomet
        }
        val memoView: TextView = findViewById(R.id.counMemo)
        val dirctNum: TextView = findViewById(R.id.dirctNum)
        val custNum:TextView      = findViewById(R.id.custNum)
        val custBirth:TextView    = findViewById(R.id.custBirth)
        val custName:TextView     = findViewById(R.id.custName)
        val custSex:TextView      = findViewById(R.id.custSex)
        val agreeDate:TextView    = findViewById(R.id.agreeDate)
        val agreeType:TextView    = findViewById(R.id.agreeType)
        val phoneNum:TextView     = findViewById(R.id.phoneNum)
        val callNum:TextView      = findViewById(R.id.callNum)
        val counStepsp:Spinner    = findViewById(R.id.counStep)

        var idx = intent.getStringExtra("idxCounDB")
        if (idx != null) {
            idxCounDB = idx
        }

        if (intent.hasExtra("dirctNum") && intent.hasExtra("phoneNum") && intent.hasExtra("callNum"))
        {
            dirctNum.text = intent.getStringExtra("dirctNum")
            memoView.text = intent.getStringExtra("memoView")
            custNum.text = intent.getStringExtra("custNum")
            custBirth.text = intent.getStringExtra("custBirth")
            custName.text = intent.getStringExtra("custName")
            custSex.text = intent.getStringExtra("custSex")
            agreeDate.text = intent.getStringExtra("agreeDate")
            agreeType.text = intent.getStringExtra("agreeType")
            phoneNum.text = intent.getStringExtra("phoneNum")
            callNum.text = intent.getStringExtra("callNum")
        }

        var spin = intent.getStringExtra("counStep")
        if(spin.equals("미접촉")){counStepsp.setSelection(0)}
        else if(spin.equals("거부")){counStepsp.setSelection(1)}
        else if(spin.equals("수신거부")){counStepsp.setSelection(2)}
        else if(spin.equals("결번")){counStepsp.setSelection(3)}
        else if(spin.equals("부재중")){counStepsp.setSelection(4)}
        else if(spin.equals("진행")){counStepsp.setSelection(5)}
        else if(spin.equals("예약")){counStepsp.setSelection(6)}
        else if(spin.equals("가입신청")){counStepsp.setSelection(7)}
        else if(spin.equals("보안요청")){counStepsp.setSelection(8)}
        else if(spin.equals("보완완료")){counStepsp.setSelection(9)}
        else if(spin.equals("가입완료")){counStepsp.setSelection(10)}
    }

    private fun reCreateMainActivity() {
        val launchIntent = Intent(this, WH_DialerActivity::class.java).addFlags(
            FLAG_ACTIVITY_CLEAR_TOP)

        var sf: SharedPreferences = getSharedPreferences(sfName, 0)
        var editor: SharedPreferences.Editor = sf.edit()
        editor.putString("formatted", formatted)
        editor.commit()

        val dirctNum:TextView     = findViewById(R.id.dirctNum)
        val memoView:TextView     = findViewById(R.id.counMemo)
        val custNum:TextView      = findViewById(R.id.custNum)
        val custBirth:TextView    = findViewById(R.id.custBirth)
        val custName:TextView     = findViewById(R.id.custName)
        val custSex:TextView      = findViewById(R.id.custSex)
        val agreeDate:TextView    = findViewById(R.id.agreeDate)
        val agreeType:TextView    = findViewById(R.id.agreeType)
        val phoneNum:TextView     = findViewById(R.id.phoneNum)
        val callNum:TextView      = findViewById(R.id.callNum)
        val counStepsp:Spinner    = findViewById(R.id.counStep)

        launchIntent.putExtra("dirctNum", dirctNum.text.toString())
        launchIntent.putExtra("memoView", memoView.text.toString())
        launchIntent.putExtra("custNum", custNum.text.toString())
        launchIntent.putExtra("custName", custName.text.toString())
        launchIntent.putExtra("custBirth", custBirth.text.toString())
        launchIntent.putExtra("custSex", custSex.text.toString())
        launchIntent.putExtra("agreeDate", agreeDate.text.toString())
        launchIntent.putExtra("agreeType", agreeType.text.toString())
        launchIntent.putExtra("phoneNum", phoneNum.text.toString())
        launchIntent.putExtra("callNum", callNum.text.toString())
        launchIntent.putExtra("counStep", counStepsp.selectedItem.toString())
        launchIntent.putExtra("idxCounDB", idxCounDB)

        startActivity(launchIntent)
    }

    fun sqlDB(phnum: String) {
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select db.custNum,db.counStep,db.alocdate,info.custName,info.custSex,info.custBirth,info.agreeDate,info.agreeType,info.cellNum,info.tellNum,db.idxCounDB \n" +
                            "from customer_db as db  left outer join customer_info as info \n" +
                            "on db.custnum = info.custnum\n" +
                            "where db.agentNum = '1' and db.custNum = '" + phnum + "'\n"
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
        var selcectstep = counStepsp.selectedItem.toString()
        Log.d("selcectstep", "selcectstep: " + selcectstep)
        var step = ""
        var codeitem = CodeItem
        var codename = CodeName
        for(i in codeitem.indices){
            var cd = codeitem.get(i)
            var cd2 = codename.get(i)
            Log.d("step", "cd: " + cd + " cd2: " + cd2)
            if(cd2 == selcectstep){
                step = cd
            }
        }
        var counMemo = findViewById<EditText>(R.id.counMemo)
        var memo = counMemo.text.toString()

        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "insert into counsel_list (agentNum,custNum,idxCounDB,custName,counStep,counMemo)values('1','" + num + "','" + idxCounDB + "', '" + name + "','" + step + "','" + memo + "')"
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
        var selcectstep = counStepsp.selectedItem.toString()
        var step = ""
        var codeitem = CodeItem
        var codename = CodeName
        for(i in codeitem.indices){
            var cd = codeitem.get(i)
            var cd2 = codename.get(i)
            Log.d("step", "cd: " + cd +" cd2: " + cd2)
            if(cd2 == selcectstep){
                step = cd
            }
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

    fun spinner(){
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
                    Log.d("code", "codeItem: " + codeItem + ", codeName: " + codeName)

                    CodeName.add(codeName)
                    CodeItem.add(codeItem)
                }
            } catch (e: SQLException){
                e.printStackTrace()
            }
        } else{
            Log.d("sqlDB", "Connection is null")
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            when(state){
                // 통화중 아님
                TelephonyManager.CALL_STATE_IDLE -> {
                    isCalling = false
                    insertbtn.visibility = View.VISIBLE
                    call1.visibility = View.VISIBLE
                    call2.visibility = View.VISIBLE
                    call3.visibility = View.VISIBLE
                }
                // 통화중
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    isCalling = true
                    insertbtn.visibility = View.GONE
                    call1.visibility = View.GONE
                    call2.visibility = View.GONE
                    call3.visibility = View.GONE
                }
                // 통화벨울림
                TelephonyManager.CALL_STATE_RINGING -> {
                    isCalling = true
                    insertbtn.visibility = View.GONE
                    call1.visibility = View.GONE
                    call2.visibility = View.GONE
                    call3.visibility = View.GONE
                }
            }
        }
    }
}
