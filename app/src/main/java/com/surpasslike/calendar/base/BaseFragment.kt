package com.surpasslike.calendar.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

typealias Inflate<VB> = (LayoutInflater, ViewGroup?, Boolean) -> VB

abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    private var _binding: VB? = null

    /**
     * 只在 View 存活期间访问此属性
     * 推荐：在访问前先检查 view != null 或 isAdded
     */
    protected val mBinding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding 只能在 onCreateView 之后、onDestroyView 之前访问！" + "当前 Fragment: $TAG"
        )

    // 也可以提供一个安全版本，返回 null 而不是崩溃
    protected val mBindingOrNull: VB? get() = _binding

    open val TAG: String by lazy { javaClass.simpleName }

    abstract fun initView()
    open fun initObserve() {}
    open fun initRequestData() {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
        initRequestData()
    }

    protected fun <T> Flow<T>.collectWithViewLife(
        context: CoroutineContext = EmptyCoroutineContext,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend CoroutineScope.(T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch(context) {
            viewLifecycleOwner.repeatOnLifecycle(state) {
                collect { value -> block(value) }
            }
        }
    }

    fun dbg(log: Any?) = Log.d(TAG, log.toString())

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}