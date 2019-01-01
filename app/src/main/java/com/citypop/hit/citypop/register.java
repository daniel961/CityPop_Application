package com.citypop.hit.citypop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class register extends AppCompatActivity {

    //vars
    Button back_loginScreen_btn,clear_fields_btn,create_user_btn;
    EditText id_et,full_name_et,password_et,password2_et,email_et,phone_num_et;

    FirebaseAuth auth;
    ProgressDialog progressDialogRegisterUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //refs
        back_loginScreen_btn = (Button)findViewById(R.id.back_toLogin_btn);
        clear_fields_btn = (Button)findViewById(R.id.clear_fields_btn);
        create_user_btn = (Button)findViewById(R.id.create_user_btn);

        full_name_et = (EditText)findViewById(R.id.Et_full_name_register);
        password_et = (EditText)findViewById(R.id.Et_password_register);
        password2_et = (EditText)findViewById(R.id.Et_password2_register);
        email_et = (EditText)findViewById(R.id.Et_email_register);
        phone_num_et = (EditText)findViewById(R.id.Et_phone_register);

        auth = FirebaseAuth.getInstance();




        //back_to_login btn
        back_loginScreen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //we send data for (StartActivityForResult)
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
            }
        });

        //clear all fields BTN
        clear_fields_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                full_name_et.setText("");
                password_et.setText("");
                password2_et.setText("");
                email_et.setText("");
                phone_num_et.setText("");
            }
        });

        //registerBtn
        create_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialogRegisterUser = new ProgressDialog(register.this, AlertDialog.THEME_HOLO_DARK);
                progressDialogRegisterUser.setTitle("בודק נתונים");
                progressDialogRegisterUser.setMessage("אנא המתן...");
                progressDialogRegisterUser.show();
                progressDialogRegisterUser.setCancelable(false);

                startRegisterUser();
            }
        });





    }

    private void startRegisterUser() {

        String email,password;


        if(!password_et.getText().toString().equals(password2_et.getText().toString())){
            Toast.makeText(this, "סיסמאות לא תואמות בשני השדות", Toast.LENGTH_SHORT).show();
            password2_et.setError("לא תואמת לראשונה");
            progressDialogRegisterUser.dismiss();
        }else if(full_name_et.getText().toString().length() <= 3 ||
                 password_et.getText().toString().length() <= 6 ||
                phone_num_et.getText().toString().length() <= 6){
                //Toast.makeText(this, full_name_et.getText().toString().length(), Toast.LENGTH_SHORT).show();
            if (full_name_et.getText().toString().length() <= 3){
                full_name_et.setError("שם קצר מדי אם צריך הוסף שם משפחה");
                progressDialogRegisterUser.dismiss();
            }
            if (password_et.getText().toString().length() <= 6){
                password_et.setError("סיסמא קצרה מדי");
                progressDialogRegisterUser.dismiss();
            }
            if (phone_num_et.getText().toString().length() <= 6){
                phone_num_et.setError("טלפון חייב לכלול לכל הפחות 8 ספרות");
                progressDialogRegisterUser.dismiss();
            }

        }else{
            email = email_et.getText().toString();
            password = password_et.getText().toString();

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if (task.isSuccessful()) {
                        //create user on DataBase And On Auth
                        String full_name = full_name_et.getText().toString();
                        String email = email_et.getText().toString();
                        String password = password_et.getText().toString();
                        String phoneNum = phone_num_et.getText().toString();

                        User user = new User(full_name, password, email, phoneNum);

                        user.setUid(auth.getCurrentUser().getUid());
                        user.SaveToDatabase();
                        Toast.makeText(register.this, "נרשמת בהצלחה למערכת - התחבר כעת", Toast.LENGTH_LONG).show();
                        //todo-go back to user connection like start activity for result
                        //todo-think about email confirmation
                        //for now just go back to the main intent
                        progressDialogRegisterUser.dismiss();

                        //we send the data for the main INTENT  who wait for results
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("EmailToConnect",email);
                        returnIntent.putExtra("PasswordToConnect",password);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();


                    } else {
                        email_et.setError("כתובת אימייל זו כבר רשומה למערכת");
                        Toast.makeText(register.this, "כתובת האימייל שהוזנה נמצאת בשימוש", Toast.LENGTH_LONG).show();
                        progressDialogRegisterUser.dismiss();
                    }


                }

            });


        }

    }


}
