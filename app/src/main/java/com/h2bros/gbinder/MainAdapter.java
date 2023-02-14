package com.h2bros.gbinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.h2bros.annotation.BindView;
import com.h2bros.annotation.OnClick;
import com.h2bros.gbinder.databinding.ItemListViewBinding;
import com.h2bros.reflection.ButterKnife;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.VH> {

    private List<String> data;

    public MainAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tagTv.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        @BindView(R.id.tag_tv)
        TextView tagTv;

        @OnClick(R.id.tag_tv)
        void onClick() {
            Toast.makeText(itemView.getContext(), "Click", Toast.LENGTH_SHORT).show();
        }

        public VH(@NonNull View itemView) {
            super(itemView);
            ItemListViewBinding binding = ItemListViewBinding.bind(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
