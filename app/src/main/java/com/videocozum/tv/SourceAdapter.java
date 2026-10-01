package com.videocozum.tv;

import android.graphics.Typeface;
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

        if (item.type == SourceItem.TYPE_QUESTION) {
            // Soru satırında oynat ikonu göster
            holder.imgAction.setImageResource(R.drawable.ic_play_circle);
        } else {
            // Kategori veya Test satırında sağ ok ikonu göster
            holder.imgAction.setImageResource(R.drawable.ic_chevron_right);
        }

        // TV Kumandası odaklanma efekti (Turuncu Çerçeve & Canlı Metin & Büyütme)
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.txtName.setTextColor(0xFFD84315); // Turuncu-kırmızı vurgu
                holder.txtName.setTypeface(null, Typeface.BOLD);
                holder.itemView.animate().scaleX(1.025f).scaleY(1.025f).setDuration(120).start();
                holder.itemView.setElevation(8f);
            } else {
                holder.txtName.setTextColor(0xFF374151); // Normal koyu gri
                holder.txtName.setTypeface(null, Typeface.NORMAL);
                holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                holder.itemView.setElevation(0f);
            }
        });

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
