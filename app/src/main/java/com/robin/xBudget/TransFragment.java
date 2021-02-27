package com.robin.xBudget;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.robin.xBudget.database.DatabaseSchema;

import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Comparator;
import java.util.List;


public class TransFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    OuterListener mOuterListener;
    ViewPager2 viewPager;
    static Spinner mSpinner;
    static String dateSelected;
    static ArrayAdapter spinnerAdapter;
    DemoCollectionAdapter demoCollectionAdapter;
    SharedPreferences sharedPreferences;
    Boolean firstTime;

    public static TransFragment newInstance() {
        return new TransFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        sharedPreferences = this.getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        firstTime = sharedPreferences.getBoolean("firstTime", true);        //Set default value to true if the boolean is not in the sharedPreferences object

        if (firstTime) {        //Initiate DialogOnboarding on the first execution of the application to give the user guidance of app usage
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    firstTime = false;
                    editor.putBoolean("firstTime", firstTime);
                    editor.apply();
                    new DialogOnBoarding().show(getFragmentManager(), "dialogOnBoarding");
                }
            }, 2000);
        }

        //new DialogOnBoarding().show(getFragmentManager(), "dialogOnBoarding"); //DELETE ME AFTER ALL TESTS

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        demoCollectionAdapter = new DemoCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(demoCollectionAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        //viewPager.setCurrentItem(1);
        //Log.d("DEMO", "CURRENT POSITION IS: " + viewPager.getCurrentItem()); 250221

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(((position % 2) == 0) ? getString(R.string.tab_title_incomes) : getString(R.string.tab_title_expenses))
        ).attach();

        spinnerAdapter = mOuterListener.getMonthArrayAdapter();
        mSpinner = view.findViewById(R.id.spinner_month);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setSelection(mOuterListener.getMonthArrayAdapter().getPosition(mOuterListener.getCurrentDateParsed()) != -1 ?
                mOuterListener.getMonthArrayAdapter().getPosition(mOuterListener.getCurrentDateParsed())
                : mOuterListener.getMonthArrayAdapter().getCount() - 1);
        dateSelected = mOuterListener
                .spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null)
                        .get(mSpinner.getSelectedItemPosition()));

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int currentItem = viewPager.getCurrentItem();
                dateSelected = mOuterListener.spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null).get(i));
                TransFragment.DemoObjectFragment fragment = (TransFragment.DemoObjectFragment) getChildFragmentManager()
                        .findFragmentByTag("f" + currentItem);
                fragment.update(++currentItem, dateSelected);
                mOuterListener.getMonthArrayAdapter().notifyDataSetInvalidated();
                mOuterListener.getMonthArrayAdapter().notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int currentItem = viewPager.getCurrentItem();
                dateSelected = mOuterListener.spinnerToJodaTime(mOuterListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null).get(mSpinner.getSelectedItemPosition()));
                TransFragment.DemoObjectFragment fragment = (TransFragment.DemoObjectFragment) getChildFragmentManager()
                        .findFragmentByTag("f" + currentItem);
                fragment.update(++currentItem, dateSelected);
                mOuterListener.getMonthArrayAdapter().notifyDataSetInvalidated();
                mOuterListener.getMonthArrayAdapter().notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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

            // Return a NEW fragment instance in createFragment(int)
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();

            args.putInt(DemoObjectFragment.POSITION_KEY, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public static class DemoObjectFragment extends Fragment {

        public static final String POSITION_KEY = "key_position ";
        ExpandingList expandingList;
        Listener mInnerListener;
        FloatingActionButton addTransaction;
        List<String> parsedDates;
        List<Category> listCatsSelectedMonth;


        int tabPosition;
        private LinearLayout mNoDataLayout;
        private ScrollView mScrollView;
        private RelativeLayout mGlobalDataLayout;
        private ImageView mImageNoData;
        private TextView mTextNoData, mTextMonthlyTotal;
        private Button mGeneralSpendBtn;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {


            super.onCreate(savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void setMenuVisibility(boolean menuVisible) {
            super.setMenuVisibility(menuVisible);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_trans, container, false);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            int position = args.getInt(POSITION_KEY);
            tabPosition = ++position;

            mNoDataLayout = (LinearLayout) view.findViewById(R.id.no_data_layout);
            mScrollView = (ScrollView) view.findViewById(R.id.scrollViewIncome);
            mGlobalDataLayout = (RelativeLayout) view.findViewById(R.id.global_message_layout);
            mImageNoData = (ImageView) view.findViewById(R.id.img_no_data);
            mTextNoData = (TextView) view.findViewById(R.id.txt_no_data);
            mTextMonthlyTotal = (TextView) view.findViewById(R.id.txt_monthly_total);
            mGeneralSpendBtn = ((Button) view.findViewById(R.id.btn_general_spent));

            expandingList = (ExpandingList) view.findViewById(R.id.expanding_list_main);
            addTransaction = (FloatingActionButton) view.findViewById(R.id.imgview_add_transaction);
            parsedDates = mInnerListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null);

            switch (tabPosition) {
                case 1:
                    ((Button) view.findViewById(R.id.text_transaction)).setText(getString(R.string.trans_fragment_title_incomes));
                    ((Button) view.findViewById(R.id.text_transaction)).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_income_title));
                    mTextMonthlyTotal.setText(R.string.fragment_trans_txt_totalEarned);
                    addTransaction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.incomes_cat)));
                    addTransaction.setRippleColor(getResources().getColor(R.color.incomes_trans));


                    // Fetch parents
                    update(Category.Group.INCOMES, dateSelected);

                    addTransaction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(@Nullable View v) {
                            DialogTransaction dialog = new DialogTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putInt(DialogTransaction.GROUP_ID, Category.Group.INCOMES);
                            dialog.setArguments(bundle);
                            dialog.setTargetFragment(DemoObjectFragment.this, tabPosition);
                            dialog.show(getFragmentManager(), "Input new income");
                        }
                    });

                    break;

                case 2:
                    Button expenseBtn = ((Button) view.findViewById(R.id.text_transaction));
                    expenseBtn.setText(getString(R.string.trans_fragment_title_expenses));
                    expenseBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_expense_title));
                    mTextMonthlyTotal.setText(R.string.fragment_trans_txt_totalSpent);
                    addTransaction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.expenses_cat)));
                    addTransaction.setRippleColor(getResources().getColor(R.color.expenses_trans));
                    update(Category.Group.EXPENSES, dateSelected);

                    addTransaction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(@Nullable View v) {
                            DialogTransaction dialog = new DialogTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putInt(DialogTransaction.GROUP_ID, Category.Group.EXPENSES);
                            dialog.setArguments(bundle);
                            dialog.setTargetFragment(DemoObjectFragment.this, tabPosition);
                            dialog.show(getFragmentManager(), "Input new expense");
                        }
                    });
                    break;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void update(int group_id, String dateSelected) {
            expandingList.removeAllViews();

            listCatsSelectedMonth = mInnerListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ? AND CAST(date as TEXT) = ?", new String[]{String.valueOf(group_id), dateSelected});
            Log.d("DEMOFRAGMENT", "update() was called with dateSelected " + dateSelected + " and group id " + group_id);

            listCatsSelectedMonth.sort(Comparator.comparing(o -> o.getName()));


            if (!listCatsSelectedMonth.isEmpty()) {
                mScrollView.setVisibility(View.VISIBLE);
                mGlobalDataLayout.setVisibility(View.VISIBLE);
                mNoDataLayout.setVisibility(View.GONE);

                double moneyBudgetedGeneral = 0;
                double moneySpentGeneral = 0;

                for (Category c : listCatsSelectedMonth) {
                    ExpandingItem parent = expandingList.createNewItem(R.layout.expanding_layout);
                    double moneySpentCategory = 0;
                    double moneyBudgetedCategory = c.getAmount();
                    ((TextView) parent.findViewById(R.id.title)).setText(c.getName());

                    parent.setIndicatorColorRes(group_id % 2 == 0 ? R.color.expenses_cat : R.color.incomes_cat);
                    //parent.setIndicatorIconRes(group_id % 2 == 0 ? R.drawable.expense_ic : R.drawable.income_ic); // Next update - Add icon to each of the categories.

                    parent.findViewById(R.id.edit_item).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogCategory dialogCategory = new DialogCategory();
                            Bundle bundle = new Bundle();
                            bundle.putInt(DialogCategory.GROUP_ID, group_id);
                            bundle.putSerializable(DialogCategory.CATEGORY_ID, c.getId());
                            dialogCategory.setTargetFragment(DemoObjectFragment.this, tabPosition);
                            dialogCategory.setArguments(bundle);
                            dialogCategory.show(getFragmentManager(), "Category Expense");
                        }
                    });

                    List<Transaction> listTransactions = mInnerListener.getTransactions(DatabaseSchema.TransactionTable.mTransactions, "CAST(category_id as TEXT) = ?", new String[]{c.getId().toString()});
                    listTransactions.sort(Comparator.comparing(o -> o.getDateTime()));

                    for (Transaction t : listTransactions) {
                        View subItem = parent.createSubItem();
                        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yy HH:mm");
                        ((TextView) subItem.findViewById(R.id.sub_date)).setText(fmt.print(t.getDateTime()));
                        ((TextView) subItem.findViewById(R.id.sub_title)).setText(t.getName());
                        ((TextView) subItem.findViewById(R.id.sub_amount)).setText(MainActivity.CURRENCY_SYMBOL + t.getAmount().toString());

                        moneySpentCategory += t.getAmount();
                        subItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DialogTransaction dialog = new DialogTransaction();
                                Bundle bundle = new Bundle();
                                bundle.putInt(DialogTransaction.GROUP_ID, group_id);
                                bundle.putSerializable(DialogTransaction.TRANSACTION_ID, t.getId());
                                dialog.setArguments(bundle);
                                dialog.setTargetFragment(DemoObjectFragment.this, group_id);
                                dialog.show(getFragmentManager(), "Input new expense");
                            }
                        });
                    }
                    parent.collapse();

                    // This set the text on the button representing the moneySpent/moneyBudgeted (Individual categories on the selected month)
                    double relation = moneySpentCategory / moneyBudgetedCategory;
                    Button spentBtn = ((Button) parent.findViewById(R.id.cat_spent));
                    buttonColorer(spentBtn, relation, group_id);
                    spentBtn.setText((int) moneySpentCategory + "/" + (int) moneyBudgetedCategory);

                    moneySpentGeneral += moneySpentCategory;
                    moneyBudgetedGeneral += moneyBudgetedCategory;
                }

                // This set the text on the button representing the moneySpent/moneyBudgeted (All categories in the selected month)
                double generalRelation = moneySpentGeneral / moneyBudgetedGeneral;
                buttonColorer(mGeneralSpendBtn, generalRelation, group_id);
                mGeneralSpendBtn.setText(Double.valueOf(moneySpentGeneral).intValue() + "/" + (int) moneyBudgetedGeneral);

            } else { //When the mBarDataSet is empty this layout will be visible to inform the user that there is no data.
                mScrollView.setVisibility(View.GONE);
                mImageNoData.setImageResource(group_id == 1 || group_id == 2 ? R.drawable.no_transaction_ic : R.drawable.no_savings_ic);
                mTextNoData.setText(group_id == 1 ? R.string.dataview_incomes_no_data : group_id == 2 ? R.string.dataview_expenses_no_data : R.string.dataview_savings_no_data);
                mNoDataLayout.setVisibility(View.VISIBLE);
                mGlobalDataLayout.setVisibility(View.INVISIBLE);
            }
            Log.d("DEMO", "Transactions updated");

        }

        synchronized void buttonColorer(Button button, double relation, int group_id) {
            if (group_id == 1) {         //Buttons in Income category

                if (relation >= 0 && relation <= 0.7) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_red));
                } else if (relation > 0.7 && relation < 1) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_yellow));
                } else if (relation >= 1) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_green));
                } else {
                    button.setVisibility(View.INVISIBLE);
                }
            }
            if (group_id == 2) {         //Buttons in Expense category

                if (relation >= 0 && relation <= 0.7) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_green));
                } else if (relation > 0.7 && relation <= 1) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_yellow));
                } else if (relation > 1) {
                    button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_red));
                } else {
                    button.setVisibility(View.INVISIBLE);
                }
            }
        }


        @Override
        public void onAttach(@NotNull Context context) {
            super.onAttach(context);
            try {
                mInnerListener = (Listener) context;
            } catch (ClassCastException cce) {
                throw new ClassCastException("Class must implement StatisticListener");
            }
        }

        public interface Listener {
            List<Category> getCategories(String table, String whereClause, String[] whereArgs);

            List<Transaction> getTransactions(String table, String whereClause, String[] whereArgs);

            List<String> getParsedMonthDates(String table, String whereClause, String[] whereArgs);
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mOuterListener = (OuterListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("Class must implement StatisticListener");
        }
    }

    public interface OuterListener {
        List<String> getParsedMonthDates(String table, String whereClause, String[] whereArgs);

        String getCurrentDateParsed();

        ArrayAdapter getMonthArrayAdapter();

        String spinnerToJodaTime(String parsedDate);
    }
}
