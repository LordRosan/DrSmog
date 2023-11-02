package com.jlu.drsmog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jlu.drsmog.adapters.HistoryAdapter;
import com.jlu.drsmog.database.DatabaseHelper;
import com.jlu.drsmog.adapters.Record;

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
        // 初始化数据库
        dbHelper = new DatabaseHelper(this);
        // 插入测试数据
        dbHelper.addData("2023-10-28 12:00:00", "SomeBlacknessValue", "/some/path/to/data");
        // 初始化 RecyclerView
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
                Record record = records.get(position);
                dbHelper.deleteData(record.getId());
                loadData();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
