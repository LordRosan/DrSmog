package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ShowActivity extends AppCompatActivity {

    private Button btn_share;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        mContext=ShowActivity.this;
        btn_share=(Button) findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopWindow(view);
            }
        });
    }

    private void initPopWindow(View v) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_popup, null, false);
        Button btn_pyq = (Button) view.findViewById(R.id.btn_pyq);
        Button btn_qq = (Button) view.findViewById(R.id.btn_qq);
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.drawable.ic_action_pop);  //设置加载动画

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //要为popWindow设置一个背景才有效


        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown(v, 50, 0);

        //设置popupWindow里的按钮的事件
        btn_pyq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToPYQ();
            }
        });
        btn_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToQQ();
            }
        });
    }

    private void shareToQQ() {
        Bitmap bitmap = generateImage(); // 生成要分享的图片

        // 保存图片到本地
        File imagePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image.png");
        try {
            FileOutputStream fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 分享图片到QQ
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        boolean isQQInstalled = false;
        for(ResolveInfo resolveInfo : resolveInfoList){
            String packageName = resolveInfo.activityInfo.packageName;
            if(packageName!=null&&packageName.startsWith("com.tencent.mobileqq")){
                isQQInstalled=true;
            }
        }
         if (isQQInstalled){
             Intent qqIntent = new Intent(Intent.ACTION_SEND);
             qqIntent.setType("image/*");
             Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imagePath);
             qqIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
             qqIntent.setPackage("com.tencent.mobileqq");
             startActivity(qqIntent);
         }else{
             Toast.makeText(this,"未安装QQ",Toast.LENGTH_SHORT).show();
         }
    }

    private void shareToPYQ() {
        Bitmap bitmap = generateImage(); // 生成要分享的图片

        // 保存图片到本地
        File imagePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image.png");
        try {
            FileOutputStream fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 分享图片到微信
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        boolean isWechatInstalled = false;
        for(ResolveInfo resolveInfo : resolveInfoList){
            String packageName = resolveInfo.activityInfo.packageName;
            if(packageName!=null&&packageName.startsWith("com.tencent.mm")){
                isWechatInstalled=true;
            }
        }
        if (isWechatInstalled){
            Intent pyqIntent = new Intent(Intent.ACTION_SEND);
            pyqIntent.setType("image/*");
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imagePath);
            pyqIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            pyqIntent.setPackage("com.tencent.mm");
            startActivity(pyqIntent);
        }else{
            Toast.makeText(this,"未安装微信",Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap generateImage() {
        // 创建一个画布
        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制图片内容
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);
        canvas.drawText("这是要分享的图片", 100, 250, paint);

        return bitmap;
    }
}