package com.ecoshare.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.ChatMessagesAdapter;
import com.ecoshare.app.models.ChatMessage;
import com.ecoshare.app.models.Conversation;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private FloatingActionButton sendButton;

    private FirebaseHelper firebaseHelper;
    private FirebaseAuth mAuth;
    private ChatMessagesAdapter adapter;
    private List<ChatMessage> messagesList;
    private ListenerRegistration messagesListener;

    private String conversationId;
    private String otherParticipantId;
    private String otherParticipantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        conversationId = getIntent().getStringExtra("conversationId");
        otherParticipantId = getIntent().getStringExtra("otherParticipantId");
        otherParticipantName = getIntent().getStringExtra("otherParticipantName");

        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupRecyclerView();
        
        if (conversationId == null && otherParticipantId != null) {
            checkExistingConversation();
        } else if (conversationId != null) {
            loadMessages();
            markMessagesAsRead();
            resetUnreadCount();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();
        messagesList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherParticipantName != null ? otherParticipantName : "Chat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ChatMessagesAdapter(this, messagesList, mAuth.getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void checkExistingConversation() {
        String currentUserId = mAuth.getUid();
        firebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_CONVERSATIONS)
                .whereArrayContains("participantIds", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participantIds");
                        if (participants != null && participants.contains(otherParticipantId)) {
                            conversationId = doc.getId();
                            loadMessages();
                            markMessagesAsRead();
                            resetUnreadCount();
                            return;
                        }
                    }
                });
    }

    private void loadMessages() {
        if (conversationId == null) return;

        if (messagesListener != null) {
            messagesListener.remove();
        }

        messagesListener = firebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshots != null) {
                        int oldSize = messagesList.size();
                        messagesList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null) {
                                message.setMessageId(doc.getId());
                                messagesList.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (messagesList.size() > oldSize) {
                            chatRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                        }
                        markMessagesAsRead();
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        messageEditText.setText("");
        String currentUserId = mAuth.getUid();
        ChatMessage message = new ChatMessage(currentUserId, otherParticipantId, messageText);

        if (conversationId == null) {
            createNewConversationAndSendMessage(message);
        } else {
            appendMessageToConversation(message);
        }
    }

    private void createNewConversationAndSendMessage(ChatMessage message) {
        String currentUserId = mAuth.getUid();
        DocumentReference convRef = firebaseHelper.getFirestore().collection(Constants.COLLECTION_CONVERSATIONS).document();
        conversationId = convRef.getId();

        Map<String, Object> conversation = new HashMap<>();
        conversation.put("participantIds", Arrays.asList(currentUserId, otherParticipantId));
        conversation.put("lastMessage", message.getMessage());
        conversation.put("lastMessageTimestamp", message.getTimestamp());
        
        Map<String, String> names = new HashMap<>();
        names.put(currentUserId, PrefsManager.getInstance().getUserName());
        names.put(otherParticipantId, otherParticipantName);
        conversation.put("participantNames", names);

        Map<String, String> profiles = new HashMap<>();
        profiles.put(currentUserId, PrefsManager.getInstance().getProfileImage());
        // We might not have the other participant's profile image here, 
        // ideally we should fetch it or pass it. For now, we'll leave it empty.
        conversation.put("participantProfiles", profiles);
        
        Map<String, Integer> unreadCounts = new HashMap<>();
        unreadCounts.put(currentUserId, 0);
        unreadCounts.put(otherParticipantId, 0);
        conversation.put("unreadCounts", unreadCounts);

        convRef.set(conversation).addOnSuccessListener(aVoid -> {
            appendMessageToConversation(message);
            loadMessages();
        });
    }

    private void appendMessageToConversation(ChatMessage message) {
        DocumentReference convRef = firebaseHelper.getFirestore().collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId);
        
        WriteBatch batch = firebaseHelper.getFirestore().batch();
        
        DocumentReference msgRef = convRef.collection(Constants.COLLECTION_MESSAGES).document();
        batch.set(msgRef, message);
        
        batch.update(convRef, "lastMessage", message.getMessage());
        batch.update(convRef, "lastMessageTimestamp", message.getTimestamp());
        
        String recipientKey = "unreadCounts." + message.getReceiverId();
        batch.update(convRef, recipientKey, FieldValue.increment(1));
        
        batch.commit().addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        });
    }

    private void markMessagesAsRead() {
        if (conversationId == null) return;
        
        String currentUserId = mAuth.getUid();
        firebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(Constants.COLLECTION_MESSAGES)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        WriteBatch batch = firebaseHelper.getFirestore().batch();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            batch.update(doc.getReference(), "isRead", true);
                        }
                        batch.commit();
                    }
                });
    }
    
    private void resetUnreadCount() {
        if (conversationId == null) return;
        
        String currentUserId = mAuth.getUid();
        String unreadKey = "unreadCounts." + currentUserId;
        
        firebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(unreadKey, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}
