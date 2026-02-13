package com.ecoshare.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private Context context;
    private List<User> users;

    public LeaderboardAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, position + 1);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        TextView rankTextView;
        CircleImageView userImageView;
        TextView userNameTextView;
        TextView impactTextView;
        TextView pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            userImageView = itemView.findViewById(R.id.userImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            impactTextView = itemView.findViewById(R.id.impactTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
        }

        public void bind(User user, int rank) {
            rankTextView.setText(String.valueOf(rank));
            userNameTextView.setText(user.getFullName());
            impactTextView.setText(user.getItemsDonated() + " items donated");
            pointsTextView.setText((user.getItemsDonated() * 50 + user.getItemsListed() * 10) + " pts");

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.ic_notification)
                        .into(userImageView);
            } else {
                userImageView.setImageResource(R.drawable.ic_notification);
            }

            // Highlight top 3
            if (rank == 1) {
                rankTextView.setTextColor(context.getResources().getColor(R.color.primary));
                rankTextView.setTextSize(24);
            } else if (rank == 2) {
                rankTextView.setTextColor(context.getResources().getColor(R.color.primary));
                rankTextView.setAlpha(0.8f);
            } else if (rank == 3) {
                rankTextView.setTextColor(context.getResources().getColor(R.color.primary));
                rankTextView.setAlpha(0.6f);
            } else {
                rankTextView.setTextColor(context.getResources().getColor(R.color.text_secondary));
                rankTextView.setTextSize(18);
            }
        }
    }
}
