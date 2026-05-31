package com.nextgen.iptv.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView

class RowRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    var onRowDown: (() -> Unit)? = null
    var onRowUp: (() -> Unit)? = null
    var onFirstItemBack: (() -> Unit)? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> { onRowDown?.invoke(); true }
            KeyEvent.KEYCODE_DPAD_UP -> { onRowUp?.invoke(); true }
            KeyEvent.KEYCODE_BACK -> {
                val focused = focusedChild
                val pos = if (focused != null) getChildAdapterPosition(focused) else -1
                if (pos == 0) { onFirstItemBack?.invoke(); true }
                else { scrollToPosition(0); layoutManager?.findViewByPosition(0)?.requestFocus(); true }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
