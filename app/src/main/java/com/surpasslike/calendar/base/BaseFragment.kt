package com.surpasslike.calendar.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

typealias Inflate<VB> = (LayoutInflater, ViewGroup?, Boolean) -> VB

/**
 * Fragment 的通用基类
 * 举例:
 * 1. kotlin:
 *    1.1 class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {}
 *    1.2 mBinding.tvTitle
 *    1.3 TAG
 * 2. java:
 *    2.1
 *    public class HomeFragment extends BaseFragment<FragmentHomeBinding> {
 *        public HomeFragment() {
 *           super(FragmentHomeBinding::inflate);  // 调用父类构造函数
 *        }
 *    }
 *    2.2 getMBinding().tvTitle
 *    2.3 getTAG()
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    private var _binding: VB? = null

    /**
     * 在 initView, initObserve等点击事件中使用
     */
    protected val mBinding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding 只能在 onCreateView 之后、onDestroyView 之前访问！当前 Fragment: $TAG"
        )

    /**
     * 在网络请求回调、postDelayed、倒计时等可能在页面销毁后使用
     * 用法：mBindingOrNull?.tvTitle?.text = "安全更新"
     */
    protected val mBindingOrNull: VB? get() = _binding

    open val TAG: String by lazy { javaClass.simpleName }

    // 抽象方法,子类必须实现
    abstract fun initView()
    // 数据观察，子类可选择重写
    open fun initObserve() {}
    // 数据请求，子类可选择重写
    open fun initRequestData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        LogUtils.d(TAG, "onCreateView")
        _binding = inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtils.d(TAG, "onViewCreated")
        initView()
        initObserve()
        initRequestData()
    }

    override fun onStart() {
        super.onStart()
        LogUtils.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtils.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtils.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtils.d(TAG, "onStop")
    }

    override fun onDestroyView() {
        LogUtils.d(TAG, "onDestroyView")
        super.onDestroyView()
        _binding = null // 释放 ViewBinding 防止内存泄漏
    }

    override fun onDestroy() {
        LogUtils.d(TAG, "onDestroy")
        super.onDestroy()
    }

    /**
     * 安全地在 Fragment 生命周期内收集 Flow 数据
     * 使用 viewLifecycleOwner 确保只在 UI 可见时收集，避免内存泄漏
     *
     * @param context 协程上下文
     * @param state 生命周期状态（默认 STARTED）
     * @param block 收集数据的回调函数
     */
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

    /*
    * 备用Log使用方法
    * */
    fun dbg(log: Any?) = LogUtils.d(TAG, log.toString())
}