package com.jlu.drsmog;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

public class CropActivity extends AppCompatActivity {

    final int READ_REQUEST_CODE = 200;
    private ImageView imageView;
    private Bitmap originalImage;
    private Bitmap currentImage;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private Path currentPath;
    private Paint pathPaint;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] != PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission is required!", Toast.LENGTH_LONG).show();
                } else {
                    GetImagePath();
                }
                break;
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageView = findViewById(R.id.imageView);
        ImageButton BackButton = findViewById(R.id.back_image_button);
        ImageButton UndoButton = findViewById(R.id.undo_image_button);
        ImageButton RedoButton = findViewById(R.id.redo_image_button);
        ImageButton NextButton = findViewById(R.id.next_image_button);

        GetImagePath();
        currentImage = originalImage.copy(originalImage.getConfig(), true);

        if (originalImage != null) {
            currentImage = originalImage.copy(originalImage.getConfig(), true);
            imageView.setImageBitmap(currentImage);  // 确保将Bitmap设置到ImageView中
        } else {
            Toast.makeText(this, "无法加载图像", Toast.LENGTH_SHORT).show();
            Intent intent_back = new Intent(CropActivity.this, CamActivity.class);
            startActivity(intent_back);
            finish();  // 关闭当前的Activity
        }

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(5);
        pathPaint.setColor(Color.RED);

        //返回按钮的具体实现
        BackButton.setOnClickListener(v -> {
            Intent intent_back = new Intent(CropActivity.this, CamActivity.class);
            startActivity(intent_back);
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
            File croppedImageFile = saveBitmapToFile(currentImage);
            if (croppedImageFile != null) {
                Intent calculateIntent = new Intent(this, Calculate.class);
                calculateIntent.putExtra("cropped_image_path", croppedImageFile.getAbsolutePath());
                startService(calculateIntent);
            }
        });

        //自由裁切的具体实现
        imageView.setOnTouchListener((v, event) -> {
            float[] realCoords = getBitmapPositionInsideImageView(imageView, event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentPath = new Path();
                    currentPath.moveTo(realCoords[0], realCoords[1]);
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentPath.lineTo(realCoords[0], realCoords[1]);
                    break;
                case MotionEvent.ACTION_UP:
                    new ApplyCropTask().execute(currentPath);
                    break;
            }
            redrawImage();
            return true;
        });
    }

    private float[] getBitmapPositionInsideImageView(ImageView imageView, MotionEvent event) {
        float[] ret = new float[2];
        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        int imageWidth = imageView.getDrawable().getIntrinsicWidth();
        int imageHeight = imageView.getDrawable().getIntrinsicHeight();

        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Calculate the real image coordinates
        ret[0] = (event.getX() - f[Matrix.MTRANS_X]) / scaleX;
        ret[1] = (event.getY() - f[Matrix.MTRANS_Y]) / scaleY;

        return ret;
    }


    private void redrawImage() {
        Bitmap tempBitmap = currentImage.copy(currentImage.getConfig(), true);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawPath(currentPath, pathPaint);
        imageView.setImageBitmap(tempBitmap);
    }

    private class ApplyCropTask extends AsyncTask<Path, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Path... paths) {
            Path path = paths[0];
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

            return resultBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {
            undoStack.push(currentImage);
            redoStack.clear();
            currentImage = resultBitmap;
            imageView.setImageBitmap(currentImage);
        }
    }

    private void GetImagePath() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
            return;
        }

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("image_path");
        if (imagePath != null) {
            originalImage = BitmapFactory.decodeFile(imagePath);
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File outputDir = getCacheDir();
            File outputFile = File.createTempFile("cropped_image", ".png", outputDir);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}