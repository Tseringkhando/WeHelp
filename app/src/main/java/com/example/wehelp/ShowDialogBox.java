package com.example.wehelp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.wehelp.R;
import com.example.wehelp.Signin;
//this class is created to show message dialogue to the user
public class ShowDialogBox extends AppCompatDialogFragment {
    private String message;
    public ShowDialogBox(String message)
    { this.message= message;}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       dismiss();

                    }
                })
        ;
        // Create the AlertDialog object and return it
        return builder.create();
    }

}

