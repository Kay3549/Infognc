package com.github.arekolek.phone
import android.annotation.SuppressLint
import android.os.FileObserver
import android.util.Log
import java.io.File


private class FileObserver(root: String) :  FileObserver(File(path),mask) {

    var rootPath: String

    @SuppressLint("LogNotTimber")
    override fun onEvent(event: Int, path: String?) {
        when (event) {
            CREATE -> Log.d(TAG, "CREATE:$rootPath$path")
            DELETE -> Log.d(TAG, "DELETE:$rootPath$path")
            DELETE_SELF -> Log.d(TAG, "DELETE_SELF:$rootPath$path")
            MODIFY -> Log.d(TAG, "MODIFY:$rootPath$path")
            MOVED_FROM -> Log.d(TAG, "MOVED_FROM:$rootPath$path")
            MOVED_TO -> Log.d(TAG, "MOVED_TO:$path")
            MOVE_SELF -> Log.d(TAG, "MOVE_SELF:$path")
            else -> {
            }
        }
    }

    fun close() {
        super.finalize()
    }

    companion object {
        const val path = "/storage/emulated/0/Call"
        const val TAG = "FILEOBSERVER"
        const val mask = CREATE or
                DELETE or
                DELETE_SELF or
                MODIFY or
                MOVED_FROM or
                MOVED_TO or
                MOVE_SELF
    }

    init {

        var root = root
        if (!root.endsWith(File.separator)) {
            root += File.separator
        }
        rootPath = root
    }
}