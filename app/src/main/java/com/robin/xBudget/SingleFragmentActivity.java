package com.robin.xBudget;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.danlew.android.joda.JodaTimeAndroid;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    protected abstract void initDatabase();

    protected abstract Fragment createFragment();

    BottomNavigationView bottomNavigationView;
    Toolbar topToolbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this); //Initialize Joda time
        initDatabase();

        //With this method SingleFragmentActivity gets prepared to adapt the layout to a tablet in future implementations
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        topToolbar = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(topToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) topToolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_transactions);
        topToolbar.getOverflowIcon().setTint(getResources().getColor(R.color.dark_blue));
        bottomBarSupport();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {  // Item selection in the overflow menu

        switch (item.getItemId()) {
            case R.id.overflow_menu_about:
                DialogAboutUs aboutUsFragment = new DialogAboutUs();
                aboutUsFragment.show(getSupportFragmentManager(), "Dialog AboutUs");
                break;

            case R.id.overflow_menu_settings:
                DialogSettings dialogSettings = new DialogSettings();
                dialogSettings.show(getSupportFragmentManager(), "Dialog Settings");
                break;

            case R.id.overflow_menu_exitapp:
                finishAndRemoveTask();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void bottomBarSupport() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {   //Change fragments in the bottom bar
                switch (item.getItemId()) {

                    case R.id.transactions:
                        TransFragment transFragment = TransFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, transFragment).commit();
                        ((TextView) topToolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_transactions);
                        break;

                    case R.id.statistics:
                        ((TextView) topToolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_statistics);
                        StatisticsFragment statisticsFragment = StatisticsFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, statisticsFragment).commit();
                        break;

                    case R.id.dataview:
                        DataViewFragment dataViewFragment = DataViewFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dataViewFragment).commit();
                        ((TextView) topToolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_dataview);
                        break;
                }
                return true;
            }
        });
    }
}
