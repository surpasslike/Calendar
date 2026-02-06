package com.surpasslike.calendar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.surpasslike.calendar.data.dao.ScheduleDao
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.utils.RepeatRuleConverter

/*
* @Database: 数据库,管理整个数据库,提供各种DAO的获取方法
* entities = [ScheduleEntity::class]数据库里有哪些表
* version = 1数据库版本号
* exportSchema = false不导出数据库结构文件
* */
@Database(entities = [ScheduleEntity::class], version = 1, exportSchema = false)
@TypeConverters(RepeatRuleConverter::class) // 注册类型转换器, 让 Room 自动转换 RepeatRule ↔ String
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao // 通过它来存取数据,相当于一个业务窗口

    companion object {
        @Volatile // 一个人修改后,强制其他人看到修改的最新内容
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase { // 获取入口的方法
            return INSTANCE
                ?: synchronized(this) { // 锁synchronized(this),this指的是AppDatabase.Companion;同一时刻只有一个线程能进入synchronized块
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                    // buildDatabase(context).also { INSTANCE = it }等价于以下三步:
                    // val database = buildDatabase(context)  // 1. 创建数据库对象
                    // INSTANCE = database                     // 2. 把它赋值给 INSTANCE
                    // return database                         // 3. 返回这个数据库对象
                }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, // Application Context 的生命周期 = 整个 App 的生命周期
                AppDatabase::class.java, // Room 需要知道要创建哪个数据库类的实例
                "calendar_database" // 这个名字会成为存储在设备上的数据库文件名,文件路径通常是: `/data/data/包名/databases/calendar_database`
            )
                .fallbackToDestructiveMigration() // 升级数据库后会导致直接删除重建,后续需要修改
                .build()
        }
    }
}