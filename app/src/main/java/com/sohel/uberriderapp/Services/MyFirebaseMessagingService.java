package com.sohel.uberriderapp.Services;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sohel.uberriderapp.Common.Common;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            UserUtils.updateToken(this,s);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> dataRcv=remoteMessage.getData();
        if(dataRcv!=null){
            Common.showNotification(this,new Random().nextInt(),
                    dataRcv.get(Common.NOTE_TITLE),
                    dataRcv.get(Common.NOTE_CONTENT),
                    null
            );
        }



    }
}
