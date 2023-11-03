package com.jlu.drsmog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jlu.drsmog.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Record> records;

    public HistoryAdapter(List<Record> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Record record = records.get(position);
        holder.textViewTime.setText(record.getTime());
        holder.textViewBlackness.setText(record.getBlackness());
        holder.textViewPath.setText(record.getPath());
    }
    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTime;
        TextView textViewBlackness;
        TextView textViewPath;

        public ViewHolder(View view) {
            super(view);
            textViewTime = view.findViewById(R.id.textView_time);
            textViewBlackness = view.findViewById(R.id.textView_blackness);
            textViewPath = view.findViewById(R.id.textView_path); // 注意这里ID仍然是imageView_path，尽管它是TextView
        }
    }
}
