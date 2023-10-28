package com.jlu.drsmog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
