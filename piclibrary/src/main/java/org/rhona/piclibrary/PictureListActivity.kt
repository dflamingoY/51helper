package org.rhona.piclibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_list.*
import org.rhona.piclibrary.model.FileData
import org.rhona.piclibrary.tools.AppTools
import org.rhona.piclibrary.wedgits.CustomCheckImageView
import org.rhona.wallper.adapter.BaseQuickAdapter
import org.rhona.wallper.adapter.ViewHolder
import java.io.File
import java.io.FilenameFilter

/**
 * Created by yzm on 2018/5/14.
 */
class PictureListActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PRECODE = 0x0001
        private const val REQUEXT_CAMERA = 0x0002
    }

    private var maxCount = 9
    private var column = 4
    private var adapter: BaseQuickAdapter<String, ViewHolder>? = null
    private var data = ArrayList<String>()
    private val heardList by lazy { ArrayList<FileData>() }//文件夹是数据
    private var heardAdapter: BaseQuickAdapter<FileData, ViewHolder>? = null//显示所有文件夹
    private var current = 0
    private var selectedList = ArrayList<String>()
    private var extraFile: String? = null  //拍照图片的保存路径
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        column = intent.getIntExtra("column", 4)
        maxCount = intent.getIntExtra("count", 9)
        extraFile = intent.getStringExtra("fileName")
        adapter = object : BaseQuickAdapter<String, ViewHolder>(this, R.layout.item_details, data) {
            override fun convert(holder: ViewHolder?, item: String?) {
                if ("相机" == item) {
                    holder?.getImageView(R.id.img)?.setImageResource(R.mipmap.btn_take_photo)
                    holder?.getView(R.id.check_img)?.visibility = View.GONE
                } else {
                    holder?.getView(R.id.check_img)?.visibility = View.VISIBLE
                    Glide.with(this@PictureListActivity)
                            .asBitmap()
                            .load(item)
                            .apply(RequestOptions().centerCrop().override(180))
                            .into(holder?.getImageView(R.id.img))
                    item?.let { holder?.getView(R.id.iv_Gif)?.visibility = if (it.endsWith(".gif")) View.VISIBLE else View.GONE }
                    val view = holder?.getView(R.id.check_img) as CustomCheckImageView
                    view.isSelected = selectedList.contains(item)
                    view.setOnStateChangeListener { selected ->
                        if (selected)
                            if (selectedList.size == maxCount) {
                                Toast.makeText(this@PictureListActivity, "最多选择" + maxCount + "张照片", Toast.LENGTH_SHORT).show()
                                view.isSelected = false
                                return@setOnStateChangeListener
                            }
                        if (selectedList.contains(item)) {
                            item?.let { selectedList.remove(it) }
                        } else {
                            item?.let { selectedList.add(it) }
                        }
                        tv_Count.text = selectedList.size.toString()
                        tv_Count.visibility = if (selectedList.size == 0) View.GONE else View.VISIBLE
                    }
                }
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = adapter
        heardAdapter = object : BaseQuickAdapter<FileData, ViewHolder>(this, R.layout.item_heard_details, heardList) {
            override fun convert(holder: ViewHolder?, item: FileData?) {
                Glide.with(this@PictureListActivity)
                        .asBitmap()
                        .load(item?.firstPath)
                        .apply(RequestOptions().centerCrop().override(180))
                        .into(holder?.getImageView(R.id.iv_img))
                holder?.getTextView(R.id.tv_Title)?.text = item?.parentName
                holder?.getTextView(R.id.tv_Count)?.text = item?.count.toString()
            }
        }

        heardRecycler.layoutManager = LinearLayoutManager(this)
        heardRecycler.adapter = heardAdapter
        ObjectAnimator.ofFloat(heardRecycler, "translationY", -AppTools.getWindowsHeight(this).toFloat()).setDuration(0).start()
        getHeard()
        bindEvent()
    }

    private fun bindEvent() {
        tv_Title.setOnClickListener {
            if (heardRecycler.y == 0f) {
                ObjectAnimator.ofFloat(heardRecycler, "translationY", -AppTools.getWindowsHeight(this).toFloat()).setDuration(320).start()
            } else {
                ObjectAnimator.ofFloat(heardRecycler, "translationY", 0f).setDuration(320).start()
            }
        }
        adapter?.setOnItemClikListener(object : BaseQuickAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                /**
                 * 数据太大导致数据传递失败  solution
                 * 1.数据存储在本地  调取本地数据
                 * 2.数据共享 写静态数组
                 * 3.压缩数据
                 * 4.直接给文件夹路径
                 */
                if ("相机" == data?.get(position)) {
                    /**
                     * 打开相机
                     */
                    file = File(extraFile + "/" + System.currentTimeMillis() + ".png")
                    AppTools.openCamera(this@PictureListActivity, REQUEXT_CAMERA, file)
                } else
                    startActivityForResult(Intent(this@PictureListActivity, PreActivity::class.java)
                            .putExtra("selected", selectedList)
                            .putExtra("current", if (current == 0) position - 1 else position)
                            .putExtra("parentPath", heardList[current].path)
                            .putExtra("allow", maxCount)
                            , REQUEST_PRECODE)
            }
        })
        heardAdapter?.setOnItemClikListener(object : BaseQuickAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                data.clear()
                if (position == 0) {
                    getHeard()
                } else {
                    getPicList(heardList[position].path!!)
                }
                ObjectAnimator.ofFloat(heardRecycler, "translationY", -AppTools.getWindowsHeight(this@PictureListActivity).toFloat()).setDuration(320).start()
                current = position
            }
        })
        tv_Pre.setOnClickListener {
            /**
             * 预览图片
             */
            if (selectedList.size > 0)
                startActivityForResult(Intent(this@PictureListActivity, PreActivity::class.java)
                        .putExtra("selected", selectedList)
                        .putExtra("allow", maxCount)
                        , REQUEST_PRECODE)
        }
        tv_Commit.setOnClickListener {
            if (selectedList.size > 0) {
                setResult(Activity.RESULT_OK, Intent().putExtra("result", selectedList))
                finish()
            }
        }
    }

    /*
     * 获取相册文件夹
     */
    @SuppressLint("StaticFieldLeak")
    @Synchronized
    fun getHeard() {
        heardList.clear()
        object : AsyncTask<Void, Void, List<String>>() {
            override fun doInBackground(vararg p0: Void?): List<String>? {
                val data = ArrayList<String>()
                var firstPath: String?
                val parentList = java.util.ArrayList<String>()
                val imageUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val resolver = this@PictureListActivity.contentResolver
                val cursor = resolver.query(imageUrl, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        arrayOf("image/jpeg", "image/png", "image/gif"), MediaStore.Images.Media.DATE_MODIFIED)
                if (null != cursor && cursor!!.count > 0) {
                    while (cursor!!.moveToNext()) {
                        val path = cursor!!.getString(cursor!!
                                .getColumnIndex(MediaStore.Images.Media.DATA))
                        data.add(0, path)

                        firstPath = path
                        val parentFile = File(path).parentFile ?: continue
                        val parentPath = parentFile.absolutePath
                        var backResult: FileData?
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
                        val picSize: Int//此目录下有几张图片
                        try {
                            picSize = parentFile.list(filter).size
                        } catch (e: Exception) {
                            e.printStackTrace()
                            continue
                        }
                        backResult!!.count = picSize
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
                data.add("相机")
                data.addAll(result!!)
                adapter?.notifyDataSetChanged()
            }
        }.execute()
    }

    @SuppressLint("StaticFieldLeak")
    @Synchronized
    fun getPicList(path: String) {
        object : AsyncTask<Void, Void, Array<String>>() {
            override fun doInBackground(vararg p0: Void?): Array<String>? {
                val file = File(path)
                if (!file.exists() || !file.isDirectory) {
                    return null
                }
                val fileter = FilenameFilter { _, s -> s.endsWith(".gif") || s.endsWith("jpeg") || s.endsWith(".png") || s.endsWith(".jpg") }
                val list = file.list(fileter)
                return list
            }

            override fun onPostExecute(result: Array<String>?) {
                super.onPostExecute(result)
                if (result == null)
                    return
                for (suffix in result!!) {
                    data.add("$path/$suffix")
                }
                adapter?.notifyDataSetChanged()
            }
        }.execute()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PRECODE) {
                data?.let {
                    val resultData = data.getSerializableExtra("select") as ArrayList<String>
                    setResult(Activity.RESULT_OK, Intent().putExtra("result", resultData))
                    finish()
                }
            } else if (requestCode == REQUEXT_CAMERA) {
                this.data.add(1, file?.absolutePath!!)
                adapter?.notifyDataSetChanged()
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                sendBroadcast(mediaScanIntent)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_PRECODE) {
                data?.let {
                    selectedList = data.getSerializableExtra("select") as ArrayList<String>
                    adapter?.notifyDataSetChanged()
                    tv_Count.text = selectedList.size.toString()
                    tv_Count.visibility = if (selectedList.size == 0) View.GONE else View.VISIBLE
                }
            }
        }

    }

}