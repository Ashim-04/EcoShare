package com.ecoshare.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.ItemsAdapter;
import com.ecoshare.app.models.Item;
import com.ecoshare.app.utils.AnimationUtil;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.NetworkUtil;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView itemsRecyclerView;
    private TextView emptyTextView;
    private FloatingActionButton addItemFab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private ChipGroup categoryChipGroup;
    private BottomNavigationView bottomNavigation;

    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;
    private ItemsAdapter itemsAdapter;
    private List<Item> itemsList;
    private ListenerRegistration itemsListener;
    private String selectedCategory = "All";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadItems();
        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        addItemFab = findViewById(R.id.addItemFab);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance();
        itemsList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        itemsAdapter = new ItemsAdapter(this, itemsList, item -> {
            Intent intent = new Intent(MainActivity.this, ItemDetailsActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });
        
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemsAdapter);
    }

    private void setupListeners() {
        addItemFab.setOnClickListener(v -> {
            AnimationUtil.pulse(v);
            startActivity(new Intent(this, AddItemActivity.class));
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadItems);

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "All";
                itemsAdapter.filterByCategory("All");
                return;
            }

            int checkedId = checkedIds.get(0);
            Chip selectedChip = findViewById(checkedId);
            
            if (selectedChip != null) {
                selectedCategory = selectedChip.getText().toString();
                itemsAdapter.filterByCategory(selectedCategory);
            }
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(this, ChatListActivity.class));
                return true;
            } else if (itemId == R.id.nav_impact) {
                startActivity(new Intent(this, EcoImpactActivity.class));
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void loadItems() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        if (itemsListener != null) {
            itemsListener.remove();
        }

        itemsListener = firebaseHelper.getFirestore()
            .collection("items")
            .orderBy("createdTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (error != null) {
                    Toast.makeText(this, "Error loading items: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    itemsList.clear();
                    
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setItemId(doc.getId());
                            itemsList.add(item);
                        }
                    }
                    
                    itemsAdapter.updateItems(itemsList);
                    itemsAdapter.filterByCategory(selectedCategory);
                    
                    if (itemsList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        itemsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                        itemsRecyclerView.setVisibility(View.VISIBLE);
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
        if (itemsListener != null) {
            itemsListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        } else if (itemId == R.id.action_leaderboard) {
            startActivity(new Intent(this, LeaderboardActivity.class));
            return true;
        } else if (itemId == R.id.action_eco_impact) {
            startActivity(new Intent(this, EcoImpactActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
