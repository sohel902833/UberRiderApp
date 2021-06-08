package com.sohel.uberriderapp.Services;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sohel.uberriderapp.Common.Common;
import com.sohel.uberriderapp.Model.TokenModel;

public class UserUtils {

    public static void updateToken(Context context,String token){
        TokenModel tokenModel=new TokenModel(token);
        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFRENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage().toString(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> {

                });
    }
}
