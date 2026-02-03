package com.surpasslike.calendar.view.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle

import com.surpasslike.calendar.base.BaseFragment
import com.surpasslike.calendar.viewmodel.CalendarViewModel
import com.surpasslike.calendar.databinding.FragmentCalendarBinding

class CalendarFragment : BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {

    private val mCalendarViewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel

    }

    override fun initView() {
        mBinding.helloWor

    }

}