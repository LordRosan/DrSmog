package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ShowActivity extends AppCompatActivity {

    private Button btn_share;
    private Context mContext;
    private Button btn_back;
    private Button btn_store;
    private Button btn_home;
    private TextView tv2;
    private ImageView iv1;
    String ShareText;
    Bitmap currentImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn_back=(Button)findViewById(R.id.btn_back);
        btn_home=(Button)findViewById(R.id.btn_home);
        mContext=ShowActivity.this;
        btn_store=(Button)findViewById(R.id.btn_store);
        btn_share=(Button) findViewById(R.id.btn_share);
        iv1=findViewById(R.id.iv1);
        tv2=findViewById(R.id.tv2);

        Intent intent = getIntent();
        boolean isPath = intent.getBooleanExtra("isPath", true);

        if (isPath) {
            String imagePath = intent.getStringExtra("image_path");
            if (imagePath != null) {
                currentImage = BitmapFactory.decodeFile(imagePath);
            }
        } else {
            Uri imageUri = Uri.parse(intent.getStringExtra("image_uri"));
            if (imageUri != null) {
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                currentImage = BitmapFactory.decodeStream(inputStream);
            }
        }
        // 显示裁剪后的图片在ImageView界面上
        iv1.setImageBitmap(currentImage);
        float darkness = getIntent().getFloatExtra("dacker_value", 0); // 0为默认值
        if(darkness==0)
            ShareText="全白 黑度值:"+darkness;
        if(darkness>0&&darkness<=51)
            ShareText="微灰 黑度值:"+darkness;
        if(darkness>51&&darkness<=102)
            ShareText="灰 黑度值:"+darkness;
        if(darkness>102&&darkness<=153)
            ShareText="深灰 黑度值:"+darkness;
        if(darkness>153&&darkness<=204)
            ShareText="灰黑 黑度值:"+darkness;
        if(darkness>204&&darkness<=255)
            ShareText="全黑 黑度值:"+darkness;
        String darknessLevel = "Darkness Level: " + darkness;
        tv2.setText(ShareText);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowActivity.this, CamActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btn_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查SD卡是否可用
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    // 获取SD卡的根目录
                    File sdCardDir = Environment.getExternalStorageDirectory();
                    // 创建一个名为"my_images"的子目录
                    File myDir = new File(sdCardDir, "my_images");
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                    // 创建一个名为"my_image.jpg"的文件
                    File file = new File(myDir, "my_image.jpg");
                    try {
                        // 创建一个输出流，将图片数据写入文件
                        FileOutputStream fos = new FileOutputStream(file);
                        Bitmap bitmap = null;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                        // 写入成功，提示用户
                        Toast.makeText(ShowActivity.this, "图片已保存至SD卡", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        // 写入失败，提示用户
                        Toast.makeText(ShowActivity.this, "图片保存失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    // SD卡不可用，提示用户
                    Toast.makeText(ShowActivity.this, "SD卡不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initPopWindow(v);
                shareContent(ShareText,currentImage);
            }
        });
    }
    private void shareContent(String shareText, Bitmap shareImageBitmap) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra("Kdescription", shareText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText); // 设置分享的主题或标题
        if (shareImageBitmap != null) {
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), shareImageBitmap, "ShareImage", null);
            Uri bitmapUri = Uri.parse(bitmapPath);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        }
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
}