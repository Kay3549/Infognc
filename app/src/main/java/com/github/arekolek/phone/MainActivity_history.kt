package com.github.arekolek.phone

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import kotlinx.android.synthetic.main.activity_main_history.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileOutputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class MainActivity_history : AppCompatActivity() {
    private val ip = "192.168.1.206"
    private val port = "1433"
    private val Classes = "net.sourceforge.jtds.jdbc.Driver"
    private val database = "smart_DB"
    private val username = "smart_TM"
    private val password = ".Digital"
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"
    private var connection: Connection? = null
    private var sum: String? = null
    //커스텀리스트뷰를 위해 추가
    private var CodeItem = ArrayList<String>()
    private var CodeName = ArrayList<String>()

    private var id:String = ""
    var path = ""
    val arrayList = ArrayList<String>()
    val audioPlay = MediaPlayer()
    var start = 0
    var play = 1

    private var agentNum = Data.retunagentNum()

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_history)

        // 액션바 실행
        var susin = findViewById<Button>(R.id.recv)
        susin.setOnClickListener{
            val intent = Intent(applicationContext, Logindetail::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

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

        var intent: Intent = intent

        var a = intent.getStringExtra("DB")
        Log.d("DB", "DB: " + a)
        var b = a?.split(",")
        Log.d("B", "B: " + b)
        var phnum = b?.get(1).toString()

        if (phnum != null) {
            sqlDB(phnum)
            var c = sum?.split("|")
            Log.d("DB", "sum: " + sum)
            Log.d("DB", "C: " + c)
            var custNum = findViewById<TextView>(R.id.custNum1)
            custNum.setText(c?.get(0))
            var custName = findViewById<TextView>(R.id.custName1)
            custName.setText(c?.get(1))
            var custSex = findViewById<TextView>(R.id.custSex1)
            custSex.setText(c?.get(2))
            var custBirth = findViewById<TextView>(R.id.custBirth1)
            custBirth.setText(c?.get(3))
            var recNum = findViewById<TextView>(R.id.recNum1)
            recNum.setText(c?.get(4))
            var startTime = findViewById<TextView>(R.id.startTime1)
            startTime.setText(c?.get(5))
            var endTime = findViewById<TextView>(R.id.endTime1)
            endTime.setText(c?.get(6))
            var callNum = findViewById<TextView>(R.id.callNum1)
            callNum.setText(c?.get(7))
            var counStep = findViewById<TextView>(R.id.counStep1)
            counStep.setText(c?.get(8))
            var contType = findViewById<TextView>(R.id.contType1)
            contType.setText(c?.get(9))
            var counMemo = findViewById<TextView>(R.id.counMemo1)
            counMemo.setText(c?.get(10))
        }

        path = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        //path = "/storage/emulated/0/Call"
        id = recNum1.text as String

        listen.setOnClickListener() {
            if (start == 0) {
                start = 1
                listen.text = "중지"

                if (play == 1) {
                    FileDownload()
                    File(path).walkBottomUp().forEach {
                        arrayList.add(it.toString())
                    }
                    val pf = arrayList[0]
                    audioPlay.setDataSource(pf)
                    audioPlay.prepare()
                    audioPlay.start()
                    play = 0
                } else {
                    if (!audioPlay.isPlaying) {
                        audioPlay.start()
                    }
                }

            } else {
                start = 0
                listen.text = "듣기"
                if (audioPlay.isPlaying) {
                    audioPlay.pause()
                }
            }
        }

        audioPlay.setOnCompletionListener {
            listen.text="듣기"
            val file = File(path)
            file.deleteRecursively()
        }

        listbtn.setOnClickListener{
            val intent = Intent(applicationContext, MainActivity_list::class.java)
            startActivity(intent)
            Data.reset()
            finish()
        }
    }

    fun sqlDB(phnum: String){
        //step()함수 사용
        step()
        if (connection != null) {
            var statement: Statement? = null
            try {
                statement = connection!!.createStatement()
                val sql =
                    "select coun.custNum,info.custName,info.custSex,info.custBirth,cal.recNum,cal.startTime,cal.endTime,cal.callNum,coun.counStep,coun.contType,coun.counMemo \n" +
                            "from counsel_list as coun \n" +
                            "inner join call_list as cal\n" +
                            "on coun.recNum = cal.recNum\n" +
                            "inner join customer_info as info\n" +
                            "on coun.custNum = info.custNum\n" +
                            "where coun.agentNum = '"+agentNum+"' and coun.custNum='"+phnum+"'"
                val resultSet = statement.executeQuery(sql) // DB
                Log.d("sql", "sql: " + sql)

                while (resultSet.next()) {
                    var counStep:String? = resultSet.getString(9)
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

                    var contType:String? = resultSet.getString(10)
                    Log.d("contType", "CONTTYPE" + contType)
                    contType = when(resultSet.getString(10)){
                        "00" -> "기본가입"
                        else -> "  "
                    }
                    var custSex = resultSet.getString(3)
                    custSex = when(resultSet.getString(3)){
                        "1" -> "남자"
                        "2" -> "여자"
                        else -> "  "
                    }

                    var custnum = resultSet.getString(1)
                    var custName = resultSet.getString(2)

                    var custBirth = resultSet.getString(4)
                    var recNum = resultSet.getString(5)
                    var startTime = resultSet.getString(6)
                    var endTime = resultSet.getString(7)
                    var callNum = resultSet.getString(8)

                    var counMemo = resultSet.getString(11)

                    sum = custnum + "|" + custName+"|"+custSex+"|"+custBirth+"|"+recNum+"|"+startTime+"|"+endTime+"|"+callNum+"|"+step+"|"+contType +"|"+counMemo
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Log.d("sqlDB", "Connection is null")
        }
    }

    // step()함수 추가
    fun step(){
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

    private fun FileDownload() {

        try {
            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.changeWorkingDirectory("/202102")
            con.enterLocalPassiveMode();
            con.setFileType(FTP.BINARY_FILE_TYPE);
            val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$id.m4a")
            val directory = File(path)

            if (!directory.exists()) {       // 원하는 경로에 폴더가 있는지 확인
                directory.mkdirs() // 하위폴더를 포함한 폴더를 전부 생성
            }

            var fos = FileOutputStream(file)
            con.retrieveFile("$id.m4a", fos)

            con.logout()
            con.disconnect()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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

