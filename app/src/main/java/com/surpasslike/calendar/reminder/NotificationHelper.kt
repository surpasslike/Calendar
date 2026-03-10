package com.surpasslike.calendar.reminder

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils

/**
 * 通知权限辅助工具: 处理 POST_NOTIFICATIONS 运行时权限
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"

    /**
     * 在 Fragment 中注册通知权限请求 Launcher
     * 必须在 onCreate() 中调用
     */
    fun registerPermissionLauncher(fragment: Fragment): ActivityResultLauncher<String> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            LogUtils.d(TAG, "通知权限请求结果: $isGranted")
        }
    }

    /**
     * 检查通知权限,如果未授权则请求
     */
    fun requestIfNeeded(fragment: Fragment, launcher: ActivityResultLauncher<String>) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(), permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            LogUtils.d(TAG, "requestIfNeeded: 请求通知权限")
            launcher.launch(permission)
        } else {
            LogUtils.d(TAG, "requestIfNeeded: 通知权限已授权")
        }
    }
}
