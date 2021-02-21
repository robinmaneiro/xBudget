package com.robin.xBudget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
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

public class DialogAboutUs extends DialogFragment {

    public final String TAG = this.getClass().getSimpleName();
    public TextView mTextViewLinkLinkedIn;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_aboutus,null);

        //TextView for AboutUsDialog title
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.overflow_menu_about));
        textView.setPadding(50, 30, 20, 30);
        textView.setTextSize(23F);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.carter_one));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.title_orange));
        textView.setTextColor(ContextCompat.getColor(getContext(),R.color.text_gray));

        builder.setView(view).setCustomTitle(textView)
                .setPositiveButton(R.string.dialog_okbutton, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        mTextViewLinkLinkedIn = view.findViewById(R.id.txt_link_linkedin);
        mTextViewLinkLinkedIn.setMovementMethod(LinkMovementMethod.getInstance());

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
}