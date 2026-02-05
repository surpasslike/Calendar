package com.surpasslike.calendar

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.surpasslike.calendar.data.database.AppDatabase

class MyApplication : Application() {

    // 整个APP只需要这一个数据库实例
    val appDatabase: AppDatabase by lazy { // by lazy懒加载让数据库只在需要时才创建
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 UtilCodeX
        Utils.init(this)
    }
}
