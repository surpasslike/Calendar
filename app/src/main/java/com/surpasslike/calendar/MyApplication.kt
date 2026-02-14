package com.surpasslike.calendar

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.surpasslike.calendar.data.database.AppDatabase

class MyApplication : Application() {

    companion object {
        private const val TAG = "MyApplication"
    }

    // 整个APP只需要这一个数据库实例
    val appDatabase: AppDatabase by lazy { // by lazy懒加载让数据库只在需要时才创建
        LogUtils.d(TAG, "appDatabase: 创建数据库实例")
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 UtilCodeX
        Utils.init(this)
        LogUtils.d(TAG, "onCreate: Application 初始化完成")
    }
}
