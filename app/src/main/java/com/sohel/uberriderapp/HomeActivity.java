package com.sohel.uberriderapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sohel.uberriderapp.Common.Common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private DatabaseReference driverLocationRef;
    private DatabaseReference riderRef;
    private DrawerLayout drawer;
    private   NavigationView navigationView;
    private   NavController navController;
    private Uri imageUri;


    private CircleImageView img_avater;
    private static final int PICK_IMAGE_REQUEST=2244;
    private  AlertDialog watingDialouge;
    private  StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth=FirebaseAuth.getInstance();
     /*   driverLocationRef= FirebaseDatabase.getInstance().getReference().child(Common.DRIVER_LOCATION_REF);

*/
        riderRef= FirebaseDatabase.getInstance().getReference().child(Common.RIDER_INFO_REF);
        storageReference= FirebaseStorage.getInstance().getReference();



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.nav_signOut){

                    AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Sign Out")
                                .setMessage("Do You Really want to sign out?")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).setCancelable(false);

                    AlertDialog dialog=builder.create();
                    dialog.setOnShowListener(dialog1 -> {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(HomeActivity.this, android.R.color.holo_red_dark));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(HomeActivity.this,R.color.colorAccent));

                    });

                    dialog.show();

                 /*   driverLocationRef.child(mAuth.getCurrentUser().getUid())
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mAuth.signOut();

                        }
                    });*/
                }
                return true;
            }
        });

        
        init();



        img_avater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK){
            if(data !=null && data.getData() !=null){
                imageUri=data.getData();
                img_avater.setImageURI(imageUri);

                showDialogUpload();

            }
        }


    }

    private void showDialogUpload() {

        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Change Avatar")
                .setMessage("Do You Really want to change avatar?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                    }
                }).setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(imageUri != null){
                    watingDialouge.setMessage("Uploading....");
                    watingDialouge.show();

                    String unique_name=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    StorageReference avatarFolder=storageReference.child("avatars/"+unique_name+System.currentTimeMillis());


                    avatarFolder.putFile(imageUri)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    watingDialouge.dismiss();
                                    Snackbar.make(drawer,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                avatarFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        riderRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("avater")
                                                .setValue(uri.toString());
                                    }
                                });
                            }
                        }
                    });



                }
            }
        });



    }

    private void init() {
        watingDialouge=new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Wating..")
                .create();




        View headerView=navigationView.getHeaderView(0);
        TextView txt_name=headerView.findViewById(R.id.text_name);
        TextView txt_Phone=headerView.findViewById(R.id.text_phone);
        img_avater=headerView.findViewById(R.id.img_avatar);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_Phone.setText(Common.CURENT_RIDER!=null?Common.CURENT_RIDER.getPhoneNumber():"");

        if(Common.CURENT_RIDER!=null && Common.CURENT_RIDER.getAvater()!=null &&  !TextUtils.isEmpty(Common.CURENT_RIDER.getAvater())){
            Glide.with(this)
                    .load(Common.CURENT_RIDER.getAvater())
                    .placeholder(R.drawable.pickup_icon)
                    .into(img_avater);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}