package com.surpasslike.calendar

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //调用 BarUtils 设置透明状态栏,在加载完布局后立即调用
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)

    }
}