package com.jlu.drsmog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jlu.drsmog.adapters.HistoryAdapter;
import com.jlu.drsmog.database.DatabaseHelper;
import com.jlu.drsmog.adapters.Record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private List<Record> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ImageButton imageButton = findViewById(R.id.btn_back);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        dbHelper = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadData();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {}
            @Override
            public void onLongClick(View view, int position) {
                showDeleteDialog(position);
            }
        }));
        ImageButton shareButton = findViewById(R.id.btn_more);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行截图并分享的代码
                takeScreenshotAndShare();
            }
        });

    }

    private void loadData() {
        records = dbHelper.getAllData();
        if (records == null || records.isEmpty()) {
            Log.d("HistoryActivity", "No data fetched from the database.");
        } else {
            Log.d("HistoryActivity", "Fetched " + records.size() + " records from the database.");
        }
        adapter = new HistoryAdapter(records);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除条目");
        builder.setMessage("您确定要删除此条目吗?");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 显式调用dismiss
                Record record = records.get(position);
                dbHelper.deleteData(record.getId());
                loadData();
                Log.d("HistoryActivity", "Item deleted and data reloaded");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 显式调用cancel
            }
        });
        builder.show();
    }


    private void takeScreenshotAndShare() {
        try {
            // 1. 获取屏幕截图
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);

            // 2. 保存Bitmap到文件
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File screenshotFile = new File(cachePath, "screenshot.png");

            try (FileOutputStream stream = new FileOutputStream(screenshotFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }

            // 3. 调用分享Intent
            Uri contentUri = FileProvider.getUriForFile(this, "com.jlu.drsmog.provider", screenshotFile);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "分享截图失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void showImageDialog(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image, null);
        ImageView imageView = dialogView.findViewById(R.id.dialog_imageview);

        // Set the image on the ImageView
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        } else {
            Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        builder.setView(dialogView);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
