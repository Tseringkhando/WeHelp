package com.example.wehelp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;

public class SigninProcess {
    private Activity activity;
    private AlertDialog alert;

    public SigninProcess(Activity a){
        this.activity=a;
    }

    public void showProgressDialog()
    {

        AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        LayoutInflater inflater =activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.activity_signin_process, null));
        alert =builder.create();
        alert.show();
    }

    public void dismissProgressDialog()
    {
        alert.dismiss();
    }

}