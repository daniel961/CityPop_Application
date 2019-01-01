package com.citypop.hit.citypop;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LoginScreen extends AppCompatActivity implements OnMapReadyCallback {

    //todo- compress image before upload
    //todo- set all notes on map with images
    //todo - make user menu 1. disconnect . 2. change password 3. look at notes he open(add option to cancel notes he open) 4.exit from app
    //todo -Officers in the City make sample of offices array
    //todo -CityUpdates make intent with getting updates. check in the array of updates -> cityname -> listOfUpdates



    //loginScreenVars
    private Button submitNoteToServer, close_NoteForm_btn, login_intent_CreateUser_btn, login_intent_SubmitLogin_btn, open_system_malfunction_btn, show_inspectors_onMap_btn, show_block_roads_onMap_btn, show_Town_updates_btn;
    private EditText Et_email_login, Et_password_login;
    private FirebaseAuth auth;
    private ImageView NotePicture;



    private String email, password;
    private TextView HavePictureTV;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int REGISTER_INTENT_CODE = 10;
    String noteSubject, noteExplained, noteAddress, noteDateNtime, photoDownloadUrl, CurrentDate, CurrentHour;
    String mCurrentPhotoPath;
    private EditText Et_subject_note, Et_information_note;
    private StorageReference mStorage;


    RelativeLayout LoginLayout;

    int refreshCounter = 0;
    String TAG = "log Test";
    TextView Gps_location_TV, addressTV, timeTV;
    boolean isActionsMenuOpen = false, GpsSignal = false, isLoginScreenOpen = false, isUserConnected = false, isNoteLayoutOpen = false, isImageCaptured = false, isNoteDatabaseObtained = false,isInspectorDatabaseObtained = false;
    boolean inspectorsFirstTimeRun = true,moveCamerafirstTime = false;
    Button ActionsBTN, MapMoveLocationBtn, loginSystem_btn, close_login_screen_btn, connected_btn, takePictureNodeBtn;
    LinearLayout menu_Actions_layout, NoteLayout;
    private GoogleMap mMap;
    //this brodcast reciver we register to get locations updates
    private BroadcastReceiver broadcastReceiver;
    Handler handler = new Handler();

    Geocoder geocoder;
    List<android.location.Address> addresses = null;

    ProgressDialog progressDialog;
    ProgressDialog progressDialogUploadingPhoto;
    ProgressDialog progressDialogInspector;

    //all notes list from server
    private List<Note> AllNotesList = new ArrayList<Note>();

    //marker
    Marker selfMarker;
    Circle selfCircle;




    //bitmap for the map
    BitmapDescriptor PersonIcon;
    BitmapDescriptor InspectorIcon;
    BitmapDescriptor warning_sign;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //to avoid memory leaks we unregister the reciver when the app close
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // here we register the Broadcast reciver
        //and we check first if the register is exist or not if he is not exist we register him
        //else we dont need to register
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // we need to check if there is GPS signal
                    GpsSignal = true;

                    //here we can get the values from the reciver
                    /*
                    Toast.makeText(context, "reciver called " + (double)intent.getExtras().get("latitude")+"  "+
                    (double)intent.getExtras().get("longitude") , Toast.LENGTH_SHORT).show();
                    */

                    // move the GPS Camera to the right position
                    if(moveCamerafirstTime == false) {
                        cameraMoveCurrectLocation((double) intent.getExtras().get("latitude"),
                                (double) intent.getExtras().get("longitude"));
                        moveCamerafirstTime = true;
                    }else{
                        MapUpdate((double) intent.getExtras().get("latitude"),
                                (double) intent.getExtras().get("longitude"));
                    }
                    // Update the UI text Address line
                    UpdateAddressUI((double) intent.getExtras().get("latitude"),
                            (double) intent.getExtras().get("longitude"));

                    //this will get Address from GEOCODER
                    //getAddressFromLongLat(latitude,longitude);
                    //save the Variables
                    longitude = (double) intent.getExtras().get("longitude");
                    latitude = (double) intent.getExtras().get("latitude");


                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
        }
    }


    //for the exmple lanLat
    public LatLng holon = new LatLng(32.0112, 34.7748);
    //public LatLng currentLocation = new LatLng();
    double longitude = 34.7748;
    double latitude = 32.0112;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // make istance of Geocoder use it next
        geocoder = new Geocoder(this, Locale.getDefault());

        //installs & Downloads
        getNoteDatabase();
        //install bitmap
        PersonIcon = BitmapDescriptorFactory.fromResource(R.drawable.boymarker);
        InspectorIcon = BitmapDescriptorFactory.fromResource(R.drawable.policeman_icon);
        warning_sign = BitmapDescriptorFactory.fromResource(R.drawable.warning_sign);


        //GlobalRefs
        LoginLayout = (RelativeLayout) findViewById(R.id.LoginLayout);
        menu_Actions_layout = (LinearLayout) findViewById(R.id.menu_Actions_layout);
        ActionsBTN = (Button) findViewById(R.id.ActionsBtn);
        MapMoveLocationBtn = (Button) findViewById(R.id.MapMoveLocationBtn);
        Gps_location_TV = (TextView) findViewById(R.id.Gps_location_TV);


        //LoginScreenRefs
        login_intent_CreateUser_btn = (Button) findViewById(R.id.login_intent_CreateUser_btn);
        login_intent_SubmitLogin_btn = (Button) findViewById(R.id.login_intent_SubmitLogin_btn);
        connected_btn = (Button) findViewById(R.id.connected_btn);
        Et_email_login = (EditText) findViewById(R.id.Et_email_login);
        Et_password_login = (EditText) findViewById(R.id.Et_password_login);
        loginSystem_btn = (Button) findViewById(R.id.loginSystem_btn);
        close_login_screen_btn = (Button) findViewById(R.id.close_login_screen_btn);

        //NoteRefs
        NoteLayout = (LinearLayout) findViewById(R.id.NoteLayout);
        close_NoteForm_btn = (Button) findViewById(R.id.close_NoteForm_btn);
        timeTV = (TextView) findViewById(R.id.timeTV);
        addressTV = (TextView) findViewById(R.id.addressTV);
        takePictureNodeBtn = (Button) findViewById(R.id.takePictureNodeBtn);
        NotePicture = (ImageView) findViewById(R.id.NotePicture);
        HavePictureTV = (TextView) findViewById(R.id.HavePictureTV);
        submitNoteToServer = (Button) findViewById(R.id.submitNoteToServer);
        Et_subject_note = (EditText) findViewById(R.id.Et_subject_note);
        Et_information_note = (EditText) findViewById(R.id.Et_information_note);


        //ActionBarRefs
        open_system_malfunction_btn = (Button) findViewById(R.id.open_system_malfunction_btn);
        show_inspectors_onMap_btn = (Button) findViewById(R.id.show_inspectors_onMap_btn);
        show_block_roads_onMap_btn = (Button) findViewById(R.id.show_block_roads_onMap_btn);
        show_Town_updates_btn = (Button) findViewById(R.id.show_Town_updates_btn);


        setupFirebaseStorage();
        auth = FirebaseAuth.getInstance();


        ////////LoginScreen//////////////LoginScreen//////////////LoginScreen//////////////LoginScreen//////////////LoginScreen//////


        //todo-Automatic login !
        //todo-User-menu - which there he can see his taklot his open / chenge password and more


        login_intent_SubmitLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });


        login_intent_CreateUser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Register_Intent = new Intent(LoginScreen.this, register.class);
                startActivityForResult(Register_Intent,REGISTER_INTENT_CODE);
            }
        });


        //connect to system btn
        loginSystem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginScreenOpen == false) {
                    LoginLayout.setVisibility(View.VISIBLE);
                }
                isLoginScreenOpen = true;
            }
        });

        //close login screen btn
        close_login_screen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginScreenOpen == true) {
                    LoginLayout.setVisibility(View.GONE);
                }
                isLoginScreenOpen = false;
            }
        });


        ////////End OF LoginScreen////// ////////End OF LoginScreen////// ////////End OF LoginScreen////// ////////End OF LoginScreen//////


        ////////ActionBar//////////////ActionBar//////////////ActionBar//////////////ActionBar//////////////ActionBar//////////////ActionBar//////


        //Bar Btns Menu
        open_system_malfunction_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserConnected == false) {
                    ActionBarRegisterDialog();
                }/*else if(GpsSignal == false){
                    ActionBarLocationDialog();
                } */ else {
                    //todo-open note form
                    if (isNoteLayoutOpen == false) {
                        isNoteLayoutOpen = true;
                        NoteLayout.setVisibility(View.VISIBLE);
                        //show the current location on note
                        addressTV.setText("כתובת: \n" +
                                Gps_location_TV.getText().toString());
                    }
                }
            }
        });

        show_inspectors_onMap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserConnected == false) {
                    ActionBarRegisterDialog();
                } else {
                    getInspectorDatabase();

                }
            }
        });

        show_block_roads_onMap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserConnected == false) {
                    ActionBarRegisterDialog();
                } else {
                    //do stuff
                }
            }
        });

        show_Town_updates_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserConnected == false) {
                    ActionBarRegisterDialog();
                } else {
                    Intent cityUpdatesIntent = new Intent(LoginScreen.this, cityUpdates.class);
                    startActivity(cityUpdatesIntent);
                }
            }
        });


        ////////End OF ActionBar//////////////End OF ActionBar//////////////End OF ActionBar//////////////End OF ActionBar//////


        //////NoteLayout///////////NoteLayout///////////NoteLayout///////////NoteLayout///////////NoteLayout///////////NoteLayout/////


        close_NoteForm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel note
                isImageCaptured = false;
                isNoteLayoutOpen = false;
                HavePictureTV.setVisibility(View.GONE);
                photoDownloadUrl = null;
                noteSubject = null;
                noteExplained = null;
                noteAddress = null;
                noteDateNtime = null;
                Et_information_note.setText("");
                Et_subject_note.setText("");
                NotePicture.setImageBitmap(null);
                NoteLayout.setVisibility(View.GONE);
            }
        });

        //get the current time from calendar
        String currentDateandTime = new SimpleDateFormat("dd.MM.yyyy       HH:mm").format(new Date());
        CurrentDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        CurrentHour = new SimpleDateFormat("HH:mm").format(new Date());

        timeTV.setText("תאריך ושעת פתיחת תקלה:\n" +
                "" + currentDateandTime);

        //Camera intent and save the picture inside the storage
        takePictureNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent CameraOpenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (CameraOpenIntent.resolveActivity(getPackageManager()) != null) {
                    //now create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        //Error Occurred while creating the file
                        Log.i("mylog", "Photo storage IO Exception");
                    }
                    //continue only if the File was successfully created
                    if (photoFile != null) {
                        /*Its not the right way to solve this, this will basically remove the strictmode policies.
                        and will ignore the security warning*/
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        ///// but we use this ignore StrictMode
                        CameraOpenIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(CameraOpenIntent, CAMERA_REQUEST_CODE);
                    }
                }


            }
        });


        submitNoteToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check all fields
                //check if image captured
                noteSubject = Et_subject_note.getText().toString();
                noteExplained = Et_information_note.getText().toString();


                if (isImageCaptured == true && noteSubject.length() >= 5 && noteExplained.length() >= 10) {
                    //evrything good to OK the Note
                    //upload the image
                    progressDialogUploadingPhoto.show();
                    Uri imagePath = Uri.parse(mCurrentPhotoPath);
                    final StorageReference filePath = mStorage.child("CityNotesPhotos").child(imagePath.getLastPathSegment());
                    filePath.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            photoDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                            Log.i("downloadLink", photoDownloadUrl);
                            Toast.makeText(LoginScreen.this, "בקשתך נשלחה בהצלחה לרשות האחראית", Toast.LENGTH_LONG).show();

                            if (auth != null) {
                                Note note = new Note(auth.getCurrentUser().getUid(), noteSubject, noteExplained, Gps_location_TV.getText().toString(), "nullForNow",
                                        CurrentHour, CurrentDate, photoDownloadUrl, longitude, latitude);
                                note.SaveToDatabase();
                            } else {
                                Log.e("myerror", "user Is not connected or other error");

                            }
                            //todo - set all variebles of note form to default after finish the job
                            isImageCaptured = false;
                            isNoteLayoutOpen = false;
                            HavePictureTV.setVisibility(View.GONE);
                            photoDownloadUrl = null;
                            noteSubject = null;
                            noteExplained = null;
                            noteAddress = null;
                            noteDateNtime = null;
                            Et_information_note.setText("");
                            Et_subject_note.setText("");
                            NotePicture.setImageBitmap(null);
                            NoteLayout.setVisibility(View.GONE);
                            progressDialogUploadingPhoto.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginScreen.this, "אירעה שגיאה בעת העלאת התמונה לשרתינו", Toast.LENGTH_SHORT).show();
                            isImageCaptured = false;
                            isNoteLayoutOpen = false;
                            HavePictureTV.setVisibility(View.GONE);
                            photoDownloadUrl = null;
                            noteSubject = null;
                            noteExplained = null;
                            noteAddress = null;
                            noteDateNtime = null;
                            Et_information_note.setText("");
                            Et_subject_note.setText("");
                            NotePicture.setImageBitmap(null);
                            NoteLayout.setVisibility(View.GONE);
                            progressDialogUploadingPhoto.dismiss();
                        }
                    });


                    //make instance of new note object
                    //save the url to the note
                    //make sure that the id of the note is Unique
                    //make sure inside the note will be the user id (so you can find his notes next time)

                } else {
                    NoteErrorDialog();
                }


            }
        });


        //////End OF NoteLayout///////////End OF NoteLayout///////////End OF NoteLayout///////////End OF NoteLayout/////


        //////User Options Menu on top/////////////////User Options Menu on top/////////////////User Options Menu on top///////////


        ////// END OF User Options Menu on top///////////////// END OF User Options Menu on top///////////////// END OF User Options Menu on top///////////


        //googleMap Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.My_map);
        mapFragment.getMapAsync(this);


        //actions BTN OPEN Actions menu and close
        ActionsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCloseActionMenu();
                if (isUserConnected == false && isActionsMenuOpen == true) {
                    Toast.makeText(LoginScreen.this, "אפשרויות למשתמשים רשומים בלבד!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        //mapCamera move to the currect location //mirkuz
        MapMoveLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraMoveCurrectLocation(latitude, longitude);
                //Toast.makeText(LoginScreen.this, " מיכרוז   long = " + logitude + " lat = " +latitude , Toast.LENGTH_SHORT).show();
            }
        });


        //check for userPermisions and request permisions for GPS if needed
        if (!runtime_permissions()) {
            startService();
        } else {
            Toast.makeText(this, "יש לתת הרשאות לצורך שימוש בשירות", Toast.LENGTH_SHORT).show();
        }


        progressDialogUploadingPhoto = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
        progressDialogUploadingPhoto.setTitle("שולח טופס לשרת אנא המתן...");
        progressDialogUploadingPhoto.setMessage("זה עשוי לקחת קצת זמן");
        progressDialogUploadingPhoto.setCancelable(false);


        progressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
        progressDialog.setMessage("המתן בזמן שאנחנו מאתרים את מיקומך על המפה שלנו...");

        progressDialog.show();
        progressDialog.setCancelable(false);


        //after 3 secounds closes the progress dialog
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //Create an Intent that will start the Menu-Activity.

                progressDialog.dismiss();
                if (GpsSignal == false) {
                    Toast.makeText(LoginScreen.this, "אין קליטת GPS", Toast.LENGTH_SHORT).show();
                    Gps_location_TV.setText("לא ניתן לאתר מיקום, צא לשטח פתוח.");
                    Gps_location_TV.setTextColor(Color.RED);
                }
            }
        }, 3000);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this, "map is ready to use", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        LatLng holon = new LatLng(32.0112, 34.7748);
        //mMap.addMarker(new MarkerOptions().position(holon).title("Marker in Holon"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(holon));

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(30.0f);


    }


    //-----Functions-----//

    private void startService() {
        //start GPS service
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        startService(i);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //check if we neet to ask for permisions
    //todo- ask for storage permision
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request permisions
            // if we ask for permisions we return true
            // go to onRequestPermissionsResult
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return true;
        } else {
            //if we dont need permisions we return false
            return false;

        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //handle user permisions results
    //we asking GPS and Camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                //we get the permisions we need
                startService();
            } else {
                runtime_permissions();
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void OpenCloseActionMenu() {
        if (isActionsMenuOpen == false) {
            menu_Actions_layout.setVisibility(View.VISIBLE);
            isActionsMenuOpen = true;
        } else {
            menu_Actions_layout.setVisibility(View.GONE);
            isActionsMenuOpen = false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void cameraMoveCurrectLocation(double latitude, double longitude) {

        /*Defaults Values
        double longitude = 34.7748;
        double latitude = 32.0112;*/
        if (latitude != 32.0112 && longitude != 34.7748) {

            mMap.clear();

            LatLng currentLocation = new LatLng(latitude,longitude);
            LatLng currentLocationForCircle = new LatLng(latitude,longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,17f));

            mMap.addCircle(new CircleOptions()
                    .center(currentLocationForCircle)
                    .radius(100.0)
                    .strokeWidth(3f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(70,50,50,150))
            );

            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).
                    icon(PersonIcon).title("אתה נמצא כאן"));
            Log.i("longtitude & Latitude", " " + longitude + "  " + latitude);
            //refresh counter is integer that increased each time we enter the function
            //after he gets to 7 download databases agine and update the maps

                if (isNoteDatabaseObtained == true) {
                    getNoteDatabase();
                }
                if (isInspectorDatabaseObtained == true) {
                    getInspectorDatabase();
                }


        } else {
            Toast.makeText(this, "לא ניתן לאתר מיקום", Toast.LENGTH_SHORT).show();
        }




    }

    ////////////////////////////////////////////////////////////////////////////////

    public void MapUpdate(double latitude, double longitude) {

        /*Defaults Values
        double longitude = 34.7748;
        double latitude = 32.0112;*/
        if (latitude != 32.0112 && longitude != 34.7748) {

            //todo- if he open note
            //moveCamerafirstTime = false;
            //to show all new notes;

            mMap.clear();




            LatLng currentLocation = new LatLng(latitude,longitude);
            LatLng currentLocationForCircle = new LatLng(latitude,longitude);
/*
            //add marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            selfMarker = mMap.addMarker(markerOptions);
            selfMarker.setIcon(PersonIcon);
*/
/*
            //add circle
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(currentLocationForCircle);
            selfCircle = mMap.addCircle(circleOptions);
            selfCircle.setRadius(50.0);
            selfCircle.setFillColor(Color.argb(70,50,50,150));
            selfCircle.setStrokeColor(Color.argb(20,50,50,200));
            selfCircle.setStrokeWidth(3f);
*/

            mMap.addCircle(new CircleOptions()
                    .center(currentLocationForCircle)
                    .radius(100.0)
                    .strokeWidth(3f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(70,50,50,150))
            );


            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).
                    icon(PersonIcon).title("אתה נמצא כאן"));
            Log.i("longtitude & Latitude", " " + longitude + "  " + latitude);
            //refresh counter is integer that increased each time we enter the function
            //after he gets to 7 download databases agine and update the maps


            if (isNoteDatabaseObtained == true) {
                getNoteDatabase();
            }
            if (isInspectorDatabaseObtained == true) {
                getInspectorDatabase();
            }


        } else {
            Toast.makeText(this, "לא ניתן לאתר מיקום", Toast.LENGTH_SHORT).show();
        }




    }






    ////////////////////////////////////////////////////////////////////////////////


    public void addMarkerToNewLocation(double latitude, double longitude) {

        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));

    }
    ////////////////////////////////////////////////////////////////////////////////

    private void UpdateAddressUI(double latitude, double longitude) {
        String fullAddress = getAddressLine(latitude, longitude);
        Gps_location_TV.setText(fullAddress);
        Gps_location_TV.setTextColor(Color.WHITE);
    }
    ////////////////////////////////////////////////////////////////////////////////

    private String getAddressLine(double latitude, double longitude) {
        try {
            Log.i(TAG, "Its WORKING! we inside the try");
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String cityName = addresses.get(0).getAddressLine(0); // must check the recived address to avoid crashes!
            Log.i(TAG, "Its WORKING!");
            Log.i("geocoder", "city name:" + cityName);
            return cityName;

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Exception", "Geocoder Exception" + e.getMessage() + e.getCause());
        }
        //if there is problem its returning null
        return "NULL";
    }

    //////////////////////////////////////////////////////////////////////////////
    private void startSignIn() {
        final ProgressDialog progressDialogConnection;
        progressDialogConnection = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
        progressDialogConnection.setMessage("אנא המתן...");
        progressDialogConnection.show();
        progressDialogConnection.setCancelable(false);

        email = Et_email_login.getText().toString();
        password = Et_password_login.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "מלא את כל השדות", Toast.LENGTH_SHORT).show();
            progressDialogConnection.dismiss();
            //dialog user not registerd
            LoginErrorDialog();
        } else {
            //start signin
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginScreen.this, "שם משתמש או סיסמא אינם נכונים", Toast.LENGTH_SHORT).show();
                        progressDialogConnection.dismiss();
                        //dialog user not registerd
                        LoginErrorDialog();


                    } else {
                        //user have permisions to use the app
                        //move to the main intent and send informations.
                        //save the strings email and password to file for the next time the user open the app he will login automaticly

                        Toast.makeText(LoginScreen.this, "אתה מחובר", Toast.LENGTH_SHORT).show();
                        if (isLoginScreenOpen == true) {
                            LoginLayout.setVisibility(View.GONE);
                        }
                        isLoginScreenOpen = false;
                        //change login button to connected button
                        connected_btn.setVisibility(View.VISIBLE);
                        loginSystem_btn.setVisibility(View.GONE);
                        progressDialogConnection.dismiss();
                        isUserConnected = true;
                    }


                }
            });
        }


    }

    ////////////////////////////////////////////////////////////////////////////////
    private void ActionBarRegisterDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("אם אתה מעוניין להשתמש באפשרויות אלה, עליך להרשם למערכת" +
                " או אם כבר יש לך משתמש התחבר אליו.")
                .setTitle("אפשרויות למשתמשים רשומים בלבד")
                .setPositiveButton("סגור", dialogClickListener)
                .show();


    }

    ////////////////////////////////////////////////////////////////////////////////
    private void ActionBarLocationDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("אם אתה מעוניין להשתמש באפשרות זו, עליך לאפשר להאפליקציה " +
                " לזהות את מיקומך, אנחנו צריכים את מיקומך כדי לאפשר לגופים לדעת היכן ה-תקלה/ליקוי נמצא וודא שאתה נמצא באזור הליקוי")
                .setTitle("אפשרות זו ניתנת למימוש בתנאי זיהוי מיקומך")
                .setPositiveButton("סגור", dialogClickListener)
                .show();


    }

    ////////////////////////////////////////////////////////////////////////////////

    private void LoginErrorDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("וודא שאתה מקיש נכון את שם המשתמש והסיסמא")
                .setTitle("שם משתמש או סיסמא שגוים")
                .setPositiveButton("בסדר", dialogClickListener)
                .show();


    }




    private void NoteErrorDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("בכדי לספק לרשויות את מיטב המידע הדרוש בכדי לטפל בתקלה \n" +
                "אנא ספק את הפרטים הבאים: \n" +
                "1. תמונה של התקלה/ליקוי העירוני שמצאת \n" +
                "2. נושא מדוייק עבור התקלה. \n" +
                "3. הסבר מפורט ומנומק היטב, עבור התקלה שנתקלת בה. \n" +
                "כל אלה יעזרו לקדם את הטיפול עבורך \n" +
                "תודה!")
                .setTitle("טופס לא מולא כראוי שים לב")
                .setPositiveButton("אוקי הבנתי", dialogClickListener)
                .show();

    }

    ////////////////////////////////////////////////////////////////////////////////

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void setupFirebaseStorage(){
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    ////////////////////////////////////////////////////////////////////////////////
    private void getInspectorDatabase() {
        if(inspectorsFirstTimeRun == true) {
            progressDialogInspector = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
            progressDialogInspector.setTitle("בודק מיקום פקחים אנא המתן...");
            progressDialogInspector.setMessage("הסבלנות משתלמת...");
            progressDialogInspector.setCancelable(false);
            progressDialogInspector.show();
        }
        DatabaseReference inspectorRef = FirebaseDatabase.getInstance().getReference().child("CityPopSERVER").child("inspector");
        inspectorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> Inspector = (Map<String, Object>) dataSnapshot.getValue();
                //vars = null
                String Inspector_Name = null, Date = null, Time = null;
                double latitude, longtitude;
                int i = 0;
                if (Inspector != null) {
                    for (Map.Entry<String, Object> entry : Inspector.entrySet()) {
                        //get user map
                        Map singleNote = (Map) entry.getValue();
                        //get all fields of the Object to strings
                        Inspector_Name = singleNote.get("Inspector_Name").toString();
                        Date = singleNote.get("Date").toString();
                        Time = singleNote.get("Time").toString();
                        latitude = Double.parseDouble(singleNote.get("latitude").toString());
                        longtitude = Double.parseDouble(singleNote.get("longtitude").toString());

                        LatLng currentInspectorLocation = new LatLng(latitude,longtitude);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longtitude)).
                                icon(InspectorIcon).title(Inspector_Name)).setSnippet(Date + "    " + Time + " נראה לאחרונה ");


                        i++;
                    }
                    isInspectorDatabaseObtained = true;
                    setAllNotesMarkersOnMap(); //after data obtained we can use this function
                    Log.i("myInfo","InspectorDatabase has obtained");
                    if(inspectorsFirstTimeRun == true) {
                        progressDialogInspector.dismiss();
                        inspectorsFirstTimeRun = false;
                    }
                }else{
                    Toast.makeText(LoginScreen.this, "לא נמצאו פקחים", Toast.LENGTH_SHORT).show();
                    if(inspectorsFirstTimeRun == true) {
                        progressDialogInspector.dismiss();
                        inspectorsFirstTimeRun = false;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginScreen.this, "אירעה שגיאה בעת העברת רשימת הפקחים משרתינו", Toast.LENGTH_SHORT).show();
                Log.e("myerror",databaseError.toString());
                progressDialogInspector.dismiss();
            }
        });

    }



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void getNoteDatabase(){
        DatabaseReference NoteRef = FirebaseDatabase.getInstance().getReference().child("CityPopSERVER").child("Notes");
        NoteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> notes = (Map<String, Object>) dataSnapshot.getValue();
                String noteUid = null, userUid = null, //vars = null
                        city = null, fullAddress = null,
                        imageUrl = null, noteStatus = null,
                        openingDate = null, openingTime = null,
                        information = null, subject = null;
                double latitude, longtitude;
                int i = 0;
                if (notes != null) {
                    for (Map.Entry<String, Object> entry : notes.entrySet()) {
                        //get user map
                        Map singleNote = (Map) entry.getValue();
                        //get all fields of the Object to strings
                        noteUid = singleNote.get("noteUid").toString();
                        userUid = singleNote.get("userUid").toString();
                        city = singleNote.get("city").toString();
                        information = singleNote.get("information").toString();
                        subject = singleNote.get("subject").toString();
                        fullAddress = singleNote.get("fullAddress").toString();
                        imageUrl = singleNote.get("imageUrl").toString();
                        latitude = Double.parseDouble(singleNote.get("latitude").toString());
                        longtitude = Double.parseDouble(singleNote.get("longtitude").toString());
                        noteStatus = singleNote.get("noteStatus").toString();
                        openingDate = singleNote.get("openingDate").toString();
                        openingTime = singleNote.get("openingTime").toString();
                        //adding the note object to our list
                        AllNotesList.add(new Note(userUid, subject, information, fullAddress, city
                                , openingTime, openingDate, imageUrl, longtitude, latitude));
                        AllNotesList.get(i).setNoteUid(noteUid);
                        AllNotesList.get(i).setNoteStatus(noteStatus);
                        i++;
                    }
                    isNoteDatabaseObtained = true;
                    setAllNotesMarkersOnMap(); //after data obtained we can use this function
                    Log.i("myInfo","NotesDatabase has obtained");
                }else{
                    Toast.makeText(LoginScreen.this, "אין אף תקלה ברשימת התקלות", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginScreen.this, "אירעה שגיאה בעת העברת רשימת התקלות", Toast.LENGTH_SHORT).show();
                Log.e("myerror",databaseError.toString());
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////
    private void setAllNotesMarkersOnMap(){



        for(int i=0;i<AllNotesList.size();i++){
            //new LatLng(latitude, longitude)
            mMap.addMarker(new MarkerOptions().position(new LatLng(AllNotesList.get(i).getLatitude(),
                    AllNotesList.get(i).getLongtitude()))
                    .title(AllNotesList.get(i).getSubject()).
                            snippet(AllNotesList.get(i).getInformation()).icon(warning_sign));
        }


    }
    ////////////////////////////////////////////////////////////////////////////////
    //this function get the result from the intent Camera or any other intent
    //and handle it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            //do someting after we have the result
            Picasso.get().load(mCurrentPhotoPath).into(NotePicture);
            //the full uri of the picture in the storage is locate in mCurrentPhotoPath
            Toast.makeText(this, "תודה על הוספת התמונה", Toast.LENGTH_SHORT).show();
            HavePictureTV.setVisibility(View.VISIBLE);
            isImageCaptured = true;
        }
        if(requestCode == REGISTER_INTENT_CODE && resultCode == RESULT_OK){
            Log.i("myinfo","we get OK from the register intent");
            String emailFromRegister = data.getStringExtra("EmailToConnect");
            String passwordFromRegister = data.getStringExtra("PasswordToConnect");
            Log.i("myinfo",emailFromRegister + " " + passwordFromRegister);





                        //start signin
            auth.signInWithEmailAndPassword(emailFromRegister,passwordFromRegister).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Log.i("myinfo","Problem with automatic login after register");
                        //dialog user not registerd
                        LoginErrorDialog();


                    } else {
                        //user have permisions to use the app
                        //move to the main intent and send informations.


                        Toast.makeText(LoginScreen.this, "אתה מחובר למערכת", Toast.LENGTH_SHORT).show();
                        if (isLoginScreenOpen == true) {
                            LoginLayout.setVisibility(View.GONE);
                        }
                        isLoginScreenOpen = false;
                        //change login button to connected button
                        connected_btn.setVisibility(View.VISIBLE);
                        loginSystem_btn.setVisibility(View.GONE);
                        isUserConnected = true;
                    }


                }
            });





        }


        if(requestCode == REGISTER_INTENT_CODE && resultCode == RESULT_CANCELED){
            Log.i("myinfo","user canceled the register");
        }

    }
    ////////////////////////////////////////////////////////////////////////////////
    /*
    public String compressImage(String ImageUri){
        Bitmap bitmapImage = BitmapFactory.decodeFile(ImageUri);
        int nh = (int)(bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage,512,nh,true);


        return scaled.toString();
    }
    */


    ////////////////////////////////////////////////////////////////////////////////





    //-----END --  Functions-----//


}
