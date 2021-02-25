package com.robin.xBudget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;


import com.github.mikephil.charting.utils.Utils;
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class DialogSettings extends DialogFragment {

    Listener mListener;
    public final String TAG = this.getClass().getSimpleName();
   // Set <Currency> mCurrencySet;

    TextView mTextViewValue;
    ImageView mImageViewFlag;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings,null);

        //getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount()-1).getId();

        //TextView for AboutUsDialog title
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.overflow_menu_settings));
        textView.setPadding(50, 30, 20, 30);
        textView.setTextSize(23F);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.carter_one));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.title_orange));
        textView.setTextColor(ContextCompat.getColor(getContext(),R.color.text_gray));

        ExtendedCurrency currency = ExtendedCurrency.getCurrencyByISO(mListener.getConstantValue(MainActivity.CURRENCY_KEY));

        mTextViewValue = view.findViewById(R.id.dialog_settings_currency_value);
        mImageViewFlag = view.findViewById(R.id.dialog_settings_currency_flag);

        mTextViewValue.setText(currency.getName());
        mImageViewFlag.setImageDrawable(getResources().getDrawable(currency.getFlag()));

        CurrencyPicker picker = CurrencyPicker.newInstance("Select Currency");  // dialog title
        picker.setListener(new CurrencyPickerListener() {
            @Override
            public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                // yToast.makeText(getContext(),"Name: "+name+" Code: "+code+"Symbol: "+symbol,Toast.LENGTH_SHORT).show();
                mTextViewValue.setText(name);
                mImageViewFlag.setImageDrawable(getResources().getDrawable(flagDrawableResID));
                mListener.setConstantValue(MainActivity.CURRENCY_KEY, code);
                picker.dismiss();
            }
        });

        mTextViewValue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                picker.show(getFragmentManager(), "CURRENCY_PICKER");
            }
        });

        mImageViewFlag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                picker.show(getFragmentManager(), "CURRENCY_PICKER");

            }
        });

/**
 *
 */

        //List<ExtendedCurrency> currencies = ExtendedCurrency.getAllCurrencies(); //List of all currencies
        //ExtendedCurrency[] currencies = ExtendedCurrency.CURRENCIES; //Array of all currencies

        /*
         //Get currency by its name

        String name = currency.getName();
        String code = currency.getCode();
        int flag = currency.getFlag();  // returns android resource id of flag or -1, if none is associated
        String symbol = currency.getSymbol();

        */
        //currency.loadFlagByCode();  // attempts to associate flag to currency based on its ISO code. Used if you create your own instance of Currency.class

        // mListener.setConstantValue(MainActivity.CURRENCY_KEY,currency.getCode());

        /*
        mSpinner = view.findViewById(R.id.settings_spinner);


        mCurrencySet = Currency.getAvailableCurrencies();

        List<Currency> currencyList = new ArrayList<>(mCurrencySet);

        currencyList.sort(new Comparator<Currency>() {
            @Override
            public int compare(Currency o1, Currency o2) {
                return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
            }
        });
        Locale current = getResources().getConfiguration().locale;

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_dropdown_item, currencyList);

        mSpinner.setAdapter(spinnerArrayAdapter);

        //mSpinner.setSelection(currencyList.indexOf(Currency.getInstance(current)));

        Log.d(TAG, "SUNDAY LOCALE "+current);
        Log.d(TAG, "SUNDAY CURRENCY "+Currency.getInstance(current));

        mSpinner.setSelection(currencyList.indexOf(Currency.getInstance(current)));

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 mListener.setConstantValue(MainActivity.CURRENCY_KEY,Utils.getCurrencySymbol(currencyList.get(i).getCurrencyCode()));

                //Toast.makeText(getContext(),"Symbol: "+ Utils.getCurrencySymbol(currencyList.get(i).getCurrencyCode()),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
         */

        builder.setView(view).setCustomTitle(textView)

                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.dialog_negativeButton,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });



        return builder.create();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        //Toast.makeText(getContext(),childFragment.getId(),Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.alpha=0.90f;
        getDialog().getWindow().setAttributes(layoutParams);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public interface Listener {
        void setConstantValue (String key,String value);
        String getConstantValue (String key );


        }
        @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogSettings.Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement Listener");
        }
    }

}

