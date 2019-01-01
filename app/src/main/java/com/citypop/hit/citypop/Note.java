package com.citypop.hit.citypop;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Note {

    String NoteUid;
    String userUid;
    String subject;
    String information;
    String fullAddress;
    String city; //for the web admins
    String openingTime;
    String openingDate;
    String ImageUrl; //picture
    String noteStatus;
    double longtitude;
    double latitude;

    public Note(String userUid, String subject, String information, String fullAddress, String city,
                String openingTime, String openingDate, String imageUrl, double longtitude, double latitude) {
        NoteUid =  UUID.randomUUID().toString(); //set a random UID to note
        this.userUid = userUid;
        this.subject = subject;
        this.information = information;
        this.fullAddress = fullAddress;
        this.city = city;
        this.openingTime = openingTime;
        this.openingDate = openingDate;
        this.ImageUrl = imageUrl;
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.noteStatus = "מועבר לרשות המקומית";
    }


    public String getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(String noteStatus) {
        this.noteStatus = noteStatus;
    }

    //this methode saves the data inside firebase database tree
    public void SaveToDatabase(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref;
        DatabaseReference usersRef;

        //CityPopSERVER/Notes
        ref = database.getReference("CityPopSERVER/Notes");

        //primary key NoteUID
        usersRef = ref.child(this.NoteUid);
        usersRef.setValue(this);

    }


    public String getNoteUid() {
        return NoteUid;
    }

    public void setNoteUid(String noteUid) {
        NoteUid = noteUid;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }
}
