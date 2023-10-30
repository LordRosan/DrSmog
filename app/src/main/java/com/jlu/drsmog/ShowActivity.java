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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ShowActivity extends AppCompatActivity {

    private Button btn_share;
    private Context mContext;
    private Button btn_back;
    private Button btn_store;
    private Context context;
    private ImageView imageView;
    //定义一个保存图片的File变量
    private File currentImageFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn_back=(Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowActivity.this, CamActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        ImageView imageView = findViewById(R.id.iv1);
        Intent intent = getIntent();

        if (intent != null) {
            // 如果传递的是字节数组
            byte[] byteArray = intent.getByteArrayExtra("image");
            if (byteArray != null) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageView.setImageBitmap(imageBitmap);
            }
            // 如果传递的是文件路径
            // String imagePath = intent.getStringExtra("imagePath");
            // if (imagePath != null) {
            //     Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
            //     imageView.setImageBitmap(imageBitmap);
            // }
        }
        btn_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前显示的图片
                Drawable drawable = imageView.getDrawable();
                Bitmap imageBitmap = ((BitmapDrawable) drawable).getBitmap();

                // 保存图片到外部存储
                String filename = "image.jpg"; // 指定文件名
                FileOutputStream out = null;
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), filename);
                    out = new FileOutputStream(file);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // 保存为JPEG格式
                    out.flush();
                    out.close();

                    // 显示一个提示，告诉用户图片已保存
                    Toast.makeText(getApplicationContext(), "图片已保存", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 处理保存失败的情况
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mContext=ShowActivity.this;
        btn_share=(Button) findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopWindow(v);
            }
        });
    }

    private void initPopWindow(View v) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_popup, null, false);

        Button btn_pyq = (Button) view.findViewById(R.id.btn_pyq);
        Button btn_qq = (Button) view.findViewById(R.id.btn_qq);
        Button btn_email = (Button) view.findViewById(R.id.btn_email);
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.drawable.ic_pop_bg);  //设置加载动画

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效


        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown(v,-300,10);

        //设置popupWindow里的按钮的事件
        btn_pyq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shareToPYQ();
                popWindow.dismiss();
            }
        });
        btn_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shareToQQ();
                popWindow.dismiss();
            }
        });
        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用邮件分享SDK的分享接口，将图片分享至邮箱
                // 具体实现方式请参考邮件分享SDK的文档
                //shareToEmail();
                popWindow.dismiss();
            }
        });
    }
        private Bitmap generateImage() {
            // 创建一个画布
            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // 绘制图片内容
            canvas.drawColor(Color.BLUE);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(50);
            canvas.drawText("这是要分享的图片", 100, 250, paint);
            return bitmap;
        }
}