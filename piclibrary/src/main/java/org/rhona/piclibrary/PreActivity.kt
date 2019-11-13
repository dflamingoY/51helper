package org.rhona.piclibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_pre.*
import org.rhona.piclibrary.tools.AppTools
import org.rhona.piclibrary.wedgits.ivView.PhotoView
import java.io.File
import java.io.FilenameFilter

/**
 * Created by yzm on 2018/5/14.
 */
class PreActivity : AppCompatActivity() {
    private var data = ArrayList<String>()//all'数据
    private var selectedList = ArrayList<String>()//选中的list
    private var currentPage = 0 //当前角标
    private var full = false //是否全屏
    private var maxCount = 9//最大允许选择数量

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            decorView.systemUiVisibility = option
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        setContentView(R.layout.activity_pre)
        val params = status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        status_bar.layoutParams = params
        val serializableExtra = intent.getSerializableExtra("selected") as ArrayList<String>
        currentPage = intent.getIntExtra("current", -1)
        val parentPath = intent.getStringExtra("parentPath")
        maxCount = intent.getIntExtra("allow", 9)
        selectedList.addAll(serializableExtra)
        if (currentPage == -1) {//  表示是从预览点击进来的
            data.addAll(serializableExtra)
            viewPager.adapter = ImgAdapter()
            tv_Index.text = "1/" + data.size
            check_img.isSelected = true

        } else {//要去取数据
            getData(parentPath)
        }
        if (selectedList.size > 0) {
            tv_Count.visibility = View.VISIBLE
            tv_Count.text = selectedList.size.toString()
        }
        bindEvent()
    }

    private fun bindEvent() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                check_img.isSelected = selectedList.contains(data[position])
                tv_Index.text = "${(position + 1)}/${data.size}"
            }
        })
        check_img.setOnStateChangeListener {
            if (it) {
                if (selectedList.size == maxCount) {
                    check_img.isSelected = false
                    Toast.makeText(this@PreActivity, "最多选择" + maxCount + "张照片", Toast.LENGTH_SHORT).show()
                    return@setOnStateChangeListener
                }
            }
            if (selectedList.contains(data[viewPager.currentItem])) {
                selectedList.remove(data[viewPager.currentItem])
            } else {
                selectedList.add(data[(viewPager.currentItem)])
            }
            tv_Count.text = selectedList.size.toString()

            if (selectedList.size > 0) {
                tv_Count.visibility = View.VISIBLE
                tv_Count.text = selectedList.size.toString()
            } else {
                tv_Count.visibility = View.GONE
            }
        }
        tv_Commit.setOnClickListener {
            if (selectedList.size > 0) {
                setResult(RESULT_OK, Intent().putExtra("select", selectedList))
                finish()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun getData(path: String?) {
        object : AsyncTask<Void, Void, List<String>>() {
            @SuppressLint("Recycle")
            override fun doInBackground(vararg p0: Void?): List<String> {
                val data = ArrayList<String>()
                if (TextUtils.isEmpty(path)) {
                    val imageUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val resolver = this@PreActivity.contentResolver
                    val cursor = resolver.query(imageUrl, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                            arrayOf("image/jpeg", "image/png", "image/gif"), MediaStore.Images.Media.DATE_MODIFIED)
                    if (null != cursor && cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Images.Media.DATA))
                            data.add(0, path)
                        }
                    }
                    cursor!!.close()
                } else {
                    val file = File(path)
                    if (file.isDirectory) {
                        val filter = FilenameFilter { _, s -> s.endsWith("png") || s.endsWith("jpg") || s.endsWith("jpeg") || s.endsWith("gif") }
                        val strs = file.list(filter)
                        for (str in strs) {
                            data.add("$path/$str")
                        }
                    }
                }
                return data
            }

            override fun onPostExecute(result: List<String>?) {
                data.addAll(result!!)
                viewPager.adapter = ImgAdapter()
                viewPager.setCurrentItem(currentPage, false)
            }
        }.execute()
    }

    private inner class ImgAdapter : PagerAdapter() {

        @SuppressLint("InflateParams")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(this@PreActivity).inflate(R.layout.view_photo, null)
            val photoView = view.findViewById<PhotoView>(R.id.photoView)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            photoView.enable()
            Glide.with(this@PreActivity)
                    .load("file://" + data[position])
                    .apply(RequestOptions().skipMemoryCache(true))
                    .into(photoView)
            photoView.setOnClickListener {
                doAnim()
            }
            container.addView(view, params)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)

        }

        override fun getCount(): Int {
            return data.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }
    }

    private fun doAnim() {
        full = !full
        if (full) {//全屏
            ObjectAnimator.ofFloat(toolbar, "translationY", -toolbar.height.toFloat()).setDuration(220).start()
            ObjectAnimator.ofFloat(bottom_Menu, "translationY", bottom_Menu.height.toFloat()).setDuration(220).start()
        } else {
            ObjectAnimator.ofFloat(toolbar, "translationY", 0F).setDuration(220).start()
            ObjectAnimator.ofFloat(bottom_Menu, "translationY", 0F).setDuration(220).start()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent().putExtra("select", selectedList))
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        data.clear()
        selectedList.clear()
    }
}