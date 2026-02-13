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

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.models.Conversation;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    private Context context;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;
    private String currentUserId;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationsAdapter(Context context, List<Conversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        
        holder.nameTextView.setText(conversation.getOtherParticipantName(currentUserId));
        holder.lastMessageTextView.setText(conversation.getLastMessage());
        
        String timeStr = DateUtils.getRelativeTimeSpanString(conversation.getLastMessageTimestamp()).toString();
        holder.timeTextView.setText(timeStr);

        String profileUrl = conversation.getOtherParticipantProfile(currentUserId);
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(context).load(profileUrl).placeholder(android.R.drawable.ic_menu_myplaces).into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        if (conversation.getUnreadCounts() != null && conversation.getUnreadCounts().containsKey(currentUserId)) {
            int unreadCount = conversation.getUnreadCounts().get(currentUserId);
            if (unreadCount > 0) {
                holder.unreadBadge.setVisibility(View.VISIBLE);
                holder.unreadBadge.setText(String.valueOf(unreadCount));
            } else {
                holder.unreadBadge.setVisibility(View.GONE);
            }
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView timeTextView;
        TextView unreadBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
        }
    }
}
