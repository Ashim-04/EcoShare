package com.ecoshare.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.LeaderboardAdapter;
import com.ecoshare.app.models.User;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView leaderboardRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private LeaderboardAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupRecyclerView();
        loadLeaderboardData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        userList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(this, userList);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(adapter);
    }

    private void loadLeaderboardData() {
        showLoading(true);
        firebaseHelper.getUsersCollection()
                .orderBy("itemsDonated", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);
                    userList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                    }
                    adapter.updateUsers(userList);
                    
                    if (userList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        leaderboardRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                        leaderboardRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
