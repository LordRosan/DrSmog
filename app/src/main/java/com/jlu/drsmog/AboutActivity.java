package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageButton imageButton = findViewById(R.id.btn_back);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个意图(Intent)来启动MainActivity
                Intent intent = new Intent(AboutActivity.this, MainActivity.class);

                // 为了确保MainActivity不被重复创建，我们设置以下标志
                // 这将使MainActivity成为任务栈的顶部，并清除其他所有活动
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // 使用意图启动活动
                startActivity(intent);

                // 如果你想关闭当前活动(例如，如果当前活动是一个子活动或一个次要的界面)，可以添加以下行
                finish();
            }
        });
    }
}