package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView = findViewById(R.id.textView);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        textView.startAnimation(scaleUp);

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
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // 当窗口获得焦点时执行的代码
            TextView textView = findViewById(R.id.textView);

            int startColor = Color.parseColor("#FF0000");  // 从XML中的渐变定义提取的颜色值
            int endColor = Color.parseColor("#0000FF");

            // 创建着色器
            Shader textShader = new LinearGradient(0, 0, textView.getWidth(), textView.getHeight(),
                    startColor,
                    endColor,
                    Shader.TileMode.CLAMP);

            // 将着色器应用于TextView的文字
            textView.getPaint().setShader(textShader);
        }
    }


}