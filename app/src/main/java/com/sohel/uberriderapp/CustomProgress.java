package com.sohel.uberriderapp;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgress {
    Context context;
    ProgressDialog progressDialog;
    public CustomProgress(Context context){
        this.context=context;
        progressDialog=new ProgressDialog(context);

    }


    public  void showDiolouge(String message, String title){
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();


    }


    public  void dismiss(){
        progressDialog.dismiss();
    }




}
