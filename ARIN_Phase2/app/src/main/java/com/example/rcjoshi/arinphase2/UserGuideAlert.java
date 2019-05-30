package com.example.rcjoshi.arinphase2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

public class UserGuideAlert extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(getActivity());

        builderSingle.setIcon(R.drawable.ic_dashboard_black_24dp);
        builderSingle.setTitle("User Guide");
//        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        arrayAdapter.add("1. Point to floor");
        arrayAdapter.add("2. Detect surface");
        arrayAdapter.add("3. Wait for AR");
        arrayAdapter.add("4. Experience Navigation");

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
            }
        });

        builderSingle.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });

        android.app.AlertDialog dialog = builderSingle.create();
        // Create the AlertDialog object and return it
        return dialog;
    }
}
