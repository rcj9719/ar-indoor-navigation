package com.example.rcjoshi.arinphase2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.List;

public class UserGuideAlert extends DialogFragment {

    List<String> mNavInstructions;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setMessage("1. Source and Destination identified" +
                "\n2. Click next for AR")
                .setTitle("Navigation");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        android.app.AlertDialog dialog = builder.create();
        // Create the AlertDialog object and return it
        return dialog;
    }
}
