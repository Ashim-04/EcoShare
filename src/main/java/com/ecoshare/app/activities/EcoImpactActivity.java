package com.ecoshare.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecoshare.app.R;
import com.ecoshare.app.models.User;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;

import java.util.Locale;

public class EcoImpactActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView itemsReusedTextView;
    private TextView co2SavedTextView;
    private TextView treesEquivalentTextView;
    private TextView rankTextView;
    private TextView totalCommunityCO2TextView;
    private TextView totalCommunityTreesTextView;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_impact);

        initializeViews();
        initializeHelpers();
        setupToolbar();
        loadUserData();
        loadCommunityData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        itemsReusedTextView = findViewById(R.id.itemsReusedTextView);
        co2SavedTextView = findViewById(R.id.co2SavedTextView);
        treesEquivalentTextView = findViewById(R.id.treesEquivalentTextView);
        rankTextView = findViewById(R.id.rankTextView);
        totalCommunityCO2TextView = findViewById(R.id.totalCommunityCO2TextView);
        totalCommunityTreesTextView = findViewById(R.id.totalCommunityTreesTextView);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String userId = firebaseHelper.getCurrentUserId();
        if (userId == null) return;

        firebaseHelper.getUsersCollection().document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            displayUserImpact(user);
                        }
                    }
                });
    }

    private void displayUserImpact(User user) {
        int itemsDonated = user.getItemsDonated();
        double co2Saved = itemsDonated * Constants.CO2_PER_ITEM_KG;
        double treesEquivalent = co2Saved / 20.0; // Assume 1 tree absorbs 20kg CO2 per year

        itemsReusedTextView.setText(String.valueOf(itemsDonated));
        co2SavedTextView.setText(String.format(Locale.getDefault(), "%.1f kg", co2Saved));
        treesEquivalentTextView.setText(String.format(Locale.getDefault(), "%.1f", treesEquivalent));
        
        // In a real app, we would calculate rank here or fetch it from a leaderboard
        rankTextView.setText("Ranked!");
    }

    private void loadCommunityData() {
        // Calculate total items donated by all users
        firebaseHelper.getUsersCollection()
                .aggregate(com.google.firebase.firestore.AggregateField.sum("itemsDonated"))
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(aggregateQuerySnapshot -> {
                    long totalDonated = aggregateQuerySnapshot.get(com.google.firebase.firestore.AggregateField.sum("itemsDonated")).longValue();
                    displayCommunityImpact(totalDonated);
                })
                .addOnFailureListener(e -> {
                    // Fallback to simpler method if aggregate fails
                    Toast.makeText(this, "Error loading community data", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayCommunityImpact(long totalItems) {
        double totalCO2 = totalItems * Constants.CO2_PER_ITEM_KG;
        long totalTrees = (long) (totalCO2 / 20.0);

        totalCommunityCO2TextView.setText(String.format(Locale.getDefault(), "%,.0f kg", totalCO2));
        totalCommunityTreesTextView.setText(String.format(Locale.getDefault(), "Equivalent to %,d trees planted!", totalTrees));
    }
}
