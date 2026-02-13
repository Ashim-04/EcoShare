package com.ecoshare.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.models.Badge;
import com.ecoshare.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private Context context;
    private List<Badge> badges;

    public BadgeAdapter(Context context) {
        this.context = context;
        this.badges = new ArrayList<>();
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges != null ? badges : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView badgeIcon;
        private TextView badgeName;
        private TextView badgeDescription;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeIcon = itemView.findViewById(R.id.badgeIcon);
            badgeName = itemView.findViewById(R.id.badgeName);
            badgeDescription = itemView.findViewById(R.id.badgeDescription);
        }

        public void bind(Badge badge) {
            badgeName.setText(badge.getName());
            badgeDescription.setText(badge.getDescription());
            
            int iconColor = getBadgeColor(badge.getBadgeId());
            badgeIcon.setColorFilter(iconColor);
        }

        private int getBadgeColor(String badgeId) {
            Context context = itemView.getContext();
            
            if (Constants.BADGE_50_DONATIONS.equals(badgeId) || 
                Constants.BADGE_ECO_WARRIOR.equals(badgeId)) {
                return context.getResources().getColor(R.color.badgeGold, null);
            } else if (Constants.BADGE_25_DONATIONS.equals(badgeId) || 
                       Constants.BADGE_AUCTION_MASTER.equals(badgeId)) {
                return context.getResources().getColor(R.color.badgeSilver, null);
            } else {
                return context.getResources().getColor(R.color.badgeBronze, null);
            }
        }
    }
}
