package com.jlu.drsmog;

import static android.content.Intent.getIntent;

import android.content.Intent;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.content.Intent;

public class Calculate extends Service {
    public Calculate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private Bitmap croppedImage;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在这里执行图像处理和灰度直方图计算的逻辑

        // 加载图像
        croppedImage = intent.getParcelableExtra("image");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);

        // 获取图像的宽度和高度
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 初始化灰度直方图数组
        int[] histogram = new int[256]; // 0-255个灰度级别

        // 遍历图像像素，统计灰度直方图
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int red = (pixel >> 16) & 0xFF; // 提取红色通道作为灰度值
                histogram[red]++;
            }
        }
        float darkness= 0;
        for (int i = 0; i < 256; i++) {
            darkness += histogram[i];
        }
        darkness /= width*height; // 算出平均值

        // 创建一个新的Intent来启动ShowActivity
        Intent showIntent = new Intent(this, ShowActivity.class);
        // 将计算出的dacker作为extra传递给ShowActivity
        showIntent.putExtra("dacker_value", darkness);

        // 由于服务中不能直接启动Activity，需要添加FLAG_ACTIVITY_NEW_TASK标志
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动ShowActivity
        startActivity(showIntent);

        return super.onStartCommand(intent, flags, startId);
    }
}
