package com.sohel.uberriderapp.Model;

public class DriverInforModel {
    private  String firstName,lastName,phoneNumber,avater;
    private  double rating;


    public DriverInforModel(){}

    public DriverInforModel(String firstName, String lastName, String phoneNumber, String avater,double rating) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.avater = avater;
        this.rating=rating;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvater() {
        return avater;
    }



    public void setAvater(String avater) {
        this.avater = avater;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
