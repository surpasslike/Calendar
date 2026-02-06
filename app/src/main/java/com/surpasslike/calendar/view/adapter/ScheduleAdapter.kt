package com.surpasslike.calendar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.surpasslike.calendar.R
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.databinding.ItemScheduleBinding

class ScheduleAdapter(
    private val onItemClick: (ScheduleEntity) -> Unit
) : ListAdapter<ScheduleEntity, ScheduleAdapter.ScheduleViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleEntity) {
            val context = binding.root.context

            // 标题
            binding.tvTitle.text = schedule.title

            // 时间显示
            binding.tvTime.text = if (schedule.isAllDay) {
                context.getString(R.string.all_day)
            } else if (schedule.startTime != null && schedule.endTime != null) {
                val start = TimeUtils.millis2String(schedule.startTime, "HH:mm")
                val end = TimeUtils.millis2String(schedule.endTime, "HH:mm")
                "$start-$end"
            } else if (schedule.startTime != null) {
                TimeUtils.millis2String(schedule.startTime, "HH:mm")
            } else {
                ""
            }

            // 描述(可选)
            if (!schedule.description.isNullOrBlank()) {
                binding.tvDescription.text = schedule.description
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }

            // 优先级颜色条
            val colorRes = when (schedule.priority) {
                2 -> R.color.priority_important
                1 -> R.color.priority_moderate
                else -> R.color.priority_normal
            }
            binding.viewPriority.setBackgroundColor(
                ContextCompat.getColor(context, colorRes)
            )

            // 点击事件
            binding.root.setOnClickListener { onItemClick(schedule) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScheduleEntity>() {
            override fun areItemsTheSame(old: ScheduleEntity, new: ScheduleEntity): Boolean {
                return old.id == new.id
            }

            override fun areContentsTheSame(old: ScheduleEntity, new: ScheduleEntity): Boolean {
                return old == new
            }
        }
    }
}
