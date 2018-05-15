# 51helper
简单的图片选择器,使用kotlin语言
本地图片的展示使用Glide 4.0.0 (和Kotlin 兼容)
支持单选多选 

  
.putExtra("column", 4) 展示的行数  
.putExtra("count", 9)选择的数量  
.putExtra("fileName", Environment.getExternalStorageDirectory().getAbsolutePath()) 相机保存的路径

应用内已经适配android7.0调用相机的适配问题
		  
	if (targetFile != null) {
            File file = new File(targetFile.getParent());
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    targetFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(targetFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, code);
  
显示 https://im3.ezgif.com/tmp/ezgif-3-8d0b5a79ac.gif
  

