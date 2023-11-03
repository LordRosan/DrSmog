package com.jlu.drsmog;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jlu.drsmog.database.DatabaseHelper;

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
    final int READ_REQUEST_CODE = 200;

    boolean isPath;
    String imagePath;
    Uri imageUri;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] != PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission is required!", Toast.LENGTH_LONG).show();
                } else {
                    GetImage();
                }
                break;
            default:
        }
    }

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

        GetImage();

        // 显示裁剪后的图片在ImageView界面上
        iv1.setImageBitmap(currentImage);
        float darkness = getIntent().getFloatExtra("darkness_value", 0); // 0为默认值
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
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(ShowActivity.this);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentTime = dateFormat.format(calendar.getTime());
                if (!isPath) {
                    imagePath = imageUri.toString();
                }

                // 弹出对话框，让用户输入保存文件的名称
                final EditText editText = new EditText(ShowActivity.this);
                new AlertDialog.Builder(ShowActivity.this)
                        .setTitle(R.string.save_dialog_title)
                        .setView(editText)
                        .setPositiveButton(R.string.save_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = editText.getText().toString();
                                // 将文件保存在设备上
                                dbHelper.addData(currentTime, String.valueOf(darkness), imagePath);
                                Toast.makeText(ShowActivity.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.save_dialog_cancel, null)
                        .show();
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText); // 设置分享的主题或标题
        if (shareImageBitmap != null) {
            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), shareImageBitmap, "ShareImage", null);
            Uri bitmapUri = Uri.parse(bitmapPath);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        }
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void GetImage() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
            return;
        }

        Intent intent = getIntent();
        isPath = intent.getBooleanExtra("isPath", false);

        if (isPath) {imagePath = intent.getStringExtra("original_image_path");
            if (imagePath != null) {
                currentImage = BitmapFactory.decodeFile(imagePath);
            }
        } else {
            imageUri = Uri.parse(intent.getStringExtra("original_image_uri"));
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
    }
}