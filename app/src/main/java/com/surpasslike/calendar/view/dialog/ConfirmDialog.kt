package com.surpasslike.calendar.view.dialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.surpasslike.calendar.R

class ConfirmDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val onConfirm: () -> Unit
) {
    fun show() {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_confirm) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
