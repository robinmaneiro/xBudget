package com.robin.xBudget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.robin.xBudget.database.DatabaseSchema;

import java.util.List;
import java.util.UUID;

public class DialogCategory extends DialogFragment {

    public final String TAG = this.getClass().getSimpleName();
    public static final String GROUP_ID = "Group_Id";
    public static final String CATEGORY_ID = "TransactionID";
    public Listener mListener;
    private Integer mType, mInitialSpinnerSize;
    private UUID mId;
    public Category mCategory;
    public TextView mTitle, mTextViewCurrency;
    public EditText mEditTextName;
    public EditText mEditTextAllowance;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_category, null);

        mType = (Integer) getArguments().get(GROUP_ID);
        if (getArguments().get(GROUP_ID) != null) mType = getArguments().getInt(GROUP_ID);
        if (getArguments().get(GROUP_ID) != null) mId = (UUID) getArguments().get(CATEGORY_ID);

        mCategory = mListener.getSingleCategory(DatabaseSchema.TransactionTable.mCategories, DatabaseSchema.TransactionTable.CatCols.ID + " = ?", new String[]{mId.toString()});

        //Initialize the elements
        mInitialSpinnerSize = mListener.getMonthArrayAdapter().getCount();
        mTitle = view.findViewById(R.id.text_allowance);
        mEditTextName = view.findViewById(R.id.edit_category);
        mTextViewCurrency = view.findViewById(R.id.text_currency0);
        mEditTextName.setText(mCategory.getName());
        mEditTextAllowance = view.findViewById(R.id.edit_allowance);
        mEditTextAllowance.setText(mCategory.getAmount().toString());


        mTextViewCurrency.setText(MainActivity.CURRENCY_SYMBOL);

        TextView textView = new TextView(getContext());
        textView.setText(mCategory.getName());
        textView.setPadding(50, 30, 20, 30);
        textView.setTextSize(23F);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.carter_one));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), mType == Category.Group.INCOMES ? R.color.incomes_cat : R.color.expenses_cat));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));

        builder.setView(view).setCustomTitle(textView)
                .setPositiveButton(getString(R.string.dialog_positive_update), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(getString(R.string.dialog_category_neutral_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        List<Transaction> transactionsInCategory = mListener.getTransactions(DatabaseSchema.TransactionTable.mTransactions, "category_id = ?", new String[]{mCategory.getId().toString()});

                        if (transactionsInCategory.isEmpty()) {
                            deleteCatUpdateSpinner();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder
                                    .setTitle(R.string.dialog_cat_confirmation_title)
                                    .setMessage(getString(R.string.dialog_category_confirmation_msg1) + transactionsInCategory.size() + getString(R.string.dialog_category_confirmation_msg2))
                                    .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                                    .setNegativeButton(R.string.dialog_negativeButton, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.dialog_category_neutral_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteCatUpdateSpinner();
                                }
                            }).create().show();

                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new CustomListener(alertDialog));


        return alertDialog;
    }


    void deleteCatUpdateSpinner() {
        mListener.deleteCategory(mCategory);

        ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID), mCategory.getDateAssigned().toString());


        // If the category is the last in the selected list, the underlying data is going to be updated to prevent and ArrayIndexOutOfBoundsException
        // Subsequently we are going to select the spinner in the last month in the list

        int finalSpinnerSize = mListener.getMonthArrayAdapter().getCount();
        String currentDateParsed = mListener.getCurrentDateParsed();
        if (mInitialSpinnerSize != finalSpinnerSize) { // Checks if the category was the last in the selected month
            //TransFragment.spinnerAdapter.notifyDataSetChanged();
            //TransFragment.spinnerAdapter.notifyDataSetInvalidated();
            TransFragment.mSpinner.setAdapter(mListener.getMonthArrayAdapter());
            if (mListener.getMonthArrayAdapter().getPosition(currentDateParsed) != -1) { //Check if there is any category in the current month and set the position on it
                TransFragment.mSpinner.setSelection(mListener.getMonthArrayAdapter().getPosition(mListener.getCurrentDateParsed()));
            } else { //If no categories in the selected month the spinner will be set in the last month available
                TransFragment.mSpinner.setSelection(finalSpinnerSize - 1);
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.alpha = 0.90f;
        getDialog().getWindow().setAttributes(lp);

        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement Listener");
        }
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

            } else if (mEditTextAllowance.getText().toString().isEmpty() || Double.parseDouble(mEditTextAllowance.getText().toString()) <= 0) {
                Toast.makeText(getContext(), getString(R.string.dialog_trans_toast_validAmount), Toast.LENGTH_SHORT).show();
                return;
            } else {
                mCategory.setName(mEditTextName.getText().toString());
                mCategory.setAmount(Double.parseDouble(mEditTextAllowance.getText().toString()));
                mListener.updateCategory(mCategory);
                ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID), mCategory.getDateAssigned().toString());
                dialog.dismiss();
            }

        }
    }


    public interface Listener {
        Category getSingleCategory(String table, String whereClause, String[] whereArgs);

        List<Transaction> getTransactions(String table, String whereClause, String[] whereArgs);

        void deleteCategory(Category category);

        void updateCategory(Category category);

        ArrayAdapter getMonthArrayAdapter();

        String getCurrentDateParsed();
    }
}
