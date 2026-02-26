package com.surpasslike.calendar.view.fragment

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.surpasslike.calendar.R
import com.surpasslike.calendar.base.BaseFragment
import com.surpasslike.calendar.databinding.FragmentCalendarBinding
import com.surpasslike.calendar.view.adapter.ScheduleAdapter
import com.surpasslike.calendar.viewmodel.CalendarViewModel

class CalendarFragment : BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {

    private val mCalendarViewModel: CalendarViewModel by activityViewModels()
    private lateinit var mAdapter: ScheduleAdapter

    override fun initView() {
        LogUtils.d(TAG, "initView")
        // RecyclerView 设置
        mAdapter = ScheduleAdapter { schedule ->
            // 点击日程 → 跳转编辑页(传 scheduleId)
            LogUtils.d(TAG, "点击日程: id=${schedule.id}, title=${schedule.title}")
            navigateToSchedule(scheduleId = schedule.id)
        }
        mBinding.rvCalendar.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvCalendar.adapter = mAdapter

        // 初始化年月显示
        updateYearMonth(mBinding.calendarView.curYear, mBinding.calendarView.curMonth)

        // CalendarView 月份切换监听
        mBinding.calendarView.setOnMonthChangeListener { year, month ->
            updateYearMonth(year, month)
        }

        // CalendarView 日期选择监听
        mBinding.calendarView.setOnCalendarSelectListener(object :
            CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {}

            override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                calendar ?: return
                val dateMillis = calendarToMillis(calendar.year, calendar.month, calendar.day)
                LogUtils.d(TAG, "选择日期: ${calendar.year}-${calendar.month}-${calendar.day}, dateMillis=$dateMillis")
                mCalendarViewModel.selectDate(dateMillis)
                updateYearMonth(calendar.year, calendar.month)
            }
        })

        // FAB 点击 → 跳转添加页(传 selectedDate)
        mBinding.fabAdd.setOnClickListener {
            LogUtils.d(TAG, "点击FAB添加日程, selectedDate=${mCalendarViewModel.getSelectedDateMillis()}")
            navigateToSchedule(dateMillis = mCalendarViewModel.getSelectedDateMillis())
        }
    }

    override fun initObserve() {
        LogUtils.d(TAG, "initObserve")
        // 收集日程列表,自动跟随选中日期切换
        mCalendarViewModel.schedulesForSelectedDate.collectWithViewLife { list ->
            LogUtils.d(TAG, "收到日程列表更新: ${list.size} 条")
            mAdapter.submitList(list)
        }
    }

    private fun navigateToSchedule(scheduleId: Long = -1L, dateMillis: Long = -1L) {
        LogUtils.d(TAG, "navigateToSchedule: scheduleId=$scheduleId, dateMillis=$dateMillis")
        val fragment = ScheduleFragment().apply {
            arguments = Bundle().apply {
                if (scheduleId > 0) {
                    putLong(ScheduleFragment.ARG_SCHEDULE_ID, scheduleId)
                }
                if (dateMillis >= 0) {
                    putLong(ScheduleFragment.ARG_DATE, dateMillis)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /*
     * 更新顶部年月显示, 如 "2026年2月"
     */
    private fun updateYearMonth(year: Int, month: Int) {
        mBinding.tvYearMonth.text = "${year}年${month}月"
    }

    // 将年月日转成当天零点的毫秒时间戳
    private fun calendarToMillis(year: Int, month: Int, day: Int): Long {
        return java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month - 1) // Calendar.MONTH 是 0-based
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
