package com.robin.miniBudget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.robin.miniBudget.database.DatabaseSchema;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class StatisticsFragment extends Fragment {
    private OuterListener mOuterListener;
    private ViewPager2 viewPager;
    private Spinner pagerSpinner;
    private StatisticsFragment.DemoCollectionAdapter demoCollectionAdapter;

    StatisticsFragment stats;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        demoCollectionAdapter = new StatisticsFragment.DemoCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(demoCollectionAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        stats = StatisticsFragment.newInstance();
        pagerSpinner = view.findViewById(R.id.spinner_month);
        pagerSpinner.setVisibility(View.GONE);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? getString(R.string.tab_title_incomes) : (position == 1) ? getString(R.string.tab_title_expenses) : getString(R.string.tab_title_savings))
        ).attach();
    }


    public class DemoCollectionAdapter extends FragmentStateAdapter {
        public DemoCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return a NEW fragment instance in createFragment(int)
            Fragment fragment = new StatisticsFragment.DemoObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(StatisticsFragment.DemoObjectFragment.POSITION, position);
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
        public static final String POSITION = "Statistics ";

        int flagItemSelected = 0;
        InnerListener mInnerListener;
        ScrollView sv;
        int position;
        Spinner mSpinner;
        Set mCategoriesSet;
        List<Category> mCategoriesList;
        ArrayAdapter spinnerAdapter;
        DecimalFormat mDecimalFormat;



        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_statistics, container, false);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            position = args.getInt(POSITION);
            //Toast.makeText(getContext(),"position is: "+position,Toast.LENGTH_SHORT).show();
            //Log.d("position ","position is: "+position);

            mDecimalFormat = new DecimalFormat("#.##");
            sv = view.findViewById(R.id.scroll_stats_1);

            mSpinner = view.findViewById(R.id.spinner_fragment_stats);
            if (position == 2) mSpinner.setVisibility(View.GONE);
            mCategoriesList = mInnerListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(position + 1)});
            mCategoriesSet = new TreeSet();

            mCategoriesList.forEach(c->mCategoriesSet.add(c.getName()));

            if (mCategoriesSet.isEmpty())
                mCategoriesSet.add(getString(R.string.stats_fragment_insertCategory));

            spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>(mCategoriesSet));

            mSpinner.setAdapter(spinnerAdapter);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {

                    //Toast.makeText(getContext(), "" + spinnerAdapter.getItem(i), Toast.LENGTH_SHORT).show();
                    //if(position!=2)setValues(view, (String) mSpinner.getSelectedItem(), position);

                    if (flagItemSelected++ > 0 && position != 2)
                        setValuesCategories(view, (String) mSpinner.getSelectedItem(), position);

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            setName(view, position);
            setDrawable(view, position);
            if (position != 2) {
                setValuesTransactions(view, position);
                setValuesCategories(view, (String) mCategoriesSet.toArray()[0], position);
            }
            if (position == 2) setValuesSavings(view);

        }

        void setName(View view, int position) {
            List<String> type = Arrays.asList("incomes", "expenses", "savings");
            String group = type.get(position);


            for (char c : "abc".toCharArray()) {
                for (int i = 1; i <= 8; i++) {
                    ((TextView) view.findViewById(getResources().getIdentifier("title_" + String.valueOf(c) + i, "id", getContext().getPackageName()))).
                            setText(MainActivity.CURRENCY + " " + getString(getResources().getIdentifier("text_" + group + "_" + String.valueOf(c) + i, "string", getContext().getPackageName())));
                }
            }
        }

        void setDrawable(View view, int position) {

            List<String> type = Arrays.asList("incomes", "expenses", "savings");
            String group = type.get(position);

            for (char c : "abc".toCharArray()) {
                for (int i = 1; i <= 4; i++) {
                    ((LinearLayout) view.findViewById(getResources().getIdentifier("grid_" + String.valueOf(c) + i, "id", getContext().getPackageName())))
                            .setBackground(ContextCompat.getDrawable(getContext(),
                                    getResources().getIdentifier("backcolor_trans_" + group, "drawable", getContext().getPackageName())));
                }
                if (position != 2)
                    for (int i = 5; i <= 8; i++) {

                        ((LinearLayout) view.findViewById(getResources().getIdentifier("grid_" + String.valueOf(c) + i, "id", getContext().getPackageName())))
                                .setBackground(ContextCompat.getDrawable(getContext(),
                                        getResources().getIdentifier("backcolor_cat_" + group, "drawable", getContext().getPackageName())));
                    }

                for (int i = 2; i <= 8; i++) {
                    if (position == 2)
                        ((LinearLayout) view.findViewById(getResources().getIdentifier("grid_" + String.valueOf(c) + i, "id", getContext().getPackageName()))).setVisibility(View.INVISIBLE);
                }
            }
            if (position != 2)
                ((TextView) view.findViewById(R.id.category_name)).setVisibility(View.VISIBLE);
            else ((TextView) view.findViewById(R.id.category_name)).setVisibility(View.INVISIBLE);

        }


        synchronized void setValuesTransactions(View view, int position) {
            //Log.d("position", " setValues() was called");
            Toast.makeText(getContext(),"position is: "+position,Toast.LENGTH_SHORT).show();
            if (position != 2) {
                for (int i = 1; i <= 4; i++) {

                    TextView a1to4 = ((TextView) view.findViewById(getResources().getIdentifier("value_a" + i, "id", getContext().getPackageName())));
                    TextView b1to4 = ((TextView) view.findViewById(getResources().getIdentifier("value_b" + i, "id", getContext().getPackageName())));
                    TextView c1to4 = ((TextView) view.findViewById(getResources().getIdentifier("value_c" + i, "id", getContext().getPackageName())));

                    double getTransAmount = mInnerListener.getTransAmount(DatabaseSchema.TransactionTable.mTransactions, position + 1, i);
                    String getTransDiff = mInnerListener.getTransDiff(DatabaseSchema.TransactionTable.mTransactions, position + 1, i);
                    String getTransDiffAvg = mInnerListener.getTransDiffAvg(DatabaseSchema.TransactionTable.mTransactions, position + 1, i);
                    a1to4.setText(MainActivity.CURRENCY + mDecimalFormat.format(getTransAmount));

                    b1to4.setText(getTransDiff);
                    c1to4.setText(getTransDiffAvg);

                    if (getTransDiff.contains("+"))
                        b1to4.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.expenses_cat) : getResources().getColor(R.color.savings_trans));
                    else {
                        if (getTransDiff.contains("-")) {
                            b1to4.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.savings_trans) : getResources().getColor(R.color.expenses_cat));
                        } else {
                            b1to4.setTextColor(getResources().getColor(R.color.dark_grey));
                        }
                    }

                    if (getTransDiffAvg.contains("+"))
                        c1to4.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.expenses_cat) : getResources().getColor(R.color.savings_trans));
                    else {
                        if (getTransDiffAvg.contains("-")) {
                            c1to4.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.savings_trans) : getResources().getColor(R.color.expenses_cat));
                        } else {
                            c1to4.setTextColor(getResources().getColor(R.color.dark_grey));
                        }
                    }
                }
            }
        }


        synchronized void setValuesCategories(View view, String category, int position) {
            for (int i = 5; i <= 8; i++) {
                //Log.d("position", "position is :"+position+ " and i value is: "+i+" and Category is: "+category+" getContext().getPackageName() "+ getContext().getPackageName());

                TextView a5to8 = ((TextView) view.findViewById(getResources().getIdentifier("value_a" + i, "id", getContext().getPackageName())));
                TextView b5to8 = ((TextView) view.findViewById(getResources().getIdentifier("value_b" + i, "id", getContext().getPackageName())));
                TextView c5to8 = ((TextView) view.findViewById(getResources().getIdentifier("value_c" + i, "id", getContext().getPackageName())));

                Double getCatsAmount = mInnerListener.getCatsAmount(DatabaseSchema.TransactionTable.mCategories, category, position + 1, i);
                String getCatsDiff = mInnerListener.getCatsDiff(DatabaseSchema.TransactionTable.mCategories, category, position + 1, i);
                String getCatsAvgDiff = mInnerListener.getCatsDiffAvg(DatabaseSchema.TransactionTable.mCategories, category, position + 1, i);

                a5to8.setText(MainActivity.CURRENCY + mDecimalFormat.format(getCatsAmount));
                b5to8.setText(String.valueOf(getCatsDiff));
                if (position != 2)
                    c5to8.setText(String.valueOf(getCatsAvgDiff));


                if (getCatsDiff.contains("+"))
                    b5to8.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.expenses_cat) : getResources().getColor(R.color.savings_trans));
                else {
                    if (getCatsDiff.contains("-")) {
                        b5to8.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.savings_trans) : getResources().getColor(R.color.expenses_cat));
                    } else {
                        b5to8.setTextColor(getResources().getColor(R.color.dark_grey));
                    }
                }

                if (getCatsAvgDiff.contains("+"))
                    c5to8.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.expenses_cat) : getResources().getColor(R.color.savings_trans));
                else {
                    if (getCatsAvgDiff.contains("-")) {
                        c5to8.setTextColor(position == 1 || position == 2 ? getResources().getColor(R.color.savings_trans) : getResources().getColor(R.color.expenses_cat));
                    } else {
                        c5to8.setTextColor(getResources().getColor(R.color.dark_grey));
                    }
                }
            }
        }

        synchronized void setValuesSavings(View view) {
            GridLayout gridLayoutCategories = view.findViewById(getResources().getIdentifier("layout_grid_categories", "id", getContext().getPackageName()));
            gridLayoutCategories.setVisibility(View.GONE); // Set visibility to gone, also to 'disable' the scrolling function to this view.
            //Toast.makeText(getContext(), "setvaluesavings called", Toast.LENGTH_SHORT).show();

            TextView a1 = ((TextView) view.findViewById(getResources().getIdentifier("value_a1", "id", getContext().getPackageName())));
            TextView b1 = ((TextView) view.findViewById(getResources().getIdentifier("value_b1", "id", getContext().getPackageName())));
            TextView c1 = ((TextView) view.findViewById(getResources().getIdentifier("value_c1", "id", getContext().getPackageName())));

            a1.setText(MainActivity.CURRENCY + mDecimalFormat.format(mInnerListener.getMonthlySavingsAmount()));
            b1.setText(mInnerListener.getSavingsDiff());
            c1.setText(mInnerListener.getSavingsDiffAvg());

            if (mInnerListener.getMonthlySavingsAmount() > 0.0)
                a1.setTextColor(getResources().getColor(R.color.savings_trans));
            else if (mInnerListener.getMonthlySavingsAmount() < 0.0) {
                a1.setTextColor(getResources().getColor(R.color.expenses_cat));
            } else {
                a1.setTextColor(getResources().getColor(R.color.dark_grey));
            }


            if (mInnerListener.getSavingsDiff().contains("+"))
                b1.setTextColor(getResources().getColor(R.color.savings_trans));
            else {
                if (mInnerListener.getSavingsDiff().contains("-")) {
                    b1.setTextColor(getResources().getColor(R.color.expenses_cat));
                } else {
                    b1.setTextColor(getResources().getColor(R.color.dark_grey));
                }
            }

            if (mInnerListener.getSavingsDiffAvg().contains("+"))
                c1.setTextColor(getResources().getColor(R.color.savings_trans));
            else {
                if (mInnerListener.getSavingsDiffAvg().contains("-")) {
                    c1.setTextColor(getResources().getColor(R.color.expenses_cat));
                } else {
                    c1.setTextColor(getResources().getColor(R.color.dark_grey));
                }
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mInnerListener = (StatisticsFragment.DemoObjectFragment.InnerListener) context;
            } catch (ClassCastException cce) {
                throw new ClassCastException("Class must implement StatisticListener");
            }
        }

        public interface InnerListener {
            double getTransAmount(String tableName, int group_id, int period);

            double getMonthlySavingsAmount();

            String getSavingsDiff();

            String getSavingsDiffAvg();

            String getTransDiff(String tableName, int group_id, int period);

            String getTransDiffAvg(String tableName, int group_id, int period);

            double getCatsAmount(String tableName, String name, int group_id, int period);

            String getCatsDiff(String tableName, String name, int group_id, int period);

            String getCatsDiffAvg(String tableName, String name, int group_id, int period);

            List<Category> getCategories(String table, String whereClause, String[] whereArgs);

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOuterListener = (StatisticsFragment.OuterListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("Class must implement StatisticListener");
        }
    }

    interface OuterListener {
        List<Category> getCategories(String table, String whereClause, String[] whereArgs);
    }
}
