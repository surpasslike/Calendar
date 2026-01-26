package com.surpasslike.calendar

import android.app.Application
import com.blankj.utilcode.util.Utils

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化 UtilCodeX
        Utils.init(this)
    }
}
