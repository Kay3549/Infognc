package com.github.arekolek.phone

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_w_h_test.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileOutputStream

class WH_test : AppCompatActivity() {

    //private var phoneNumber = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        val path = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        val arrayList = ArrayList<String>()
        val audioPlay = MediaPlayer()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_w_h_test)
        //if (intent.extras!!.containsKey("data")) phoneNumber = intent.getStringExtra("data").toString()



        play.setOnClickListener {
            audioPlay.start();
        }

        Donwload.setOnClickListener() {
            FileDownload() // 파일 다운로드
            File("$path").walkBottomUp().forEach {
                arrayList.add(it.toString())
            }
            val pf = arrayList[0]
            audioPlay.setDataSource(pf)
            audioPlay.prepare()
            audioPlay.start()

        }
    }

    private fun FileDownload() {

        try {
            Toast.makeText(this, "다운로드 들어옴.", Toast.LENGTH_SHORT).show()
            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.changeWorkingDirectory("/202101")
            con.enterLocalPassiveMode();
            con.setFileType(FTP.BINARY_FILE_TYPE);
            Toast.makeText(this, "다운로드 들어옴.1", Toast.LENGTH_SHORT).show()
            val file = File(
                this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "202102011838862_01050992547.m4a"
            )

            var fos = FileOutputStream(file)

//            val destination = con.changeWorkingDirectory("192.168.1.206").toString()
//            val daTa= file
//            val path: String = con.printWorkingDirectory()


            Toast.makeText(this, "다운로드 들어옴.2", Toast.LENGTH_SHORT).show()

            con.retrieveFile("202102011838862_01050992547.m4a", fos)

            con.logout()
            con.disconnect()
            Toast.makeText(this, "다운로드 들어옴.3", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}