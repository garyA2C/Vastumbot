package com.vastumbot.vastumap;

import android.content.ClipData;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.vastumbot.vastumap.databinding.ActivityMainBinding;
import com.vastumbot.vastumap.ui.home.HomeFragment;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private ClipData.Item item;
    private Switch swi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Actualisation de la carte", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                HomeFragment.actualiseAllWaste();
                HomeFragment.drawOnMap();
            }
        });

        swi=findViewById(R.id.swi);
        swi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swi.isChecked()){
                    swi.setText("Actifs");
                    swi.setBackgroundColor(0xFF90CC00);
                    HomeFragment.actif=true;
                    HomeFragment.drawOnMap();

                }else{
                    swi.setText("Ramassés");
                    swi.setBackgroundColor(0xFFEE9000);
                    HomeFragment.actif=false;
                    HomeFragment.drawOnMap();
                }
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}