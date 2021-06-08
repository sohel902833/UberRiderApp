package com.sohel.uberriderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sohel.uberriderapp.Common.Common;
import com.sohel.uberriderapp.Model.RiderModel;
import com.sohel.uberriderapp.Services.UserUtils;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEdittext,passwordEdittext;
    private Button loginButton;
    private TextView registerLink;
    private  CustomProgress customProgress;


    private DatabaseReference riderRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();


        riderRef= FirebaseDatabase.getInstance().getReference().child(Common.RIDER_INFO_REF);


        customProgress=new CustomProgress(this);
        emailEdittext=findViewById(R.id.login_EmailEdittextid);
        passwordEdittext=findViewById(R.id.login_PasswordEdittextid);
        loginButton=findViewById(R.id.login_LoginButtonid);
        registerLink=findViewById(R.id.register_link);



        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailEdittext.getText().toString();
                String password=passwordEdittext.getText().toString();
                if(email.isEmpty()){
                    emailEdittext.setError("Enter Your Email");
                    emailEdittext.requestFocus();
                }else if(password.isEmpty()){
                    passwordEdittext.setError("Enter Your Password");
                    passwordEdittext.requestFocus();
                }else{
                    loginUser(email,password);
                }
            }
        });
    }

    private void sendUserToRegisterActivity() {
        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            //Update Token
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(instanceIdResult -> {

                Log.d("Token",instanceIdResult.getToken());
                UserUtils.updateToken(this,instanceIdResult.getToken());

            });

            checkUserFormFirebase();



        }
    }

    private void checkUserFormFirebase() {
        riderRef.child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                         if(snapshot.exists()){
                             RiderModel riderModel=snapshot.getValue(RiderModel.class);
                             sendUserToHomeActivity(riderModel);
                         }else{
                             sendUserToRegisterActivity();
                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void sendUserToHomeActivity(RiderModel riderModel) {

        Common.CURENT_RIDER=riderModel;


        Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser(String email, String password) {
        customProgress.showDiolouge("Logging User","Please Wait");
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        checkUserFormFirebase();
                        customProgress.dismiss();
                    }
                });
    }

}