package picture.rhona.org.recource;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.rhona.piclibrary.PictureListActivity;
import org.rhona.piclibrary.adapter.BaseQuickAdapter;
import org.rhona.piclibrary.adapter.ViewHolder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0x0000;
    private ArrayList<String> mData = new ArrayList<>();
    private BaseQuickAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_PERMISSION);
            }
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new BaseQuickAdapter<String, ViewHolder>(this, R.layout.item_details, mData) {
            @Override
            public void convert(ViewHolder holder, String item) {
                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(item)
                        .into(holder.getImageView(R.id.img));
                holder.getView(R.id.check_img).setVisibility(View.GONE);
            }
        };
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(mAdapter);
    }

    public void openPiture(View view) {
        startActivityForResult(new Intent(this, PictureListActivity.class)
                        .putExtra("column", 4)
                        .putExtra("count", 9)
                        .putExtra("fileName", Environment.getExternalStorageDirectory().getAbsolutePath() + "/51helper")
                , 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "请添加文件权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mData.clear();
            ArrayList<String> result = (ArrayList<String>) data.getSerializableExtra("result");
            mData.addAll(result);
            mAdapter.notifyDataSetChanged();
        }

        //https://raw.githubusercontent.com/593361260/51helper

    }
}
