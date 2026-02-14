package com.surpasslike.calendar.view.fragment

import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.surpasslike.calendar.R
import com.surpasslike.calendar.base.BaseFragment
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.databinding.FragmentScheduleBinding
import com.surpasslike.calendar.utils.RepeatRule
import com.surpasslike.calendar.view.dialog.ConfirmDialog
import com.surpasslike.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(FragmentScheduleBinding::inflate) {

    private val mViewModel: CalendarViewModel by activityViewModels()

    // 编辑模式下的日程数据
    private var editingSchedule: ScheduleEntity? = null

    // 当前选择的开始/结束时间(小时和分钟)
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 10
    private var endMinute = 0

    // 当前日期毫秒
    private var dateMillis = 0L

    // 重复规则映射: Spinner index → RepeatRule?
    private val repeatRules =
        arrayOf(null, RepeatRule.DAILY, RepeatRule.WEEKLY, RepeatRule.MONTHLY, RepeatRule.YEARLY)

    // 提醒映射: Spinner index → minutes?
    private val reminderMinutes = arrayOf(null, 5, 15, 30, 60)

    override fun initView() {
        val scheduleId = arguments?.getLong(ARG_SCHEDULE_ID, -1L) ?: -1L
        dateMillis = arguments?.getLong(ARG_DATE, -1L) ?: -1L
        LogUtils.d(TAG, "initView: scheduleId=$scheduleId, dateMillis=$dateMillis")

        setupSpinners()
        setupAllDaySwitch()
        setupDatePicker()
        setupTimePickers()
        setupSaveButton()
        setupDeleteButton()

        if (scheduleId > 0) {
            // 编辑模式: 加载日程
            LogUtils.d(TAG, "编辑模式: 加载日程 id=$scheduleId")
            loadSchedule(scheduleId)
        } else {
            // 新增模式
            LogUtils.d(TAG, "新增模式: dateMillis=$dateMillis")
            displayDate()
            mBinding.rbNormal.isChecked = true
        }
    }

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

    private fun setupDatePicker() {
        mBinding.tvDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(dateMillis)
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                // MaterialDatePicker 返回的是 UTC 零点毫秒,转成本地零点
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selection
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateMillis = cal.timeInMillis
                displayDate()
            }
            picker.show(childFragmentManager, "date_picker")
        }
    }

    private fun setupAllDaySwitch() {
        mBinding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.GONE else View.VISIBLE
            mBinding.layoutStartTime.visibility = visibility
            mBinding.layoutEndTime.visibility = visibility
        }
    }

    private fun setupTimePickers() {
        mBinding.tvStartTime.setOnClickListener {
            showTimePicker(getString(R.string.pick_start_time), startHour, startMinute) { h, m ->
                startHour = h
                startMinute = m
                mBinding.tvStartTime.text = formatTime(h, m)
            }
        }
        mBinding.tvEndTime.setOnClickListener {
            showTimePicker(getString(R.string.pick_end_time), endHour, endMinute) { h, m ->
                endHour = h
                endMinute = m
                mBinding.tvEndTime.text = formatTime(h, m)
            }
        }
    }

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
                if (!isAllDay) dateMillisWithTime(dateMillis, startHour, startMinute) else null
            val endTimeMillis =
                if (!isAllDay) dateMillisWithTime(dateMillis, endHour, endMinute) else null

            val existing = editingSchedule
            if (existing != null) {
                // 更新
                LogUtils.d(TAG, "保存: 更新日程 id=${existing.id}, title=$title")
                mViewModel.updateSchedule(
                    existing.copy(
                        title = title,
                        description = description,
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
                LogUtils.d(TAG, "保存: 新增日程 title=$title, date=$dateMillis")
                mViewModel.insertSchedule(
                    ScheduleEntity(
                        title = title,
                        description = description,
                        date = dateMillis,
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
            dateMillis = schedule.date

            // 填充表单
            mBinding.etTitle.setText(schedule.title)
            mBinding.etDescription.setText(schedule.description.orEmpty())
            displayDate()

            mBinding.switchAllDay.isChecked = schedule.isAllDay
            if (!schedule.isAllDay && schedule.startTime != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = schedule.startTime }
                startHour = cal.get(Calendar.HOUR_OF_DAY)
                startMinute = cal.get(Calendar.MINUTE)
                mBinding.tvStartTime.text = formatTime(startHour, startMinute)
            }
            if (!schedule.isAllDay && schedule.endTime != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = schedule.endTime }
                endHour = cal.get(Calendar.HOUR_OF_DAY)
                endMinute = cal.get(Calendar.MINUTE)
                mBinding.tvEndTime.text = formatTime(endHour, endMinute)
            }

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

    private fun displayDate() {
        if (dateMillis > 0) {
            mBinding.tvDate.text = TimeUtils.millis2String(dateMillis, "yyyy-MM-dd")
        }
    }

    private fun showTimePicker(
        title: String, hour: Int, minute: Int, onPicked: (Int, Int) -> Unit
    ) {
        val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour)
            .setMinute(minute).setTitleText(title).build()
        picker.addOnPositiveButtonClickListener {
            onPicked(picker.hour, picker.minute)
        }
        picker.show(childFragmentManager, "time_picker")
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    // 将日期零点毫秒 + 小时分钟合成完整时间戳
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
