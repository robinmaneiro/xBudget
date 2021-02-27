package com.robin.xBudget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.robin.xBudget.database.DatabaseSchema;

import org.joda.time.DateTime;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.robin.xBudget.R.*;

public class DataViewFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    public static DataViewFragment newInstance() {
        return new DataViewFragment();
    }

    OuterListener mOuterListener;
    ViewPager2 viewPager;
    TabLayout tabLayout;
    Spinner mSpinner;

    int flagItemSelected = 0;
    public static ArrayAdapter spinnerAdapter;
    static String dateSelected;

    DataViewFragment.DemoCollectionAdapter demoCollectionAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layout.fragment_pager, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        demoCollectionAdapter = new DataViewFragment.DemoCollectionAdapter(this);
        viewPager = view.findViewById(id.pager);
        viewPager.setAdapter(demoCollectionAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());


        tabLayout = view.findViewById(id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? getString(R.string.tab_title_incomes) : position == 1 ? getString(R.string.tab_title_expenses) : getString(R.string.tab_title_savings))
        ).attach();


        spinnerAdapter = mOuterListener.getMonthArrayAdapter();
        mSpinner = view.findViewById(id.spinner_month);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setSelection(mOuterListener.getMonthArrayAdapter().getPosition(mOuterListener.getCurrentDateParsed()));


        dateSelected = mOuterListener.spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null).get(mSpinner.getSelectedItemPosition()));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() != 2) {
                    dateSelected = mOuterListener.spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null).get(mSpinner.getSelectedItemPosition()));

                    DataViewFragment.DemoObjectFragment fragment = (DataViewFragment.DemoObjectFragment) getChildFragmentManager()
                            .findFragmentByTag("f" + tab.getPosition());
                    if (fragment != null) {

                        fragment.mBarChart.notifyDataSetChanged();
                        fragment.mBarChart.clear();
                        fragment.mBarDataSet.clear();
                        fragment.mBarChart.invalidate();
                        fragment.dataFiller(tab.getPosition() + 1, dateSelected);

                        mOuterListener.getMonthArrayAdapter().notifyDataSetInvalidated();
                        mOuterListener.getMonthArrayAdapter().notifyDataSetChanged();
                    }
                } else {
                    // int currentItem = viewPager.getCurrentItem();
                    flagItemSelected = 0;
                    mSpinner.setAdapter(mOuterListener.getYearArrayAdapter());
                    mSpinner.setSelection(mOuterListener.getYearArrayAdapter().getPosition(String.valueOf(new DateTime().getYear())));
                    dateSelected = (String) mSpinner.getSelectedItem();
                    //Toast.makeText(getContext(), "dateSelected is: " + dateSelected, Toast.LENGTH_SHORT).show();
                    DataViewFragment.DemoObjectFragment fragment = (DataViewFragment.DemoObjectFragment) getChildFragmentManager()
                            .findFragmentByTag("f" + tab.getPosition());
                    if (fragment != null) {
                        fragment.mBarChart.notifyDataSetChanged();
                        fragment.mBarChart.clear();
                        fragment.mBarDataSet.clear();
                        fragment.mBarChart.invalidate();
                        fragment.dataFiller(tab.getPosition() + 1, dateSelected);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    flagItemSelected = 0;
                    mSpinner.setAdapter(mOuterListener.getMonthArrayAdapter());
                    mSpinner.setSelection(mOuterListener.getMonthArrayAdapter().getPosition(mOuterListener.getCurrentDateParsed()));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (flagItemSelected++ > 0) {
                    int currentItem = tabLayout.getSelectedTabPosition();

                    if (currentItem != 2) {
                        dateSelected = mOuterListener.spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null).get(i));
                    } else {
                        dateSelected = (String) mOuterListener.getYearArrayAdapter().getItem(i);
                    }

                    DataViewFragment.DemoObjectFragment fragment = (DataViewFragment.DemoObjectFragment) getChildFragmentManager()
                            .findFragmentByTag("f" + currentItem);
                    fragment.mBarChart.clear();
                    fragment.mBarData.clearValues();
                    fragment.mBarEntries.clear();
                    fragment.mBarDataSet.clear();
                    fragment.dataFiller(++currentItem, dateSelected);
                    mOuterListener.getMonthArrayAdapter().notifyDataSetInvalidated();
                    mOuterListener.getMonthArrayAdapter().notifyDataSetChanged();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public class DemoCollectionAdapter extends FragmentStateAdapter {
        public DemoCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return a NEW fragment instance
            Fragment fragment = new DataViewFragment.DemoObjectFragment();
            Bundle args = new Bundle();
            // Pass to the fragment the position
            args.putInt(DataViewFragment.DemoObjectFragment.POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    // Instances of this class are fragments representing a single
    // object in our collection.

    public static class DemoObjectFragment extends Fragment {
        Listener mListener;
        public static final String POSITION = "DataView ";
        BarChart mBarChart;
        ArrayList<BarEntry> mBarEntries;
        BarDataSet mBarDataSet;
        BarData mBarData;
        List<Integer> mSavingsColors;
        LinearLayout mNoDataLayout, mDataViewLayout;
        Button mTextGraph1;
        TextView mTextNoData, mTextTotalPeriod;
        ImageView mImageNoData;
        DecimalFormat mDecimalFormat;


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(layout.fragment_dataview, container, false);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            int position = args.getInt(POSITION);

            mDecimalFormat = new DecimalFormat("#.##");
            mTextGraph1 = (Button) view.findViewById(id.dataview_title);
            mNoDataLayout = (LinearLayout) view.findViewById(id.no_data_layout);
            mDataViewLayout = (LinearLayout) view.findViewById(id.dataview_layout);
            mImageNoData = (ImageView) view.findViewById(id.img_no_data);
            mTextNoData = (TextView) view.findViewById(id.txt_no_data);
            mTextTotalPeriod = (TextView) view.findViewById(id.total_period_val);
            mBarChart = (BarChart) view.findViewById(id.bar_chart);
            mBarEntries = new ArrayList<>();


            mBarChart.setDrawBarShadow(false);
            mBarChart.setDrawValueAboveBar(true);
            mBarChart.setMaxVisibleValueCount(50);
            mBarChart.setPinchZoom(false);
            mBarChart.setDrawGridBackground(false);
            mBarChart.getDescription().setEnabled(false);

            switch (position) {
                case 0:
                    mTextGraph1.setText(string.dataview_incomes_title);
                    mTextGraph1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_income_title));
                    dataFiller(Category.Group.INCOMES, dateSelected);
                    break;

                case 1:
                    mTextGraph1.setText(string.dataview_expenses_title);
                    mTextGraph1.setBackground(ContextCompat.getDrawable(getContext(), drawable.rounded_expense_title));
                    dataFiller(Category.Group.EXPENSES, dateSelected);
                    break;

                case 2:
                    mTextGraph1.setText(string.dataview_savings_title);
                    mTextGraph1.setBackground(ContextCompat.getDrawable(getContext(), drawable.rounded_saving_title));
                    dataFiller(Category.Group.SAVINGS, dateSelected);
                    break;
            }
        }

        synchronized void dataFiller(int group_id, String dateSelected) {
            String[] arrayDataSavings = new String[13];
            arrayDataSavings[0] = "DummyPosition";
            mSavingsColors = new ArrayList<>();

            String label;

            List<Category> categoryList = mListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ? AND CAST(date as TEXT) = ?",
                    new String[]{String.valueOf(group_id), dateSelected});

            switch (group_id) {
                case Category.Group.INCOMES:
                    label = getString(string.dataview_incomes_label);
                    break;
                case Category.Group.EXPENSES:
                    label = getString(string.dataview_expenses_label);
                    break;
                default:
                    label = getString(string.dataview_savings_label);
                    break;
            }

            int x = 0;
            if (group_id != Category.Group.SAVINGS) { //Add the entries for Incomes and Expenses
                float totalValue = 0;
                for (Category c : categoryList) {
                    float value = 0;
                    List<Transaction> transactionList = mListener.getTransactions(DatabaseSchema.TransactionTable.mTransactions, "CAST(category_id as TEXT) = ?", new String[]{c.getId().toString()});
                    for (Transaction t : transactionList) {
                        value += t.getAmount().floatValue();
                    }
                    totalValue += value;
                    mBarEntries.add(new BarEntry((float) ++x, value));
                }

                mTextTotalPeriod.setText(MainActivity.CURRENCY_SYMBOL + mDecimalFormat.format(totalValue));

            } else {
                int checkDataInserted = 0; //Check if there is any value inserted in the months-of-the-year bars created in the chart
                float totalValue = 0;
                for (int i = 1; i <= 12; i++) { //Add the entries for Savings
                    float monthlyAmount = 0;
                    //get the categories of Incomes on this YEAR and MONTH
                    for (Category c : mListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ? AND CAST(date as TEXT) = ?",
                            new String[]{String.valueOf(Category.Group.INCOMES), dateSelected + i})) {
                        //get the transaction within this category
                        for (Transaction t : mListener.getTransactions(DatabaseSchema.TransactionTable.mTransactions, "CAST(category_id as TEXT) = ?", new String[]{c.getId().toString()})) {
                            monthlyAmount += t.getAmount();
                        }
                    }
                    //get the categories of Expenses on this YEAR and MONTH
                    for (Category c : mListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ? AND CAST(date as TEXT) = ?",
                            new String[]{String.valueOf(Category.Group.EXPENSES), dateSelected + i})) {
                        //get the transaction within this category
                        for (Transaction t : mListener.getTransactions(DatabaseSchema.TransactionTable.mTransactions, "CAST(category_id as TEXT) = ?", new String[]{c.getId().toString()})) {
                            monthlyAmount -= t.getAmount();
                        }
                    }

                    mBarEntries.add(new BarEntry((float) ++x, monthlyAmount));
                    arrayDataSavings[i] = new DateFormatSymbols().getShortMonths()[i - 1];
                    mSavingsColors.add(ContextCompat.getColor(getContext(), monthlyAmount < 0.0F ? color.expenses_cat : monthlyAmount == 0.0F ? color.dark_grey : color.savings_trans));
                    if (monthlyAmount != 0.0f) ++checkDataInserted;
                    totalValue += monthlyAmount;
                }
                if (checkDataInserted == 0) {
                    mDataViewLayout.setVisibility(View.GONE);
                    mNoDataLayout.setVisibility(View.VISIBLE);
                    mImageNoData.setImageResource(drawable.no_savings_ic);
                    mTextNoData.setText(string.dataview_savings_no_data);
                } else {
                    mNoDataLayout.setVisibility(View.GONE);
                    mDataViewLayout.setVisibility(View.VISIBLE);

                    // set text to values here
                    if (totalValue < 0.0) {
                        mTextTotalPeriod.setText("-" + MainActivity.CURRENCY_SYMBOL + mDecimalFormat.format(Math.abs(totalValue))); //Show the hyphen before the currency symbol
                        mTextTotalPeriod.setTextColor(getResources().getColor(color.expenses_cat));
                    } else if (totalValue > 0.0) {
                        mTextTotalPeriod.setText(MainActivity.CURRENCY_SYMBOL + mDecimalFormat.format(totalValue));
                        mTextTotalPeriod.setTextColor(getResources().getColor(color.savings_trans));

                    } else {
                        mTextTotalPeriod.setText(MainActivity.CURRENCY_SYMBOL + mDecimalFormat.format(totalValue));
                    }
                }
            }

            //This is the graphic
            mBarDataSet = new BarDataSet(mBarEntries, label);
            mBarDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            mBarDataSet.setValueTextSize(11f);

            if (group_id == Category.Group.SAVINGS) mBarDataSet.setColors(mSavingsColors);

            mBarData = new BarData(mBarDataSet);
            mBarData.setBarWidth(0.9f);
            mBarChart.setData(mBarData);

            String[] arrayDataTransactions = new String[categoryList.size() + 1];
            arrayDataTransactions[0] = "DummyPosition";
            for (int i = 1; i <= categoryList.size(); i++) {
                arrayDataTransactions[i] = categoryList.get(i - 1).getName();
            }

            //When the mBarDataSet is empty this layout will be visible to inform the user that there is no data.
            if (group_id != Category.Group.SAVINGS) {
                if (arrayDataTransactions.length <= 1) {
                    mDataViewLayout.setVisibility(View.GONE);
                    mImageNoData.setImageResource(group_id == 1 || group_id == 2 ? drawable.no_transaction_ic : drawable.no_savings_ic);
                    mTextNoData.setText(group_id == 1 ? string.dataview_incomes_no_data : group_id == 2 ? string.dataview_expenses_no_data : string.dataview_savings_no_data);
                    mNoDataLayout.setVisibility(View.VISIBLE);
                } else {
                    mDataViewLayout.setVisibility(View.VISIBLE);
                    mNoDataLayout.setVisibility(View.GONE);
                }
            }

            XAxis xAxis = mBarChart.getXAxis();
            xAxis.setValueFormatter(new myXAxisValueFormatter(group_id != 3 ? arrayDataTransactions : arrayDataSavings));

            xAxis.setPosition(XAxis.XAxisPosition.TOP);
            xAxis.setGranularity(1f);
            mBarChart.animateY(arrayDataTransactions.length > 2 ? 850 : 450);

        }

        class myXAxisValueFormatter extends IndexAxisValueFormatter {

            private String[] mValues;

            public myXAxisValueFormatter(String[] values) {
                this.mValues = values;
            }

            @Override
            public String getFormattedValue(float value) {
                if (value < 0 || value >= mValues.length) {
                    return "";
                } else {
                    return mValues[(int) value];
                }
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (DataViewFragment.DemoObjectFragment.Listener) context;
            } catch (ClassCastException cce) {
                throw new ClassCastException("Class must implement DataView.Listener");
            }
        }

        public interface Listener {
            List<Category> getCategories(String table, String whereClause, String[] whereArgs);

            List<Transaction> getTransactions(String table, String whereClause, String[] whereArgs);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOuterListener = (DataViewFragment.OuterListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("Class must implement StatisticListener");
        }
    }

    public interface OuterListener {
        ArrayAdapter getMonthArrayAdapter();

        ArrayAdapter getYearArrayAdapter();

        List<String> getParsedMonthDates(String table, String whereClause, String[] whereArgs);

        String getCurrentDateParsed();

        String spinnerToJodaTime(String parsedDate);
    }
}
