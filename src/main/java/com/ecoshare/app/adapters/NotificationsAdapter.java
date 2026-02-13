package com.ecoshare.app.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.models.Notification;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationsAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView notificationCard;
        ImageView notificationIcon;
        TextView notificationTitle;
        TextView notificationMessage;
        TextView notificationTime;
        View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationCard = itemView.findViewById(R.id.notificationCard);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(Notification notification) {
            notificationTitle.setText(notification.getTitle());
            notificationMessage.setText(notification.getMessage());

            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            );
            notificationTime.setText(timeAgo);

            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set icon based on type
            setNotificationIcon(notification.getType());

            notificationCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private void setNotificationIcon(String type) {
            // Default icon for now
            notificationIcon.setImageResource(R.drawable.ic_notification);
            
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "outbid":
                        // Could use a specific icon if available
                        break;
                    case "auction_won":
                        // Could use a specific icon if available
                        break;
                    case "badge_earned":
                        // Could use a specific icon if available
                        break;
                }
            }
        }
    }
}
