package com.citypop.hit.citypop;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    String Uid;
    String full_name;
    String password;
    String email;
    String phone_num;



    //CONSTRUCTOR
    public User (String full_name, String password, String email, String phone_num) {
        this.Uid = Uid;
        this.full_name = full_name;
        this.password = password;
        this.email = email;
        this.phone_num = phone_num;
    }

    public void SaveToDatabase(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref;
        DatabaseReference usersRef;


        ref = database.getReference("CityPopSERVER/Users");

        usersRef = ref.child(this.Uid);
        usersRef.setValue(this);

    }




    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }





}
