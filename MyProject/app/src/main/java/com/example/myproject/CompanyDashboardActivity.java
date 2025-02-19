package com.example.myproject;

mport android.content.DialogInterface;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myproject.AboutUsActivity;
import com.example.myproject.MainActivity;
import com.example.myproject.R;
import com.example.myproject.Utils;
import com.example.myproject.databinding.ActivityCompanyDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CompanyDashboardActivity extends AppCompatActivity {

    private ActivityCompanyDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;
    private Utils.AccountType accountType;
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private TextView userName;
    private SharedPreferences.Editor editor;
    private Boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompanyDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        accountType = (Utils.AccountType) getIntent().getSerializableExtra("accountType");
        isBlocked = getIntent().getBooleanExtra("isBlocked", false);
        if(isBlocked)
            binding.getRoot().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(getString(R.string.account_type), 2).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isBlocked", isBlocked).apply();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_post_jobs, R.id.nav_view_student_applications,
                R.id.nav_update_details, R.id.nav_change_password)
                .setDrawerLayout(binding.drawerLayout)
                .build();

        navController = Navigation.findNavController(this, R.id.company_fragment_container_view_tag);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_out_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.log_out){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Do you want to log out?").setTitle("LOG OUT")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.getInstance().signOut();
                            startActivity(new Intent(CompanyDashboardActivity.this,
                                    MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        } else if(item.getItemId() == R.id.about_us){
            startActivity(new Intent(this, AboutUsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.company_fragment_container_view_tag);
        if(isBlocked)
            return true;
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userName = binding.navView.getHeaderView(0).findViewById(R.id.userName);
        TextView userEmailId = binding.navView.getHeaderView(0).findViewById(R.id.userEmailId);

        rootRef = FirebaseDatabase.getInstance().getReference().child("Company").child(mAuth.getCurrentUser().getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String full_name = snapshot.child("Profile").child("full_name").getValue().toString();
                userName.setText(full_name);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("full_name",full_name).apply();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        userEmailId.setText(currentUser.getEmail());
    }

    public void updateUserName(String name){
        userName.setText(name);
    }
}
