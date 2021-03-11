package com.github.arekolek.phone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permission {

    private var context:Context? = null
    private var activity:Activity? = null

    private val permissions = arrayOf<String>(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_NUMBERS
    )

    private var permissionList:List<String>? = null
    private var MULTIPLE_PERMISSIONS = 1023

    fun PermissionSupport(_activity: Activity, _context: Context){
        this.activity = _activity
        this.context = _context
    }

    // 허용 받아야할 권한이 남았는지 체크
    fun checkPermission():Boolean{
        var result:Int = 0
        permissionList = ArrayList<String>()

        for(pm in permissions){
            result = context?.let { ContextCompat.checkSelfPermission(it, pm) }!!
            if(result != PackageManager.PERMISSION_GRANTED){
                (permissionList as ArrayList<String>).add(pm)
            }
        }

        if(!(permissionList as ArrayList<String>).isEmpty()){
            return false
        }
        return true
    }

    // 권한 허용 요청
    fun requestPermission() {
        ActivityCompat.requestPermissions(activity!!, permissionList!!.toTypedArray(), MULTIPLE_PERMISSIONS)
    }

    // 권한요청에 대한 결과 처리
    fun permissionResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ):Boolean {
        if (requestCode == MULTIPLE_PERMISSIONS && (grantResults.size > 0))
        {
            for (i in grantResults.indices)
            {
                if (grantResults[i] == -1)
                {
                    return false
                }
            }
        }
        return true
    }
}

