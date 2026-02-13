package com.ecoshare.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.ConversationsAdapter;
import com.ecoshare.app.models.Conversation;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView conversationsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseHelper firebaseHelper;
    private FirebaseAuth mAuth;
    private ConversationsAdapter adapter;
    private List<Conversation> conversationsList;
    private ListenerRegistration conversationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupRecyclerView();
        loadConversations();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();
        conversationsList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ConversationsAdapter(this, conversationsList, conversation -> {
            Intent intent = new Intent(ChatListActivity.this, ChatRoomActivity.class);
            intent.putExtra("conversationId", conversation.getConversationId());
            intent.putExtra("otherParticipantId", conversation.getOtherParticipantId(mAuth.getUid()));
            intent.putExtra("otherParticipantName", conversation.getOtherParticipantName(mAuth.getUid()));
            startActivity(intent);
        });
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadConversations);
    }

    private void loadConversations() {
        if (mAuth.getCurrentUser() == null) return;

        showLoading(true);
        if (conversationsListener != null) {
            conversationsListener.remove();
        }

        String currentUserId = mAuth.getUid();
        conversationsListener = firebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_CONVERSATIONS)
                .whereArrayContains("participantIds", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        conversationsList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Conversation conversation = doc.toObject(Conversation.class);
                            if (conversation != null) {
                                conversation.setConversationId(doc.getId());
                                conversationsList.add(conversation);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (conversationsList.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                            conversationsRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setVisibility(View.GONE);
                            conversationsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conversationsListener != null) {
            conversationsListener.remove();
        }
    }
}
