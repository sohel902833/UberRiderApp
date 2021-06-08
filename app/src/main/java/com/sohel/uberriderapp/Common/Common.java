package com.sohel.uberriderapp.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sohel.uberriderapp.Model.AnimationModel;
import com.sohel.uberriderapp.Model.DriverGeoModel;
import com.sohel.uberriderapp.Model.RiderModel;
import com.sohel.uberriderapp.R;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String RIDER_INFO_REF="Riders";
    public static final String TOKEN_REFRENCE = "Token";
    public static final String NOTE_TITLE = "title";
    public static final String NOTE_CONTENT = "body";
    public static  final String DRIVER_INFO_REFERENCE="DriverInfo";
    public static final String DRIVER_LOCATION_REF = "DriversLocation";
    public static RiderModel CURENT_RIDER;
    public static Set<DriverGeoModel> driversFound=new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> markerList=new HashMap<>();
    public static HashMap<String, AnimationModel> driverLocationSubscribe=new HashMap<>();

    public static String buildWelcomeMessage(){
        if(Common.CURENT_RIDER !=null){
            return new StringBuilder("Welcome")
                    .append(Common.CURENT_RIDER.getFirstName())
                    .append(" ")
                    .append(Common.CURENT_RIDER.getLastName()).toString();
        }else{
            return  "";
        }
    }





    public static void showNotification(Context context, int id, String title, String body, Intent intent) {

        PendingIntent pendingIntent=null;

        if(intent !=null)
            pendingIntent=PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID="sohel_rana_sompod_ridoy";

        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Uber Remake",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Uber Remake");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,100,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.car_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.car_icon));

        if(pendingIntent !=null){
            builder.setContentIntent(pendingIntent);
        }
        Notification notification=builder.build();
        notificationManager.notify(id,notification);
    }


    public static String buildName(String firstName, String lastName) {
        return  new StringBuilder(firstName).append(" ").append(lastName).toString();
    }

    //GET BEARING
    public static float getBearing(LatLng begin, LatLng end) {
        //You can copy this function by link at description
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    //DECODE POLY
    public static List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index=0,len=encoded.length();
        int lat=0,lng=0;
        while(index < len)
        {
            int b,shift=0,result=0;
            do{
                b=encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;

            }while(b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do{
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift +=5;
            }while(b >= 0x20);
            int dlng = ((result & 1)!=0 ? ~(result >> 1): (result >> 1));
            lng +=dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }
}
