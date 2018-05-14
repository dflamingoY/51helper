package org.rhona.piclibrary.wedgits

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * Created by yzm on 2018/5/14.
 */
class SqureRelative(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}