package com.jlu.drsmog;

import static android.content.Intent.getIntent;
import static android.content.Intent.parseIntent;

import android.content.Intent;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Calculate extends Service {

    String originalImagePath;
    Uri originalImageUri;

    public Calculate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isPath = intent.getBooleanExtra("isPath", true);
        if (isPath) {
            originalImagePath = intent.getStringExtra("original_image_path");
        } else {
            originalImageUri = Uri.parse(intent.getStringExtra("original_image_uri"));
        }

        // 接收传递过来的图像路径
        String croppedImagePath = intent.getStringExtra("cropped_image_path");
        if (croppedImagePath != null) {
            // 根据路径加载图像
            Bitmap bitmap = BitmapFactory.decodeFile(croppedImagePath);

            if (bitmap != null) {
                // 计算灰度直方图
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                // 初始化灰度直方图数组
                int[] histogram = new int[256];

                // 遍历图像像素
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = bitmap.getPixel(x, y);
                        int red = (pixel >> 16) & 0xFF;
                        histogram[red]++;
                    }
                }

                float darkness = 0;
                for (int i = 1; i < 256; i++) {
                    darkness += histogram[i] * (256 - i);
                    Log.i("HISTOGRAM", "histogram[" + i + "] = " + histogram[i]);
                }
                darkness /= (float)(width * height - histogram[0]); // 计算平均灰度值

                Log.i("DARKNESS", "darkness" + darkness);
                Log.i("DARKNESS", "width" + width);
                Log.i("DARKNESS", "height" + height);

                // 创建Intent启动ShowActivity，并传递计算结果
                Intent showIntent = new Intent(this, ShowActivity.class);
                showIntent.putExtra("darkness_value", darkness);

                showIntent.putExtra("isPath", isPath);
                if (isPath) {
                    showIntent.putExtra("original_image_path", originalImagePath);
                } else {
                    showIntent.putExtra("original_image_uri", originalImageUri);
                }
                showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(showIntent);
            }
        }
        // 如果我们超出工作，不需要重新启动
        return START_NOT_STICKY;
    }
}
