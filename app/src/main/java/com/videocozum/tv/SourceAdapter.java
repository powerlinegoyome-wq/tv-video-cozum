package com.videocozum.tv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SourceItem item, int position);
    }

    private List<SourceItem> originalList = new ArrayList<>();
    private List<SourceItem> displayList = new ArrayList<>();
    private OnItemClickListener listener;

    public SourceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<SourceItem> items) {
        this.originalList = new ArrayList<>(items);
        this.displayList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String lower = query.toLowerCase().trim();
            for (SourceItem item : originalList) {
                if (item.name != null && item.name.toLowerCase().contains(lower)) {
                    displayList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SourceItem item = displayList.get(position);
        holder.txtName.setText(item.name);

        if (item.isParent) {
            holder.imgAction.setImageResource(R.drawable.ic_chevron_right);
        } else {
            // Soru satırında oynat ikonu göster (Ekran görüntüsü 6 gibi)
            holder.imgAction.setImageResource(R.drawable.ic_play_circle);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_item_name);
            imgAction = itemView.findViewById(R.id.img_item_action);
        }
    }
}
