package com.sohel.uberriderapp.Model;

import com.firebase.geofire.GeoLocation;

public class DriverGeoModel {
    private String key;
    private GeoLocation geoLocation;
    private  DriverInforModel driverInforModel;

    public DriverGeoModel(){

    }

    public DriverGeoModel(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public DriverInforModel getDriverInforModel() {
        return driverInforModel;
    }

    public void setDriverInforModel(DriverInforModel driverInforModel) {
        this.driverInforModel = driverInforModel;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
