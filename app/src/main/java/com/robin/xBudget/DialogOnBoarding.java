package com.robin.xBudget;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class DialogOnBoarding extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_on_boarding, null);

        builder.setView(view).setPositiveButton("SKIP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        // Initialize ViewPager view
        ViewPager viewPager = view.findViewById(R.id.viewPagerOnBoarding);
        // create ViewPager adapter
        TestAdapter viewPagerAdapter = new TestAdapter();

        /*
        // Add All Fragments to ViewPager
        viewPagerAdapter.addFragment(new StepOneFragment());
        viewPagerAdapter.addFragment(new StepTwoFragment());
        viewPagerAdapter.addFragment(new StepThreeFragment());
        viewPagerAdapter.addFragment(new StepFourFragment());

         */

        //mSkipButton = view.findViewById(R.id.skip_btn);

/*
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


 */


        // Set Adapter for ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // Setup dot's indicator
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutIndicator);
        tabLayout.setupWithViewPager(viewPager);


        return builder.create();
    }

    // ViewPager Adapter class
    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }
        @Override
        public Fragment getItem(int i) {
            return mList.get(i);
        }
        @Override
        public int getCount() {
            return mList.size();
        }
        public void addFragment(Fragment fragment) {
            mList.add(fragment);
        }
    }

    private class TestAdapter extends PagerAdapter {

        private int mCurrentPosition = -1;
        
        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            int layoutThingy;

            switch(position) {
                case 0:
                    layoutThingy = R.layout.fragment_step_one;
                    break;
                case 1:
                    layoutThingy = R.layout.fragment_step_two;
                    break;
                case 2:
                    layoutThingy = R.layout.fragment_step_three;
                    break;
                default:
                    layoutThingy = R.layout.fragment_step_four;
            }

            View view = inflater.inflate(layoutThingy, null);
            container.addView(view);
            view.requestFocus();
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            /*
            if (position != mCurrentPosition) {
                View view = (View) object;
                CustomPager pager = (CustomPager) container;
                if (view != null) {
                    mCurrentPosition = position;
                    pager.measureCurrentView(view);
                }
            }

             */
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.alpha=0.90f;
        getDialog().getWindow().setAttributes(lp);

        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        return super.onCreateView(inflater, container, savedInstanceState);
    }



}
