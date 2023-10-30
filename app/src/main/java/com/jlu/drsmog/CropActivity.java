package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Stack;

public class CropActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap originalImage;
    private Bitmap currentImage;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private Path currentPath;
    private Paint pathPaint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageView = findViewById(R.id.imageView);
        ImageButton BackButton = findViewById(R.id.back_image_button);
        ImageButton UndoButton = findViewById(R.id.undo_image_button);
        ImageButton RedoButton = findViewById(R.id.redo_image_button);
        ImageButton NextButton = findViewById(R.id.next_image_button);

        //获取从CamActivity传递进来的图片
        //originalImage = getIntent().getParcelableExtra("image");

        originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        currentImage = originalImage.copy(originalImage.getConfig(), true);
        if (originalImage != null) {
            currentImage = originalImage.copy(originalImage.getConfig(), true);
            imageView.setImageBitmap(currentImage);  // 确保将Bitmap设置到ImageView中
        } else {
            Toast.makeText(this, "无法加载图像", Toast.LENGTH_SHORT).show();
            finish();  // 关闭当前的Activity
        }

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(5);
        pathPaint.setColor(Color.RED);

        //返回按钮的具体实现
        BackButton.setOnClickListener(v -> {
            Intent intent = new Intent(CropActivity.this, CamActivity.class);
            startActivity(intent);
            finish();
        });

        //向前撤销按钮的具体实现
        UndoButton.setOnClickListener(v -> {
            if (!undoStack.isEmpty()) {
                redoStack.push(currentImage);
                currentImage = undoStack.pop();
                imageView.setImageBitmap(currentImage);
            }
        });

        //向后撤销按钮的具体实现
        RedoButton.setOnClickListener(v -> {
            if (!redoStack.isEmpty()) {
                undoStack.push(currentImage);
                currentImage = redoStack.pop();
                imageView.setImageBitmap(currentImage);
            }
        });

        //下一步按钮的具体实现
        NextButton.setOnClickListener(v -> {
            Intent intent = new Intent(CropActivity.this, ShowActivity.class);
            intent.putExtra("cropped_image", currentImage);
            startActivity(intent);
        });

        //自由裁切的具体实现
        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentPath = new Path();
                    currentPath.moveTo(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentPath.lineTo(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    applyCrop(currentPath);
                    break;
            }
            redrawImage();
            return true;
        });
    }

    private void redrawImage() {
        Bitmap tempBitmap = currentImage.copy(currentImage.getConfig(), true);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawPath(currentPath, pathPaint);
        imageView.setImageBitmap(tempBitmap);
    }

    private void applyCrop(Path path) {
        undoStack.push(currentImage);
        redoStack.clear();

        Bitmap resultBitmap = Bitmap.createBitmap(currentImage.getWidth(), currentImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        Paint pathFillPaint = new Paint();
        pathFillPaint.setAntiAlias(true);
        pathFillPaint.setColor(Color.WHITE);
        pathFillPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, pathFillPaint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(currentImage, 0, 0, paint);

        currentImage = resultBitmap;
        imageView.setImageBitmap(currentImage);
    }
}