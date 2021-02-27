package com.robin.xBudget;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

public class DialogOnBoarding extends DialogFragment {

    ViewPager viewPager;
    Button nextButton, skipButton;
    int startingPosition;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_on_boarding, null);

        skipButton = view.findViewById(R.id.dialog_onboarding_btn_skip);
        nextButton = view.findViewById(R.id.dialog_onboarding_btn_next);
        // Initialize ViewPager view
        viewPager = view.findViewById(R.id.viewPagerOnBoarding);
        // create ViewPager adapter
        OnBoardingAdapter viewPagerAdapter = new OnBoardingAdapter();

        // Set Adapter for ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // Setup dot's indicator
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutIndicator);
        tabLayout.setupWithViewPager(viewPager);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() != 7) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                } else {
                    dismiss();
                }
            }
        });

        viewPager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (viewPager.getCurrentItem() == 7) {
                    nextButton.setText(R.string.dialog_onboarding_finishBtn);
                } else {
                    nextButton.setText(getResources().getText(R.string.fragment_step_two_nextBtn));
                }
                if (viewPager.getCurrentItem() == 7) {
                    skipButton.setVisibility(View.INVISIBLE);
                } else {
                    skipButton.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setView(view);

        return builder.create();
    }

    private class OnBoardingAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = getActivity().getLayoutInflater();

            int layoutSelected;

            switch (position) {
                case 0:
                    layoutSelected = R.layout.fragment_step_one;
                    break;
                case 1:
                    layoutSelected = R.layout.fragment_step_two;
                    break;
                case 2:
                    layoutSelected = R.layout.fragment_step_three;
                    break;
                case 3:
                    layoutSelected = R.layout.fragment_step_four;
                    break;
                case 4:
                    layoutSelected = R.layout.fragment_step_five;
                    break;
                case 5:
                    layoutSelected = R.layout.fragment_step_six;
                    break;
                case 6:
                    layoutSelected = R.layout.fragment_step_seven;
                    break;
                default:
                    layoutSelected = R.layout.fragment_step_eight;
            }

            View view = inflater.inflate(layoutSelected, null);
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.alpha = 0.90f;
        getDialog().getWindow().setAttributes(layoutParams);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
