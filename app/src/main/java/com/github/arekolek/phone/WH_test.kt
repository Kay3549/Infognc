package com.github.arekolek.phone

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_w_h_test.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WH_test : AppCompatActivity() {
    private var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val audioPlay = MediaPlayer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_w_h_test)
       // if (intent.extras!!.containsKey("data")) phoneNumber = intent.getStringExtra("data").toString()

        val path = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()

        val arrayList = ArrayList<String>()

        File("$path").walkBottomUp().forEach {
            arrayList.add(it.toString())
        }

        val pf = arrayList[0]
        audioPlay.setDataSource(pf)
        audioPlay.prepare()

        play.setOnClickListener {
            audioPlay.start();
        }

        Donwload.setOnClickListener(){
            //FileDownload()
        }

    }

    private fun FileDownload() {

        var k = phoneNumber.length
        var phoneNum: String? = ""
        phoneNum = if (k <= 15) {
            phoneNumber
        } else {
            var ran = IntRange(0, 14)
            var temp = phoneNumber.slice(ran)
            temp
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmSSS_$phoneNum")
        val formatted = current.format(formatter)
        val fileName = "$formatted.m4a"
        val file =
            File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName).toString()
        var fos : FileOutputStream ?= null



        try {
            var con = FTPClient()
            con.connect("192.168.1.206")
            con.login("administrator", ".Digital")
            con.enterLocalPassiveMode() // important!
            con.setFileType(FTP.BINARY_FILE_TYPE)

            fos = FileOutputStream(file)

//            val destination = con.changeWorkingDirectory("192.168.1.206").toString()
//            val daTa= file
//            val path: String = con.printWorkingDirectory()

            con.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE)
            con.setFileTransferMode(FTP.BINARY_FILE_TYPE)

            con.retrieveFile(fileName,fos)
            con.logout()
            con.disconnect()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}