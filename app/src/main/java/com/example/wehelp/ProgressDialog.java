package com.example.wehelp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class ProgressDialog {
    private Activity activity;
    private AlertDialog alert;
    public ProgressDialog(Activity a){
        this.activity=a;
    }
    public void showProgressDialog()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        LayoutInflater inflater =activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progressdialog, null));
        alert =builder.create();
        alert.show();
    }

    public void dismissProgressDialog()
    {
        alert.dismiss();
    }



}
