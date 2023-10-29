package com.jlu.drsmog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.jlu.drsmog.database.DatabaseHelper;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseHelper dbHelper;
    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history); // 请确保这是你的layout文件名
        ImageButton imageButton = findViewById(R.id.btn_back);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个意图(Intent)来启动MainActivity
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);

                // 为了确保MainActivity不被重复创建，我们设置以下标志
                // 这将使MainActivity成为任务栈的顶部，并清除其他所有活动
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // 使用意图启动活动
                startActivity(intent);

                // 如果你想关闭当前活动(例如，如果当前活动是一个子活动或一个次要的界面)，可以添加以下行
                finish();
            }
        });

        listView = findViewById(R.id.listView_history);
        dbHelper = new DatabaseHelper(this);
        loadData();
    }

    private void loadData() {
        data = dbHelper.getAllData();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // 这是一个简单的列表项布局，你可以自定义
                data
        );
        listView.setAdapter(adapter);
    }
}
