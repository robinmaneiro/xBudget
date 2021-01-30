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
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.robin.xBudget.database.DatabaseSchema;

import java.util.UUID;

public class DialogCategory extends DialogFragment {

    public final String TAG = this.getClass().getSimpleName();
    public static final String GROUP_ID = "Group_Id";
    public static final String CATEGORY_ID = "TransactionID";
    public Listener mListener;
    private Integer mType,mInitialSpinnerSize;
    private UUID mId;
    public Category mCategory;
    public TextView mTitle, mTextViewCurrency;
    public EditText mName;
    public EditText mAllowance;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_category,null);

        mType = (Integer) getArguments().get(GROUP_ID);
        if (getArguments().get(GROUP_ID) != null) mType = (Integer) getArguments().getInt(GROUP_ID);
        if (getArguments().get(GROUP_ID) != null) mId = (UUID) getArguments().get(CATEGORY_ID);

        mCategory = mListener.getSingleCategory(DatabaseSchema.TransactionTable.mCategories, DatabaseSchema.TransactionTable.CatCols.ID+" = ?",new String[]{mId.toString()});

        //Initialize the elements
        mInitialSpinnerSize = mListener.getMonthArrayAdapter().getCount();
        mTitle = view.findViewById(R.id.text_allowance);
        mName = view.findViewById(R.id.edit_category);
        mTextViewCurrency = view.findViewById(R.id.text_currency0);
        mName.setText(mCategory.getName());
        mAllowance = view.findViewById(R.id.edit_allowance);
        mAllowance.setText(mCategory.getAmount().toString());


        mTextViewCurrency.setText(MainActivity.CURRENCY);

        TextView textView = new TextView(getContext());
        textView.setText(mCategory.getName());
        textView.setPadding(50, 30, 20, 30);
        textView.setTextSize(23F);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.carter_one));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(),mType == Category.Group.INCOMES?R.color.incomes_cat:R.color.expenses_cat));
        textView.setTextColor(ContextCompat.getColor(getContext(),R.color.text_gray));

        builder.setView(view).setCustomTitle(textView)
                .setPositiveButton(getString(R.string.dialog_positive_update), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCategory.setName(mName.getText().toString());
                        mCategory.setAmount(Double.parseDouble(mAllowance.getText().toString()));
                        mListener.updateCategory(mCategory);
                        ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID),mCategory.getDateAssigned().toString());
                    }
                })
                .setNeutralButton(getString(R.string.dialog_category_neutral_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.deleteCategory(mCategory);

                        ((TransFragment.DemoObjectFragment) getTargetFragment()).update((Integer) getArguments().get(GROUP_ID),mCategory.getDateAssigned().toString());

                        int finalSpinnerSize = mListener.getMonthArrayAdapter().getCount();
                        String currentDateParsed = mListener.getCurrentDateParsed();
                        if(mInitialSpinnerSize!=finalSpinnerSize){ // Checks if the category was the last in the selected month
                            //TransFragment.spinnerAdapter.notifyDataSetChanged();
                            //TransFragment.spinnerAdapter.notifyDataSetInvalidated();
                            TransFragment.mSpinner.setAdapter(mListener.getMonthArrayAdapter());
                            if(mListener.getMonthArrayAdapter().getPosition(currentDateParsed)!=-1){ //Check if there is any category in the current month and set the position on it
                                TransFragment.mSpinner.setSelection(mListener.getMonthArrayAdapter().getPosition(mListener.getCurrentDateParsed()));
                            }else{ //If no categories in the selected month the spinner will be set in the last month available
                                TransFragment.mSpinner.setSelection(finalSpinnerSize-1);
                            }
                        }

                        // If the category is the last in the selected list, the underlying data is going to be updated to prevent and ArrayIndexOutOfBoundsException
                        // Subsequently we are going to select the spinner in the last month in the list
                        // int lastPosition = TransFragment.mSpinner.getSelectedItemPosition();

                        //Toast.makeText(getContext(),"spinner size is: "+finalSpinnerSize,Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negativeButton), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
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

    public interface Listener {
        Category getSingleCategory(String table, String whereClause, String[] whereArgs);
        void deleteCategory(Category category);
        void updateCategory(Category category);
        ArrayAdapter getMonthArrayAdapter();
        String getCurrentDateParsed();
    }
}
