package com.jlu.drsmog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.jlu.drsmog.database.DatabaseHelper;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 执行数据库查询操作
        //List<String> data = getDataFromDatabase(); // 假设这是从数据库中获取数据的方法

        // 获取对显示数据的 TextView 引用
        TextView dataTextView = findViewById(R.id.dataTextView);

        // 将数据显示在布局中
        /*if (!data.isEmpty()) {
            StringBuilder dataString = new StringBuilder();
            for (String item : data) {
                dataString.append(item).append("\n");
            }
            dataTextView.setText(dataString.toString());
        } else {
            dataTextView.setText("No data found.");
        }
    }
    /*private List<String> getDataFromDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        return dbHelper.getData();
    }*/
}}