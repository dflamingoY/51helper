package org.rhona.piclibrary.adapter

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by yzm on 2018/1/29.
 */
class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var views = SparseArray<View>()

    fun getTextView(layoutId: Int): TextView {
        return this.retrieveView(layoutId)
    }

    fun getImageView(layoutId: Int): ImageView? {
        return this.retrieveView(layoutId)
    }

    fun getView(layoutId: Int): View {
        return retrieveView(layoutId)
    }

    fun <T : View> retrieveView(layId: Int): T {
        var <T> view = views.get(layId);
        if (view == null) {
            view = itemView.findViewById(layId);
            views.put(layId, view)
        }
        return view as T
    }

}