package com.sohel.uberriderapp.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sohel.uberriderapp.CallBack.IFirebaseDriverInfoListner;
import com.sohel.uberriderapp.CallBack.IFirebaseFailedListner;
import com.sohel.uberriderapp.Common.Common;
import com.sohel.uberriderapp.Model.AnimationModel;
import com.sohel.uberriderapp.Remote.IGoogleApI;
import com.sohel.uberriderapp.Model.DriverGeoModel;
import com.sohel.uberriderapp.Model.DriverInforModel;
import com.sohel.uberriderapp.Model.GeoQueryModel;
import com.sohel.uberriderapp.R;
import com.sohel.uberriderapp.Remote.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, IFirebaseFailedListner, IFirebaseDriverInfoListner {
    public HomeFragment() {
    }

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    //load Driver
    private double distance = 1.0;//default in km
    private static final double LIMIT_RANGE = 10.0;//km
    private Location previousLocation, currentLocation;
    private  String cityName;


    //

    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    private IGoogleApI iGoogleApI;

    //moving marker
    private List<LatLng> polylineLIst;
    private Handler handler;
    private  int index,next;
    private  LatLng start,end;
    private  float v;
    private double lat,lng;





    //Listner
    IFirebaseDriverInfoListner iFirebaseDriverInfoListner;
    IFirebaseFailedListner iFirebaseFailedListner;

    private boolean firstTime = true;


    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {


        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        init();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    private void init() {

        iGoogleApI= RetrofitClient.getInstance().create(IGoogleApI.class);




        iFirebaseDriverInfoListner = this;
        iFirebaseFailedListner = this;


        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPostition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPostition, 10f));

                //if user has change location,calculate and load driver again
                if (firstTime) {
                    previousLocation = currentLocation = locationResult.getLastLocation();
                    firstTime = false;
                } else {
                    previousLocation = currentLocation;
                    currentLocation = locationResult.getLastLocation();
                }

                if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE)
                    loadAvailableDrivers();
                else {
                    //do nothing
                }
            }
        };


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        loadAvailableDrivers();

    }

    private void loadAvailableDrivers() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
              return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //load all  driver incity
                Geocoder geocoder=new Geocoder(getContext(), Locale.getDefault());
                List<Address>  addressList;
                try{
                    addressList=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                     cityName = addressList.get(0).getLocality();

                     //Query
                    DatabaseReference driverRef= FirebaseDatabase.getInstance()
                            .getReference(Common.DRIVER_LOCATION_REF)
                            .child(cityName);
                    GeoFire gf=new GeoFire(driverRef);
                    GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),distance);

                        geoQuery.removeAllListeners();
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                Common.driversFound.add(new DriverGeoModel(key,location));
                            }

                            @Override
                            public void onKeyExited(String key) {

                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {
                                if(distance<=LIMIT_RANGE){
                                    distance++;
                                    loadAvailableDrivers();
                                }else{
                                    distance=1.0;
                                    addDriverMarker();
                                }
                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                        Snackbar.make(getView(),error.getMessage(),Snackbar.LENGTH_SHORT).show();
                            }
                        });

                        driverRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                //have a new driver
                                GeoQueryModel geoQueryModel=snapshot.getValue(GeoQueryModel.class);
                                GeoLocation geoLocation=new GeoLocation(geoQueryModel.getL().get(0),
                                        geoQueryModel.getL().get(1));
                                DriverGeoModel driverGeoModel=new DriverGeoModel(snapshot.getKey(),geoLocation);
                                Location newDriverLocation=new Location("");
                                newDriverLocation.setLatitude(geoLocation.latitude);
                                newDriverLocation.setLongitude(geoLocation.longitude);
                                float newDistance=location.distanceTo(newDriverLocation)/1000; //in  km
                                if(newDistance<=LIMIT_RANGE)
                                        findDriverByKey(driverGeoModel);


                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                }catch(Exception e){
                    Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void addDriverMarker() {
        if(Common.driversFound.size()>0){
            Observable.fromIterable(Common.driversFound)
                    .subscribeOn(Schedulers.newThread())
                   .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverGeoModel -> {
                        //onNext
                        findDriverByKey(driverGeoModel);
                    },throwable -> {
                        Snackbar.make(getView(),""+throwable.getMessage(),Snackbar.LENGTH_SHORT).show();
                    },()->{
                        Snackbar.make(getView(),"Driver Noti Found",Snackbar.LENGTH_SHORT).show();
                    });



        }else{
            Snackbar.make(getView(),"Driver Noti Found",Snackbar.LENGTH_SHORT).show();
        }


    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            driverGeoModel.setDriverInforModel(snapshot.getValue(DriverInforModel.class));
                            iFirebaseDriverInfoListner.onDriverInfoLoadSuccess(driverGeoModel);

                        }else{
                            iFirebaseFailedListner.onFirebaesLoadFailed(getString(R.string.not_found_key)+driverGeoModel.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListner.onFirebaesLoadFailed(error.getMessage());
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);

                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return false;
                                }
                                fusedLocationProviderClient.getLastLocation()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
                                    }
                                });
                                return true;
                            }
                        });

                        //Layout Button
                        View locationButton =((View)mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //Right Bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,100,300);




                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(getView(),permissionDeniedResponse.getPermissionName()+" need enable",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();
            mMap.getUiSettings().setZoomControlsEnabled(true);


        try {
            boolean success=googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_maps_style));
            if(!success){
                Snackbar.make(getView(),"Load map style failed",Snackbar.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Snackbar.make(getView(),""+e.getMessage(),Snackbar.LENGTH_SHORT).show();
        }

       /*

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }



    @Override
    public void onFirebaesLoadFailed(String message) {
       Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT);

    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        if(!Common.markerList.containsKey(driverGeoModel.getKey()))
            Common.markerList.put(driverGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions().position(new LatLng(driverGeoModel.getGeoLocation().latitude,
                            driverGeoModel.getGeoLocation().longitude))
                            .flat(true)
                            .title(Common.buildName(driverGeoModel.getDriverInforModel().getFirstName(),
                                    driverGeoModel.getDriverInforModel().getLastName()))
                                    .snippet(driverGeoModel.getDriverInforModel().getPhoneNumber())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));

                if(!TextUtils.isEmpty(cityName))
                {
                    DatabaseReference driverLocation=FirebaseDatabase.getInstance()
                            .getReference(Common.DRIVER_LOCATION_REF)
                            .child(cityName)
                            .child(driverGeoModel.getKey());
                    driverLocation.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.hasChildren()){
                                if(Common.markerList.get(driverGeoModel.getKey())!=null){
                                   Common.markerList.get(driverGeoModel.getKey()).remove();
                                   Common.markerList.remove(driverGeoModel.getKey());//Remove marker infor from hash map
                                    Common.driverLocationSubscribe.remove(driverGeoModel.getKey());//Remove Driver Information to
                                   driverLocation.removeEventListener(this);
                                }
                            }else{
                                if(Common.markerList.get(driverGeoModel.getKey())!=null){
                                    GeoQueryModel geoQueryModel=snapshot.getValue(GeoQueryModel.class);
                                    AnimationModel animationModel=new AnimationModel(false,geoQueryModel);
                                    if(Common.driverLocationSubscribe.get(driverGeoModel.getKey())!=null){
                                        Marker currentMarker=Common.markerList.get(driverGeoModel.getKey());
                                        AnimationModel oldPosition=Common.driverLocationSubscribe.get(driverGeoModel.getKey());

                                        String from=new StringBuilder()
                                                .append(oldPosition.getGeoQueryModel().getL().get(0))
                                                .append(",")
                                                .append(oldPosition.getGeoQueryModel().getL().get(1))
                                                .toString();

                                        String to=new StringBuilder()
                                                .append(animationModel.getGeoQueryModel().getL().get(0))
                                                .append(",")
                                                .append(animationModel.getGeoQueryModel().getL().get(1))
                                                .toString();


                                            moveMarkerAnimation(driverGeoModel.getKey(),animationModel,currentMarker,from,to);

                                    }else{
                                        //first location init
                                        Common.driverLocationSubscribe.put(driverGeoModel.getKey(),animationModel);

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(getView(),error.getMessage(),Snackbar.LENGTH_SHORT);
                        }
                    });

                }
    }

    private void moveMarkerAnimation(String key, AnimationModel animationModel, Marker currentMarker, String from, String to) {

        if(animationModel.isRun()){
            //Request Api
            compositeDisposable.add(iGoogleApI.getDirections("driving",
                    "less_driving",
                    from,
                    to,
                    getString(R.string.google_api_key))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(returnResult ->{
                        Log.d("Api_Return",returnResult);

                        try{
                            JSONObject jsonObject=new JSONObject(returnResult);
                            JSONArray jsonArray=jsonObject.getJSONArray("routes");

                            for(int i=0; i<jsonArray.length(); i++){
                                JSONObject route=jsonArray.getJSONObject(i);
                                JSONObject poly=route.getJSONObject("overview_polyline");

                                String polyline=poly.getString("points");
                                polylineLIst=Common.decodePoly(polyline);


                            }
                            //Moving
                            handler=new Handler();
                            index=-1;
                            next=1;
                            Runnable runnable= () -> {
                                if(polylineLIst.size()>1){
                                    if(index<polylineLIst.size()-2){
                                        index++;
                                        next=index;
                                        start=polylineLIst.get(index);
                                        end=polylineLIst.get(next);
                                    }
                                    ValueAnimator valueAnimator=ValueAnimator.ofInt(0,1);
                                    valueAnimator.setDuration(3000);
                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(value -> {
                                          v=value.getAnimatedFraction();
                                          lat=v*end.latitude+(1-v)*start.latitude;
                                          lng=v*end.longitude+(1-v)*start.longitude;
                                          LatLng newPos=new LatLng(lat,lng);
                                          currentMarker.setPosition(newPos);
                                          currentMarker.setAnchor(0.5f,0.5f);
                                          currentMarker.setRotation(Common.getBearing(start,newPos));

                                    });

                                    valueAnimator.start();
                                    if(index<polylineLIst.size()-2)
                                            handler.postDelayed((Runnable) this,1500);
                                    else if(index<polylineLIst.size()-1){
                                        animationModel.setRun(false);
                                        Common.driverLocationSubscribe.put(key,animationModel);//update data
                                    }

                                }
                            };
                            //run handler
                            handler.postDelayed(runnable,1500);


                        }catch (Exception e){
                            Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_LONG).show();
                        }


                    })
            );
        }
    }
}