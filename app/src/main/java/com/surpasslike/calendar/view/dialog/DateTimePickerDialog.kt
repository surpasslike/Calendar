package com.surpasslike.calendar.view.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.blankj.utilcode.util.LogUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.surpasslike.calendar.databinding.DialogDatetimePickerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 底部弹出的日期时间选择器
 *
 * 三列 NumberPicker: 日期(含周几) | 小时(0-23) | 分钟(0-59)
 * 日期范围: 今天前后各 180 天
 * 标题下方实时显示当前选中: 2026年2月25日周三 09:00
 */
class DateTimePickerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogDatetimePickerBinding? = null
    private val binding get() = _binding!!

    // 日期列表: 每项对应一个 Calendar 零点
    private val dateList = mutableListOf<Calendar>()

    // 日期显示文本(NumberPicker里的)
    private val dateLabels = mutableListOf<String>()

    private var onDateTimePicked: ((dateMillis: Long, hour: Int, minute: Int) -> Unit)? = null

    // 初始值
    private var initialDateMillis = 0L
    private var initialHour = 9
    private var initialMinute = 0
    private var title = ""

    companion object {
        private const val ARG_DATE_MILLIS = "arg_date_millis"
        private const val ARG_HOUR = "arg_hour"
        private const val ARG_MINUTE = "arg_minute"
        private const val ARG_TITLE = "arg_title"
        private const val TAG = "DateTimePickerDialog"
        private const val DATE_RANGE_DAYS = 180

        private val WEEK_DAYS = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

        /*
         * 创建 DateTimePickerDialog 实例
         * 通过 Bundle 传递初始日期时间参数, 避免构造函数传参在屏幕旋转时丢失
         * @param title 弹窗标题, 如"选择开始时间"
         * @param dateMillis 初始日期的零点毫秒时间戳
         * @param hour 初始小时(0-23)
         * @param minute 初始分钟(0-59)
         */
        fun newInstance(
            title: String,
            dateMillis: Long,
            hour: Int,
            minute: Int
        ): DateTimePickerDialog {
            return DateTimePickerDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putLong(ARG_DATE_MILLIS, dateMillis)
                    putInt(ARG_HOUR, hour)
                    putInt(ARG_MINUTE, minute)
                }
            }
        }
    }

    /*
     * 设置用户确认选择后的回调
     * @param listener 回调参数: dateMillis=选中日期零点毫秒, hour=小时, minute=分钟
     */
    fun setOnDateTimePickedListener(listener: (dateMillis: Long, hour: Int, minute: Int) -> Unit) {
        onDateTimePicked = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE, "")
            initialDateMillis = it.getLong(ARG_DATE_MILLIS, System.currentTimeMillis())
            initialHour = it.getInt(ARG_HOUR, 9)
            initialMinute = it.getInt(ARG_MINUTE, 0)
        }
        LogUtils.d(TAG, "onCreate: title=$title, initialDate=$initialDateMillis, initialTime=${String.format("%02d:%02d", initialHour, initialMinute)}")
        generateDateList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDatetimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPickerTitle.text = title

        setupDatePicker()
        setupHourPicker()
        setupMinutePicker()
        updateSubtitle()

        binding.btnConfirm.setOnClickListener {
            val selectedDate = dateList[binding.npDate.value]
            val dateMillis = selectedDate.timeInMillis
            val hour = binding.npHour.value
            val minute = binding.npMinute.value
            LogUtils.d(TAG, "确认选择: dateMillis=$dateMillis, time=${String.format("%02d:%02d", hour, minute)}")
            onDateTimePicked?.invoke(dateMillis, hour, minute)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            LogUtils.d(TAG, "取消选择")
            dismiss()
        }
    }

    /*
     * 生成日期列表, 以今天为中心, 前后各 DATE_RANGE_DAYS 天
     * 同时生成对应的显示文本, 如 "2月25日 周三", 今天则显示 "2月25日 今天"
     * 结果存入 dateList(Calendar对象) 和 dateLabels(显示文本)
     */
    private fun generateDateList() {
        val dateFormat = SimpleDateFormat("M月d日", Locale.CHINA)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startCal = today.clone() as Calendar
        startCal.add(Calendar.DAY_OF_YEAR, -DATE_RANGE_DAYS)

        for (i in 0 until DATE_RANGE_DAYS * 2 + 1) {
            val cal = startCal.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, i)
            dateList.add(cal)

            val dayOfWeek = WEEK_DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dateStr = dateFormat.format(cal.time)

            val label = if (cal.timeInMillis == today.timeInMillis) {
                "$dateStr 今天"
            } else {
                "$dateStr $dayOfWeek"
            }
            dateLabels.add(label)
        }
    }

    private val onValueChanged = NumberPicker.OnValueChangeListener { _, _, _ -> updateSubtitle() }

    /*
     * 初始化日期列的 NumberPicker
     * 设置显示文本为 dateLabels, 禁用循环滚动, 定位到初始日期
     */
    private fun setupDatePicker() {
        binding.npDate.apply {
            minValue = 0
            maxValue = dateLabels.size - 1
            displayedValues = dateLabels.toTypedArray()
            wrapSelectorWheel = false
            setOnValueChangedListener(onValueChanged)

            val initialCal = Calendar.getInstance().apply {
                timeInMillis = initialDateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val index = dateList.indexOfFirst { it.timeInMillis == initialCal.timeInMillis }
            value = if (index >= 0) index else DATE_RANGE_DAYS
        }
    }

    /*
     * 初始化小时列的 NumberPicker
     * 范围 0-23, 显示为两位数(如 "08"), 启用循环滚动
     */
    private fun setupHourPicker() {
        binding.npHour.apply {
            minValue = 0
            maxValue = 23
            displayedValues = (0..23).map { String.format("%02d", it) }.toTypedArray()
            wrapSelectorWheel = true
            setOnValueChangedListener(onValueChanged)
            value = initialHour
        }
    }

    /*
     * 初始化分钟列的 NumberPicker
     * 范围 0-59, 显示为两位数(如 "05"), 启用循环滚动
     */
    private fun setupMinutePicker() {
        binding.npMinute.apply {
            minValue = 0
            maxValue = 59
            displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()
            wrapSelectorWheel = true
            setOnValueChangedListener(onValueChanged)
            value = initialMinute
        }
    }

    /*
     * 实时更新标题下方的副标题文字
     * 每当用户滚动任一列时触发, 显示当前选中的完整日期时间
     * 格式: "2026年2月25日周三  09:00"
     */
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateSubtitle() {
        val cal = dateList[binding.npDate.value]
        val year = cal.get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("M月d日", Locale.CHINA)
        val dateStr = dateFormat.format(cal.time)
        val dayOfWeek = WEEK_DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val hour = binding.npHour.value
        val minute = binding.npMinute.value
        binding.tvPickerSubtitle.text = "${year}年${dateStr}${dayOfWeek}  ${String.format("%02d:%02d", hour, minute)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
