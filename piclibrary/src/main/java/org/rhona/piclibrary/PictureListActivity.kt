package org.rhona.piclibrary

import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.Window
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_list.*
import org.rhona.piclibrary.model.FileData
import org.rhona.piclibrary.tools.AppTools
import org.rhona.wallper.adapter.BaseQuickAdapter
import org.rhona.wallper.adapter.ViewHolder
import java.io.File
import java.io.FilenameFilter

/**
 * Created by yzm on 2018/5/14.
 */
class PictureListActivity : AppCompatActivity() {
    var maxCount = 9
    var column = 4
    var adapter: BaseQuickAdapter<String, ViewHolder>? = null
    var data = ArrayList<String>()
    var heardList = ArrayList<FileData>()
    var heardAdapter: BaseQuickAdapter<FileData, ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        column = intent.getIntExtra("column", 4)
        maxCount = intent.getIntExtra("count", 9)
        adapter = object : BaseQuickAdapter<String, ViewHolder>(this, R.layout.item_details, data) {
            override fun convert(holder: ViewHolder?, item: String?) {
                Glide.with(this@PictureListActivity)
                        .asBitmap()
                        .load(item)
                        .apply(RequestOptions().centerCrop().override(180))
                        .into(holder?.getImageView(R.id.img))
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 4) as RecyclerView.LayoutManager?
        recyclerView.adapter = adapter
        heardAdapter = object : BaseQuickAdapter<FileData, ViewHolder>(this, R.layout.item_heard_details, heardList) {
            override fun convert(holder: ViewHolder?, item: FileData?) {
                Glide.with(this@PictureListActivity)
                        .asBitmap()
                        .load(item?.firstPath)
                        .apply(RequestOptions().centerCrop().override(180))
                        .into(holder?.getImageView(R.id.iv_img))
                holder?.getTextView(R.id.tv_Title)?.setText(item?.parentName)
                holder?.getTextView(R.id.tv_Count)?.setText(item?.count.toString())
            }
        }
        
        heardRecycler.layoutManager = LinearLayoutManager(this)
        heardRecycler.adapter = heardAdapter
        ObjectAnimator.ofFloat(heardRecycler, "translationY", -AppTools.getWindowsHeight(this).toFloat()).setDuration(0).start()
        getHeard()
        bindEvent()
    }

    fun bindEvent() {
        tv_Title.setOnClickListener(View.OnClickListener {
            if (heardRecycler.y == 0f) {
                ObjectAnimator.ofFloat(heardRecycler, "translationY", -AppTools.getWindowsHeight(this).toFloat()).setDuration(320).start()
            } else {
                ObjectAnimator.ofFloat(heardRecycler, "translationY", 0f).setDuration(320).start()
            }
        })
    }

    /*
     * 获取相册文件夹
     */
    @Synchronized
    fun getHeard() {

        object : AsyncTask<Void, Void, List<String>>() {
            override fun doInBackground(vararg p0: Void?): List<String>? {
                val data = ArrayList<String>()
                var firstPath: String? = null
                val parentList = java.util.ArrayList<String>()
                val imageUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val resolver = this@PictureListActivity.getContentResolver()
                val cursor = resolver.query(imageUrl, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        arrayOf("image/jpeg", "image/png", "image/gif"), MediaStore.Images.Media.DATE_MODIFIED)
                if (null != cursor && cursor!!.getCount() > 0) {
                    while (cursor!!.moveToNext()) {
                        val path = cursor!!.getString(cursor!!
                                .getColumnIndex(MediaStore.Images.Media.DATA))
                        data.add(0, path)

                        firstPath = path
                        val parentFile = File(path).getParentFile() ?: continue
                        val parentPath = parentFile.getAbsolutePath()
                        var backResult: FileData? = null
                        if (parentList.contains(parentPath))
                            continue//防止多次添加
                        else {
                            parentList.add(parentPath)
                            backResult = FileData()
                            val index = parentPath.lastIndexOf("/")
                            backResult!!.parentName = parentPath.substring(index + 1)
                            backResult!!.path = parentPath
                            backResult!!.firstPath = firstPath
                        }
                        val filter = FilenameFilter { dir, filename -> filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif") }
                                ?: continue
                        val picSize: Int//此目录下有几张图片
                        try {
                            picSize = parentFile.list(filter).size
                        } catch (e: Exception) {
                            e.printStackTrace()
                            continue
                        }

                        backResult!!.count = picSize
                        firstPath = null
                        this@PictureListActivity.heardList.add(backResult)
                    }
                }
                cursor!!.close()
                return data
            }

            override fun onPostExecute(result: List<String>?) {
                super.onPostExecute(result)
                val filePath = FileData()
                try {
                    filePath.firstPath = (result?.get(0))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                filePath.count = result!!.size
                filePath.parentName = "全部图片"
                heardList.add(0, filePath)
                heardAdapter?.notifyDataSetChanged()
                data.addAll(result!!)
                adapter?.notifyDataSetChanged()
            }

        }.execute()

    }

    @Synchronized
    fun getPicList() {
//        AsyncTask<Void, Void, >

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyDown(keyCode, event)
    }


}