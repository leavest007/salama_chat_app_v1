package com.leavest.sahava.salamav1.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.leavest.sahava.salamav1.R;

import javax.annotation.Nullable;

public class ConfirmationDialog extends DialogFragment {
    private String title, message;
    private View.OnClickListener yesClickListener, noClickListener;

    public ConfirmationDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup root;
        View view = inflater.inflate(R.layout.fragment_confirmation, false);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.message)).setText(message);
        view.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                yesClickListener.onClick(view);
            }
        });
        view.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                noClickListener.onClick(view);
            }
        });

        return dialog;
    }

    public static ConfirmationDialog newInstance(String title, String message, View.OnClickListener yesClickListener, View.OnClickListener noClickListener) {
        ConfirmationDialog dialogFragment = new ConfirmationDialog();
        dialogFragment.title = title;
        dialogFragment.message = message;
        dialogFragment.yesClickListener = yesClickListener;
        dialogFragment.noClickListener = noClickListener;
        return dialogFragment;
    }
}
