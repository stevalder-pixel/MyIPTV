package com.nextgen.iptv.ui.livetv

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView

class TvFocusRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val lm = layoutManager ?: return super.onKeyDown(keyCode, event)
        val focused = focusedChild ?: return super.onKeyDown(keyCode, event)
        val pos = getChildAdapterPosition(focused)

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (pos < (adapter?.itemCount ?: 0) - 1) {
                    scrollToPosition(pos + 1)
                    getChildAt(0)?.let { layoutManager?.findViewByPosition(pos + 1)?.requestFocus() }
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (pos > 0) {
                    scrollToPosition(pos - 1)
                    layoutManager?.findViewByPosition(pos - 1)?.requestFocus()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                focused.performClick()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
