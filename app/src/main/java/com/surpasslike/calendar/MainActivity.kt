package com.surpasslike.calendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.surpasslike.calendar.databinding.ActivityMainBinding
import com.surpasslike.calendar.view.fragment.CalendarFragment

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 设置沉浸式状态栏 (透明 + 内容延伸至状态栏底部),如果上方有控件,记得加paddingTop,否则内容会和系统时间、电池图标重叠
        // 这样日历背景图可以铺满整个屏幕顶部
        BarUtils.transparentStatusBar(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(mBinding.fragmentContainer.id, CalendarFragment()).commit()
        }
    }
}