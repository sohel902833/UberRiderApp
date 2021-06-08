package com.sohel.uberriderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.sohel.uberriderapp.Common.Common;
import com.sohel.uberriderapp.Model.RiderModel;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEdittext,passwordEdittext,firstNameEdittext,lastNameEdittext;
    private Button registerButton;
    private TextView loginLink;
    private  CustomProgress customProgress;

    private DatabaseReference riderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();

        riderRef= FirebaseDatabase.getInstance().getReference().child(Common.RIDER_INFO_REF);

        customProgress=new CustomProgress(this);


        emailEdittext=findViewById(R.id.register_EmailEdittextid);
        passwordEdittext=findViewById(R.id.register_PasswordEdittextid);
        firstNameEdittext=findViewById(R.id.register_FirstNameEdittext);
        lastNameEdittext=findViewById(R.id.register_LastNameEdittext);
        registerButton=findViewById(R.id.register_RegisterButtonid);
        loginLink=findViewById(R.id.login_link);



        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailEdittext.getText().toString();
                String password=passwordEdittext.getText().toString();
                String firstName=firstNameEdittext.getText().toString();
                String lastName=lastNameEdittext.getText().toString();
                if(email.isEmpty()){
                    emailEdittext.setError("Enter Your Email");
                    emailEdittext.requestFocus();
                }else if(password.isEmpty()){
                    passwordEdittext.setError("Enter Your Password");
                    passwordEdittext.requestFocus();
                }else if(firstName.isEmpty()){
                    firstNameEdittext.setError("Enter Your First Name");
                    firstNameEdittext.requestFocus();
                }else if(lastName.isEmpty()){
                    lastNameEdittext.setError("Enter Your Last Name");
                    lastNameEdittext.requestFocus();
                }else{
                    registerUser(email,password,firstName,lastName);
                }
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }
    private void sendUserToHomeActivity(RiderModel riderModel) {
        Common.CURENT_RIDER=riderModel;

        Intent intent=new Intent(RegisterActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser(String email, String password, String firstName, String lastName) {
        customProgress.showDiolouge("Creating New Account","Please Wait.");
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        RiderModel newRider=new RiderModel(firstName,lastName,email);
                        riderRef.child(mAuth.getCurrentUser().getUid()).setValue(newRider)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if ((task.isSuccessful())) {

                                            sendUserToHomeActivity(newRider);
                                            customProgress.dismiss();

                                        }
                                    }
                                });
                    }
                });
    }

}