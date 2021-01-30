package com.robin.xBudget;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
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

    protected abstract void init();
    protected abstract Fragment createFragment();
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

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
        JodaTimeAndroid.init(this);
        init();
        Log.d(TAG, "onCreate was called");

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

        toolbar = (Toolbar) findViewById(R.id.toolbar_top); //here toolbar is your id in xml
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLUE));
        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_transactions);
        toolbar.getOverflowIcon().setTint(getResources().getColor(R.color.dark_blue));
        bottomBarSupport();
    }


    public void bottomBarSupport() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.transactions:
                        TransFragment transFragment = TransFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, transFragment).commit();
                        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_transactions);
                        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                        break;

                    case R.id.statistics:
                        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_statistics);
                        StatisticsFragment statisticsFragment = StatisticsFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, statisticsFragment).commit();
                        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
                        break;

                    case R.id.dataview:
                        DataViewFragment dataViewFragment = DataViewFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dataViewFragment).commit();
                        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(R.string.bottom_bar_dataview);
                        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.MAGENTA));
                        break;
                }
                return true;
            }
        });
    }
}
