package com.surpasslike.calendar.view.fragment

import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.surpasslike.calendar.R
import com.surpasslike.calendar.base.BaseFragment
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.databinding.FragmentScheduleBinding
import com.surpasslike.calendar.utils.RepeatRule
import com.blankj.utilcode.util.ToastUtils
import com.surpasslike.calendar.view.dialog.ConfirmDialog
import com.surpasslike.calendar.view.dialog.DateTimePickerDialog
import com.surpasslike.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(FragmentScheduleBinding::inflate) {

    private val mViewModel: CalendarViewModel by activityViewModels()

    // 编辑模式下的日程数据
    private var editingSchedule: ScheduleEntity? = null

    // 开始日期+时间
    private var startDateMillis = 0L
    private var startHour = 9
    private var startMinute = 0

    // 结束日期+时间
    private var endDateMillis = 0L
    private var endHour = 10
    private var endMinute = 0

    // 重复规则映射: Spinner index → RepeatRule?
    private val repeatRules =
        arrayOf(null, RepeatRule.DAILY, RepeatRule.WEEKLY, RepeatRule.MONTHLY, RepeatRule.YEARLY)

    // 提醒映射: Spinner index → minutes?
    private val reminderMinutes = arrayOf(null, 5, 15, 30, 60)

    override fun initView() {
        val scheduleId = arguments?.getLong(ARG_SCHEDULE_ID, -1L) ?: -1L
        val dateMillis = arguments?.getLong(ARG_DATE, -1L) ?: -1L
        LogUtils.d(TAG, "initView: scheduleId=$scheduleId, dateMillis=$dateMillis")

        // 初始化开始/结束日期为传入的日期
        startDateMillis = dateMillis
        endDateMillis = dateMillis

        setupSpinners()
        setupAllDaySwitch()
        setupDateTimePickers()
        setupSaveButton()
        setupDeleteButton()

        if (scheduleId > 0) {
            // 编辑模式: 加载日程
            LogUtils.d(TAG, "编辑模式: 加载日程 id=$scheduleId")
            loadSchedule(scheduleId)
        } else {
            // 新增模式
            LogUtils.d(TAG, "新增模式: dateMillis=$dateMillis")
            displayStartDateTime()
            displayEndDateTime()
            mBinding.rbNormal.isChecked = true
        }
    }

    /*
     * 初始化重复规则和提醒两个 Spinner 的适配器
     * 数据源来自 strings.xml 中的 repeat_options 和 reminder_options 数组
     */
    private fun setupSpinners() {
        // 重复规则 Spinner
        val repeatLabels = resources.getStringArray(R.array.repeat_options)
        mBinding.spinnerRepeat.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, repeatLabels
        )

        // 提醒 Spinner
        val reminderLabels = resources.getStringArray(R.array.reminder_options)
        mBinding.spinnerReminder.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, reminderLabels
        )
    }

    /*
     * 设置全天开关的监听
     * 打开时隐藏开始/结束时间行, 关闭时显示
     */
    private fun setupAllDaySwitch() {
        mBinding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.GONE else View.VISIBLE
            mBinding.layoutStartTime.visibility = visibility
            mBinding.layoutEndTime.visibility = visibility
        }
    }

    /*
     * 设置开始时间和结束时间的点击事件
     * 点击后弹出 DateTimePickerDialog 选择日期+时间
     * 选完开始时间后, 结束时间自动同步为相同日期和时间
     * 选结束时间时, 如果早于开始时间则弹 Toast 提示并拒绝
     */
    private fun setupDateTimePickers() {
        mBinding.tvStartTime.setOnClickListener {
            val dialog = DateTimePickerDialog.newInstance(
                title = getString(R.string.pick_start_time),
                dateMillis = startDateMillis,
                hour = startHour,
                minute = startMinute
            )
            dialog.setOnDateTimePickedListener { dateMillis, hour, minute ->
                startDateMillis = dateMillis
                startHour = hour
                startMinute = minute
                displayStartDateTime()
                // 选完开始时间后,结束时间同步为相同日期和时间
                endDateMillis = dateMillis
                endHour = hour
                endMinute = minute
                displayEndDateTime()
            }
            dialog.show(childFragmentManager, "start_datetime_picker")
        }

        mBinding.tvEndTime.setOnClickListener {
            val dialog = DateTimePickerDialog.newInstance(
                title = getString(R.string.pick_end_time),
                dateMillis = endDateMillis,
                hour = endHour,
                minute = endMinute
            )
            dialog.setOnDateTimePickedListener { dateMillis, hour, minute ->
                // 校验: 结束时间不能早于开始时间
                val newEndMillis = dateMillisWithTime(dateMillis, hour, minute)
                val startMillis = dateMillisWithTime(startDateMillis, startHour, startMinute)
                if (newEndMillis < startMillis) {
                    ToastUtils.showShort("结束时间不能早于开始时间")
                    return@setOnDateTimePickedListener
                }
                endDateMillis = dateMillis
                endHour = hour
                endMinute = minute
                displayEndDateTime()
            }
            dialog.show(childFragmentManager, "end_datetime_picker")
        }
    }

    /*
     * 设置保存按钮的点击事件
     * 收集表单中所有字段, 校验标题非空后, 根据是否有 editingSchedule 决定新增或更新
     * 保存完成后返回上一页
     */
    private fun setupSaveButton() {
        mBinding.btnSave.setOnClickListener {
            val title = mBinding.etTitle.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                mBinding.etTitle.error = "请输入标题"
                return@setOnClickListener
            }

            val description = mBinding.etDescription.text?.toString()?.trim()?.ifEmpty { null }
            val isAllDay = mBinding.switchAllDay.isChecked
            val repeatRule = repeatRules[mBinding.spinnerRepeat.selectedItemPosition]
            val reminder = reminderMinutes[mBinding.spinnerReminder.selectedItemPosition]
            val priority = when (mBinding.rgPriority.checkedRadioButtonId) {
                R.id.rb_important -> 2
                R.id.rb_moderate -> 1
                else -> 0
            }

            val startTimeMillis =
                if (!isAllDay) dateMillisWithTime(startDateMillis, startHour, startMinute) else null
            val endTimeMillis =
                if (!isAllDay) dateMillisWithTime(endDateMillis, endHour, endMinute) else null

            // date 字段取开始日期
            val date = startDateMillis

            val existing = editingSchedule
            if (existing != null) {
                // 更新
                LogUtils.d(TAG, "保存: 更新日程 id=${existing.id}, title=$title")
                mViewModel.updateSchedule(
                    existing.copy(
                        title = title,
                        description = description,
                        date = date,
                        isAllDay = isAllDay,
                        startTime = startTimeMillis,
                        endTime = endTimeMillis,
                        repeatRule = repeatRule,
                        reminderMinutes = reminder,
                        priority = priority,
                        updatedAtTime = System.currentTimeMillis()
                    )
                )
            } else {
                // 新增
                LogUtils.d(TAG, "保存: 新增日程 title=$title, date=$date")
                mViewModel.insertSchedule(
                    ScheduleEntity(
                        title = title,
                        description = description,
                        date = date,
                        isAllDay = isAllDay,
                        startTime = startTimeMillis,
                        endTime = endTimeMillis,
                        repeatRule = repeatRule,
                        reminderMinutes = reminder,
                        priority = priority
                    )
                )
            }
            parentFragmentManager.popBackStack()
        }
    }

    /*
     * 设置删除按钮的点击事件
     * 点击后弹出 ConfirmDialog 二次确认, 确认后删除日程并返回上一页
     */
    private fun setupDeleteButton() {
        mBinding.btnDelete.setOnClickListener {
            LogUtils.d(TAG, "点击删除按钮: id=${editingSchedule?.id}")
            ConfirmDialog(
                context = requireContext(),
                title = getString(R.string.dialog_delete_title),
                message = getString(R.string.dialog_delete_message),
                onConfirm = {
                    LogUtils.d(
                        TAG,
                        "确认删除: id=${editingSchedule?.id}, title=${editingSchedule?.title}"
                    )
                    editingSchedule?.let { mViewModel.deleteSchedule(it) }
                    parentFragmentManager.popBackStack()
                }).show()
        }
    }

    /*
     * 编辑模式下, 根据日程 id 从数据库加载日程数据并填充到表单
     * 包括: 标题、描述、全天开关、开始/结束日期时间、重复规则、优先级、提醒
     * 加载完成后显示删除按钮
     */
    private fun loadSchedule(id: Long) {
        LogUtils.d(TAG, "loadSchedule: id=$id")
        viewLifecycleOwner.lifecycleScope.launch {
            val schedule = mViewModel.getScheduleById(id) ?: run {
                LogUtils.d(TAG, "loadSchedule: 未找到日程 id=$id, 返回上一页")
                parentFragmentManager.popBackStack()
                return@launch
            }
            LogUtils.d(TAG, "loadSchedule: 加载成功, title=${schedule.title}")
            editingSchedule = schedule
            startDateMillis = schedule.date
            endDateMillis = schedule.date

            // 填充表单
            mBinding.etTitle.setText(schedule.title)
            mBinding.etDescription.setText(schedule.description.orEmpty())

            mBinding.switchAllDay.isChecked = schedule.isAllDay
            if (!schedule.isAllDay && schedule.startTime != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = schedule.startTime }
                startHour = cal.get(Calendar.HOUR_OF_DAY)
                startMinute = cal.get(Calendar.MINUTE)
                // 从 startTime 提取日期
                startDateMillis = Calendar.getInstance().apply {
                    timeInMillis = schedule.startTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            if (!schedule.isAllDay && schedule.endTime != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = schedule.endTime }
                endHour = cal.get(Calendar.HOUR_OF_DAY)
                endMinute = cal.get(Calendar.MINUTE)
                // 从 endTime 提取日期
                endDateMillis = Calendar.getInstance().apply {
                    timeInMillis = schedule.endTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            displayStartDateTime()
            displayEndDateTime()

            // 重复规则
            val repeatIndex = repeatRules.indexOf(schedule.repeatRule)
            if (repeatIndex >= 0) mBinding.spinnerRepeat.setSelection(repeatIndex)

            // 优先级
            when (schedule.priority) {
                2 -> mBinding.rbImportant.isChecked = true
                1 -> mBinding.rbModerate.isChecked = true
                else -> mBinding.rbNormal.isChecked = true
            }

            // 提醒
            val reminderIndex = reminderMinutes.indexOf(schedule.reminderMinutes)
            if (reminderIndex >= 0) mBinding.spinnerReminder.setSelection(reminderIndex)

            // 显示删除按钮
            mBinding.btnDelete.visibility = View.VISIBLE
        }
    }

    /*
     * 将当前的开始日期+时间格式化后显示到 tvStartTime
     * 显示格式: "2月25日 周三 09:00"
     */
    private fun displayStartDateTime() {
        mBinding.tvStartTime.text = formatDateTime(startDateMillis, startHour, startMinute)
    }

    /*
     * 将当前的结束日期+时间格式化后显示到 tvEndTime
     * 显示格式: "2月25日 周三 10:00"
     */
    private fun displayEndDateTime() {
        mBinding.tvEndTime.text = formatDateTime(endDateMillis, endHour, endMinute)
    }

    /*
     * 将日期毫秒+小时+分钟格式化为显示字符串
     * @param dateMillis 日期零点毫秒, 若 <= 0 则只显示时间
     * @return 格式如 "2026年2月25日 周三 09:00"
     */
    private fun formatDateTime(dateMillis: Long, hour: Int, minute: Int): String {
        if (dateMillis <= 0) return String.format("%02d:%02d", hour, minute)
        val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val year = cal.get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("M月d日", Locale.CHINA)
        val dateStr = dateFormat.format(cal.time)
        val dayOfWeek = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return "${year}年${dateStr} $dayOfWeek ${String.format("%02d:%02d", hour, minute)}"
    }

    /*
     * 将日期零点毫秒 + 小时分钟合成完整的时间戳
     * 例: 2月25日零点 + 9时30分 → 2月25日09:30的毫秒值
     * @param dateMillis 日期零点的毫秒时间戳
     * @param hour 小时(0-23)
     * @param minute 分钟(0-59)
     * @return 合成后的完整时间戳
     */
    private fun dateMillisWithTime(dateMillis: Long, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        const val ARG_SCHEDULE_ID = "arg_schedule_id"
        const val ARG_DATE = "arg_date"
    }
}
