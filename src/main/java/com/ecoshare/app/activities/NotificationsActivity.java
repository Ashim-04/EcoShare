package com.ecoshare.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.NotificationsAdapter;
import com.ecoshare.app.models.Notification;
import com.ecoshare.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnNotificationClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView notificationsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyTextView;
    private ProgressBar progressBar;

    private NotificationsAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        fetchNotifications();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(this, notificationList, this);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchNotifications);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void fetchNotifications() {
        if (firebaseHelper.getCurrentUserId() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        firebaseHelper.getNotificationsCollection()
                .whereEqualTo("userId", firebaseHelper.getCurrentUserId())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setNotificationId(doc.getId());
                            notificationList.add(notification);
                        }
                    }
                    adapter.updateNotifications(notificationList);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (notificationList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            markAllAsRead();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void markAllAsRead() {
        if (notificationList.isEmpty()) return;

        WriteBatch batch = firebaseHelper.getFirestore().batch();
        boolean hasUnread = false;

        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                batch.update(firebaseHelper.getNotificationsCollection().document(notification.getNotificationId()), "isRead", true);
                notification.setRead(true);
                hasUnread = true;
            }
        }

        if (hasUnread) {
            batch.commit().addOnSuccessListener(aVoid -> {
                adapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.mark_all_read, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            firebaseHelper.getNotificationsCollection()
                    .document(notification.getNotificationId())
                    .update("isRead", true);
            notification.setRead(true);
            adapter.notifyDataSetChanged();
        }

        // Navigate based on type
        Intent intent = null;
        if ("outbid".equals(notification.getType()) || "auction_won".equals(notification.getType()) || "auction_lost".equals(notification.getType())) {
            if (notification.getItemId() != null) {
                intent = new Intent(this, LiveAuctionActivity.class);
                intent.putExtra("ITEM_ID", notification.getItemId());
            }
        } else if ("badge_earned".equals(notification.getType())) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (notification.getItemId() != null) {
            intent = new Intent(this, ItemDetailsActivity.class);
            intent.putExtra("ITEM_ID", notification.getItemId());
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
