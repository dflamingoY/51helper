package org.rhona.wallper.adapter

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.rhona.piclibrary.R
import java.util.*

/**
 * Created by yzm on 2018/1/29.
 */
abstract class BaseQuickAdapter<T, H : ViewHolder> : RecyclerView.Adapter<ViewHolder>, View.OnClickListener {
    private var context: Context? = null
    private val ITEM_TYPE_DEFAULT = 0
    private val ITEM_TYPE_HEARD = 1
    private val ITEM_TYPE_FOOT = 2
    private var list: ArrayList<T>? = null
    private var layoutResId = 0
    private var hearCount = 0
    private var footCount = 0

    private var heardView: View? = null
    private var footView: View? = null
    private var isHadHeard = false
    private var isHadFoot = false
    private var loadMoreView: View? = null
    private var mLastVisibleItem: Int = 0

    constructor(context: Context?, layoutResId: Int) {
        this.context = context
        this.list = list
    }

    constructor(context: Context?, layoutResId: Int, list: ArrayList<T>?) {
        this.context = context
        this.list = list
        this.layoutResId = layoutResId
    }

    constructor(context: Context?, layoutResId: Int, list: ArrayList<T>?, heardView: View?, footView: View?) {
        this.context = context
        this.list = list
        if (heardView != null) {
            this.heardView = heardView
            isHadHeard = true
            hearCount = 1
        }
        if (footView == null) {
            this.footView = footView
            isHadFoot = true
            footCount = 1
        }
        this.layoutResId = layoutResId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == ITEM_TYPE_HEARD)
            return ViewHolder(heardView)
        if (viewType == ITEM_TYPE_FOOT)
            return ViewHolder(footView)
        val view = LayoutInflater.from(parent?.getContext()).inflate(layoutResId, parent, false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isHadHeard && isHeard(position))
            return
        if (isHadFoot && isFooter(position))
            return
        holder!!.itemView.setOnLongClickListener(View.OnLongClickListener { v ->
            if (longClickListener != null) {
                longClickListener?.onItemLongClick(v, v.tag as Int)
                true
            } else {
                false
            }
        })
        holder?.itemView?.setTag(position - hearCount)
        val item = getItem(position - hearCount)
        convert(holder as H, item)
    }

    private fun getItem(position: Int): T? {
        return if (position >= list!!.size) null else list?.get(position)
    }

    fun isHeard(position: Int): Boolean {
        if (isHadHeard) {
            if (position == 0)
                return true
        }
        return false
    }

    fun isFooter(position: Int): Boolean {
        if (list?.size == 0) {
            return position == hearCount
        }
        return position == (list!!.size + hearCount)
    }

    abstract fun convert(holder: ViewHolder?, item: T?)

    override fun getItemViewType(position: Int): Int {
        if (isHadHeard) {
            if (isHeard(position)) {
                return ITEM_TYPE_HEARD
            }
        } else if (isHadFoot) {
            if (isFooter(position))
                return ITEM_TYPE_FOOT
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        if (list == null)
            return 0
        return list!!.size + hearCount + footCount
    }

    override fun onClick(v: View?) {
        if (itemClickListener != null) {
            itemClickListener?.onItemClick(v!!, v.getTag() as Int)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnItemLongClikListener {
        fun onItemLongClick(view: View, position: Int)
    }

    private var itemClickListener: OnItemClickListener? = null
    private var longClickListener: OnItemLongClikListener? = null

    fun setOnItemClikListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun setOnLongClickListener(listener: OnItemLongClikListener) {
        longClickListener = listener
    }

    private fun setFootView(manager: RecyclerView.LayoutManager, footView: View) {
        if (footView != null) {
            footCount = 1
            this.footView = footView
            isHadFoot = true
            if (manager is GridLayoutManager) {
                manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isFooter(position)) manager.spanCount else 1
                    }
                }
            }
        }
    }

    /**
     * 设置自动更新更多数据
     */
    fun setLoadmore(recycler: RecyclerView, manager: RecyclerView.LayoutManager, footView: View) {
        setFootView(manager, footView)
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (mLastVisibleItem + 1 == this@BaseQuickAdapter.getItemCount()) {
                    if (mOnLoadListener == null) return
                    if (mLoadState == ELoadState.READY) {
                        mOnLoadListener?.onLoadMore()
                        setLoadStatue(ELoadState.LOADING)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (manager is LinearLayoutManager /*|| mLayoutManager instanceof GridLayoutManager*/) {
                    mLastVisibleItem = manager.findLastVisibleItemPosition()
                }
            }
        })
    }

    private var mLoadState = ELoadState.GONE

    enum class ELoadState {
        READY, //准备
        LOADING, //加载中
        EMPTY, //空数据
        GONE, //不可见
        NULLDATA//空数据
    }

    private var mOnLoadListener: OnLoadListener? = null

    fun setOnLoadListener(mOnLoadListener: OnLoadListener) {
        this.mOnLoadListener = mOnLoadListener
    }

    interface OnLoadListener {
        fun onLoadMore()
    }


    fun setLoadStatue(loadStatue: ELoadState) {
        mLoadState = loadStatue
        val msg = footView?.findViewById(R.id.text_msg) as TextView
        val mLinearProgress = footView?.findViewById(R.id.linearProgress) as View
        val progress = footView?.findViewById(R.id.progress) as View
        val emptyView = footView?.findViewById(R.id.relative_Empty) as View
        when (loadStatue) {
            ELoadState.GONE -> {
                msg.setText("上拉加载")
                footView?.setVisibility(View.GONE)
            }
            ELoadState.LOADING -> {
                msg.setText("正在加载…")
                progress.setVisibility(View.VISIBLE)
            }
            ELoadState.READY -> {
                msg.setText("上拉或点击加载更多")
                mLinearProgress.setVisibility(View.VISIBLE)
                emptyView.setVisibility(View.GONE)
                progress.setVisibility(View.GONE)
                footView?.setVisibility(View.VISIBLE)
            }
            ELoadState.EMPTY -> {
                msg.setText("没有更多数据啦")
                progress.setVisibility(View.GONE)
                footView?.setVisibility(View.VISIBLE)
            }
            ELoadState.NULLDATA -> {
                mLinearProgress.setVisibility(View.GONE)
                emptyView.setVisibility(View.VISIBLE)
                progress.setVisibility(View.GONE)
                footView?.setVisibility(View.VISIBLE)
            }
        }
    }

}