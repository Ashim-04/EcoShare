package com.ecoshare.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.models.Item;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.GlideHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {
    
    private Context context;
    private List<Item> items;
    private List<Item> itemsFiltered;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }
    
    public ItemsAdapter(Context context, List<Item> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.itemsFiltered = new ArrayList<>(items);
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed_card, parent, false);
        return new ItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemsFiltered.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }
    
    public void updateItems(List<Item> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        this.itemsFiltered.clear();
        this.itemsFiltered.addAll(newItems);
        notifyDataSetChanged();
    }
    
    public void filterByCategory(String category) {
        itemsFiltered.clear();
        
        if (category == null || category.equals("All")) {
            itemsFiltered.addAll(items);
        } else {
            for (Item item : items) {
                if (category.equalsIgnoreCase(item.getCategory())) {
                    itemsFiltered.add(item);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    class ItemViewHolder extends RecyclerView.ViewHolder {
        
        MaterialCardView itemCard;
        ImageView itemImageView;
        TextView itemTitleTextView;
        TextView itemDescriptionTextView;
        Chip categoryChip;
        Chip typeChip;
        TextView conditionTextView;
        TextView ownerNameTextView;
        TextView timeTextView;
        
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            itemCard = itemView.findViewById(R.id.itemCard);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            typeChip = itemView.findViewById(R.id.typeChip);
            conditionTextView = itemView.findViewById(R.id.conditionTextView);
            ownerNameTextView = itemView.findViewById(R.id.ownerNameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
        
        public void bind(Item item) {
            itemTitleTextView.setText(item.getTitle());
            itemDescriptionTextView.setText(item.getDescription());
            categoryChip.setText(item.getCategory());
            conditionTextView.setText(item.getCondition());
            ownerNameTextView.setText(item.getOwnerName());
            
            if (item.getType() != null) {
                typeChip.setText(capitalizeFirst(item.getType()));
                setTypeChipColor(typeChip, item.getType());
            }
            
            if (item.getFirstImageUrl() != null) {
                GlideHelper.loadImageWithCenterCrop(context, item.getFirstImageUrl(), itemImageView);
            } else {
                itemImageView.setImageResource(R.drawable.ic_notification);
            }
            
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                item.getCreatedTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            );
            timeTextView.setText(timeAgo);
            
            itemCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
        
        private void setTypeChipColor(Chip chip, String type) {
            int color;
            if (Constants.ITEM_TYPE_DONATE.equalsIgnoreCase(type)) {
                color = Color.parseColor("#4CAF50");
            } else if (Constants.ITEM_TYPE_EXCHANGE.equalsIgnoreCase(type)) {
                color = Color.parseColor("#2196F3");
            } else if (Constants.ITEM_TYPE_AUCTION.equalsIgnoreCase(type)) {
                color = Color.parseColor("#FF9800");
            } else {
                color = Color.parseColor("#9E9E9E");
            }
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
            chip.setTextColor(Color.WHITE);
        }
        
        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}
