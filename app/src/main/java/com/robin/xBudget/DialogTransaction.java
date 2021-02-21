package com.robin.xBudget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.robin.xBudget.database.DatabaseSchema;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class DialogTransaction extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

    public static final String GROUP_ID = "Group_Id";
    public static final String TRANSACTION_ID = "TransactionID";

    private UUID mId;
    private EditText mEditTextName,  mEditTextAmount, mEditTextDescription, mCategoryAllowance, mCategoryEditText, mCategoryNewAllowance;
    private TextView textViewDialogTitle, mTextViewCurrency0, mTextViewCurrency1, mTextViewCurrency2;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private Spinner mSpinner;
    private List<Category> mCategoriesList;
    private Set mCategoriesSet;
    private Category mCategory;
    private Integer categoryDate;
    private Transaction mTransaction;
    private String mCategoryStringSelected;
    private ArrayAdapter spinnerAdapter;
    private Integer mType;
    LinearLayout newCatLayout;
    private String positiveButton,mTitle;
    private Listener mListener;


    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_transaction, null);


        if (getArguments().get(TRANSACTION_ID) != null) {
            mId = (UUID) getArguments().get(TRANSACTION_ID);
        }

        if (getArguments().get(GROUP_ID) != null) {
            mType = (Integer) getArguments().getInt(GROUP_ID);
            switch (mType) {
                case Category.Group.INCOMES:
                    if(mId!=null)mTitle = getString(R.string.dialog_trans_title_updateIncomeTrans);
                            else mTitle = getString(R.string.dialog_trans_title_newIncomeTrans);
                    break;
                case Category.Group.EXPENSES:
                    if(mId!=null)mTitle = getString(R.string.dialog_trans_title_updateExpenseTrans);
                    else mTitle = getString(R.string.dialog_trans_title_newExpenseTrans);
                    break;
            }
        }

        /**
         * Initialize the elements
         */
        //frag = (SummaryFragment) getTargetFragment();
        mEditTextName = (EditText) view.findViewById(R.id.name_transaction);
        mTextViewCurrency0 = (TextView) view.findViewById(R.id.text_currency0);
        mTextViewCurrency1 = (TextView) view.findViewById(R.id.text_currency1);
        mTextViewCurrency2 = (TextView) view.findViewById(R.id.text_currency2);

        mEditTextAmount = (EditText) view.findViewById(R.id.amount_transaction);
        mEditTextDescription = (EditText) view.findViewById(R.id.transaction_description);
        mDatePicker = (DatePicker) view.findViewById(R.id.trans_datepicker);
        mTimePicker = (TimePicker) view.findViewById(R.id.trans_timepicker);
        mSpinner = (Spinner) view.findViewById(R.id.spinner_dialog_transaction);
        mCategoryEditText = view.findViewById(R.id.category_add_transaction);
        mCategoryAllowance = view.findViewById(R.id.category_allowance);
        mCategoryNewAllowance = view.findViewById(R.id.category_new_allowance);
        categoryDate = Integer.parseInt(mDatePicker.getYear() + "" + (mDatePicker.getMonth() + 1));

        mTimePicker.setIs24HourView(true);

        /**
         * Initialize the adapters
         */

        mCategoriesList = mListener.getCategories(DatabaseSchema.TransactionTable.mCategories, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(getArguments().get(GROUP_ID))});
        mCategoriesSet = new TreeSet();
        for(Category c: mCategoriesList)mCategoriesSet.add(c.getName());
        List<String> mFilteredCategories=new ArrayList<>(mCategoriesSet);
        mFilteredCategories.add("< "+getString(R.string.dialog_trans_spinner_new_category)+" >");

        spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, mFilteredCategories);
        mSpinner.setAdapter(spinnerAdapter);

        newCatLayout = (LinearLayout) view.findViewById(R.id.layout_add_category);

        mCategoryAllowance.setHint(mType==Category.Group.INCOMES?getString(R.string.dialog_trans_hint_cat_expected):getString(R.string.dialog_trans_hint_cat_budget));
        mCategoryNewAllowance.setHint(mType==Category.Group.INCOMES?getString(R.string.dialog_trans_hint_cat_expected):getString(R.string.dialog_trans_hint_cat_budget));

        //Either creating a new Transaction object or looking for an existing one by its 'mId'
        if (mId == null) {
            mTransaction = new Transaction();
            positiveButton = getString(R.string.dialog_transaction_positive_add);
        } else {
            //Log.d(TAG, "id is: " + mId);

            mTransaction = mListener.getSingleTransaction(DatabaseSchema.TransactionTable.mTransactions, "id = ?", new String[]{mId.toString()});

            //Log.d(TAG, "name is: " + mTransaction);

            mEditTextName.setText(mTransaction.getName());
            mEditTextAmount.setText(mTransaction.getAmount().toString());
            mEditTextDescription.setText(mTransaction.getDescription());

            DateTime dt = mTransaction.getDateTime();
            mDatePicker.init(dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth(), null);
            mTimePicker.setHour(dt.getHourOfDay());
            mTimePicker.setMinute(dt.getMinuteOfHour());

            Category category = mListener.getSingleCategory(DatabaseSchema.TransactionTable.mCategories,
                    DatabaseSchema.TransactionTable.CatCols.ID+ " = ?",
                    new String[]{mTransaction.getCategoryId().toString()});
            mSpinner.setSelection(mFilteredCategories.indexOf(category.getName()));

            positiveButton = getString(R.string.dialog_positive_update);
        }



        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == mFilteredCategories.size() - 1) {
                    //Toast.makeText(getContext(),"Other selected",Toast.LENGTH_SHORT).show();
                    mCategoryNewAllowance.setVisibility(View.INVISIBLE);
                    newCatLayout.setVisibility(View.VISIBLE);
                } else {
                    mCategoryStringSelected = (String) mSpinner.getAdapter().getItem(position);
                    categoryDate = Integer.parseInt(mDatePicker.getYear() + "" + (mDatePicker.getMonth() + 1));

                    //CHECK IF THE CATEGORY EXIST
                    Boolean categoryExist = mListener.checkIfCategoryExists(DatabaseSchema.TransactionTable.mCategories, mCategoryStringSelected, categoryDate);
                    newCatLayout.setVisibility(View.INVISIBLE);
                    if(!categoryExist) {
                        mCategoryNewAllowance.setVisibility(View.VISIBLE);
                        mTextViewCurrency1.setVisibility(View.VISIBLE);
                    }else {
                        mCategoryNewAllowance.setVisibility(View.INVISIBLE);
                        mTextViewCurrency1.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                categoryDate = Integer.parseInt(mDatePicker.getYear() + "" + (mDatePicker.getMonth() + 1));

                //CHECK IF THE CATEGORY EXIST
                Boolean categoryExist = mListener.checkIfCategoryExists(DatabaseSchema.TransactionTable.mCategories, mCategoryStringSelected, categoryDate);

                if(mSpinner.getSelectedItemPosition()!=mFilteredCategories.size()-1) {
                    newCatLayout.setVisibility(View.INVISIBLE);
                    if (!categoryExist) {
                        mCategoryNewAllowance.setVisibility(View.VISIBLE);
                        mTextViewCurrency1.setVisibility(View.VISIBLE);

                    } else {
                        mCategoryNewAllowance.setVisibility(View.INVISIBLE);
                        mTextViewCurrency1.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        mTextViewCurrency0.setText(MainActivity.CURRENCY_SYMBOL);
        mTextViewCurrency1.setText(MainActivity.CURRENCY_SYMBOL);
        mTextViewCurrency2.setText(MainActivity.CURRENCY_SYMBOL);


        textViewDialogTitle = new TextView(getContext());
        textViewDialogTitle.setText(mTitle);
        textViewDialogTitle.setPadding(50, 30, 20, 30);
        textViewDialogTitle.setTextSize(23F);
        textViewDialogTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textViewDialogTitle.setTypeface(ResourcesCompat.getFont(getContext(), R.font.carter_one));
        textViewDialogTitle.setBackgroundColor(ContextCompat.getColor(getContext(),mType == Category.Group.INCOMES?R.color.incomes_cat:R.color.expenses_cat));
        textViewDialogTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.text_gray));

        builder.setView(view)
                .setCustomTitle(textViewDialogTitle)
                //.setMessage("Add the details of the transaction")
                .setNegativeButton(R.string.dialog_negativeButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        if (mId != null) {
            builder.setNeutralButton(getString(R.string.dialog_category_neutral_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.deleteTransaction(mTransaction);
                    ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID),mListener.datePickerToJodaTime(mDatePicker.getYear()+" "+(mDatePicker.getMonth()+1)));
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new CustomListener(alertDialog));
        return alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.alpha=0.90f;
        getDialog().getWindow().setAttributes(lp);

        //getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;

        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            if (mEditTextName.getText().toString().isEmpty() || mEditTextName.toString() == null) {
                Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_validName), Toast.LENGTH_SHORT).show();
                return;

            } else if (mEditTextAmount.getText().toString().isEmpty() || Double.parseDouble(mEditTextAmount.getText().toString()) <= 0) {
                Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_validAmount), Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCatLayout.getVisibility() == View.VISIBLE) {

                if (mCategoryEditText.getText().toString().isEmpty() || mCategoryAllowance.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_bothFields), Toast.LENGTH_SHORT).show();
                    return;
                }
               else if (!mCategoryEditText.getText().toString().isEmpty() && !mCategoryAllowance.getText().toString().isEmpty()) {
                    mCategory = new Category();
                    mCategory.setName(mCategoryEditText.getText().toString());
                    mCategory.setAmount(Double.parseDouble(mCategoryAllowance.getText().toString()));
                    mCategory.setGroupId(getArguments().getInt(GROUP_ID));
                    mCategory.setDateAssigned(categoryDate);
                    if (!mCategoriesList.contains(mCategory)) {
                        mListener.insertCategory(mCategory);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_categoryExists), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else{
                //CHECK IF THE CATEGORY EXIST
                //Boolean categoryExist = mListener.checkIfCategoryExists(DatabaseSchema.TransactionTable.mCategories, mCategoryStringSelected, categoryDate);

                if(mCategoryNewAllowance.getVisibility()==View.VISIBLE){
                    if (mCategoryNewAllowance.getText().toString().isEmpty()){
                        Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_categoryBudget), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCategory = new Category();
                    mCategory.setName(mCategoryStringSelected);
                    mCategory.setAmount(Double.parseDouble(mCategoryNewAllowance.getText().toString()));
                    mCategory.setGroupId(getArguments().getInt(GROUP_ID));
                    mCategory.setDateAssigned(categoryDate);

                    mListener.insertCategory(mCategory);

                } else {
                    mCategory = mListener.getSingleCategory(DatabaseSchema.TransactionTable.mCategories,
                            DatabaseSchema.TransactionTable.CatCols.NAME+" = ? AND CAST("+ DatabaseSchema.TransactionTable.CatCols.DATE+" as TEXT) = ?",
                            new String[]{mCategoryStringSelected,String.valueOf(categoryDate)});

                }
            }

            if(mCategory.getId()!=null) {
                mTransaction.setCategoryId(mCategory.getId());
                mTransaction.setGroupId(getArguments().getInt(GROUP_ID));
                mTransaction.setName(mEditTextName.getText().toString());
                String amount = mEditTextAmount.getText().toString();
                mTransaction.setAmount(Double.parseDouble(amount == null || amount.equals("") ? "0" : amount));
                mTransaction.setDescription(mEditTextDescription.getText().toString());
                DateTime dt = new DateTime(
                        mDatePicker.getYear(),
                        mDatePicker.getMonth() + 1,
                        mDatePicker.getDayOfMonth(),
                        mTimePicker.getHour(),
                        mTimePicker.getMinute());

                mTransaction.setDate(dt);

                if (mId == null) {
                    mListener.insertTransaction(mTransaction);
                } else {
                    mListener.updateTransaction(mTransaction);
                }
                ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID),mListener.datePickerToJodaTime(mDatePicker.getYear()+" "+(mDatePicker.getMonth()+1)));

                TransFragment.spinnerAdapter.clear();
                TransFragment.spinnerAdapter.addAll(mListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories,null,null));
                TransFragment.mSpinner
                        .setSelection(mListener.getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories,null,null)
                        .indexOf(DateTimeFormat.forPattern("MMM yyyy").print(mTransaction.getDateTime())));
                dialog.dismiss();
                }
            }
        }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogTransaction.Listener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("Class must implement DialogTransaction.Listener");
        }
    }

    public interface Listener {
        List<Category> getCategories(String table, String whereClause, String[] whereArgs);
        List<Transaction> getTransactions(String table, String whereClause, String[] whereArgs);
        boolean checkIfCategoryExists(String TableName, String categoryName, int categoryDate);

        Transaction getSingleTransaction(String table, String whereClause, String[] whereArgs);
        Category getSingleCategory(String table, String whereClause, String[] whereArgs);

        void insertTransaction(Transaction transaction);
        void updateTransaction(Transaction transaction);
        void deleteTransaction(Transaction transaction);

        void insertCategory(Category category);

        String datePickerToJodaTime(String parsedDate);

        ArrayAdapter getMonthArrayAdapter();
        List<String> getParsedMonthDates(String table, String whereClause, String[] whereArgs);


    }
}
