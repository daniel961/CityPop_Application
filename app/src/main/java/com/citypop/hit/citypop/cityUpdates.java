package com.citypop.hit.citypop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class cityUpdates extends AppCompatActivity {

    Button back_btn;
    Button search_btn;
    EditText Et_city_search;
    private List<cityUpdateObject> CityUpdatesList = new ArrayList<cityUpdateObject>();
    ProgressDialog progressDialog;

    private static final String TAG = "cityUpdatesClass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_updates);

        //refs
        back_btn =(Button) findViewById(R.id.back_btn);
        Et_city_search = (EditText) findViewById(R.id.Et_city_search);
        search_btn = (Button) findViewById(R.id.search_btn);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchCity = Et_city_search.getText().toString();
                if(searchCity.isEmpty() || searchCity == null){
                    Toast.makeText(cityUpdates.this, "הזן עיר לחיפוש", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog = new ProgressDialog(cityUpdates.this, AlertDialog.THEME_HOLO_DARK);
                    progressDialog.setTitle("בודק עדכונים זמינים");
                    progressDialog.setMessage("אנא המתן...");
                    progressDialog.setCancelable(false);

                    getCityUpdates(searchCity);
                }
            }
        });



    }

    //Functions
    ////////////////////////////////////////////////////////////////////////////
    private void getCityUpdates(String searchCity) {
        DatabaseReference UpdateRef = FirebaseDatabase.getInstance().getReference().child("CityPopSERVER").child("cityUpdates").child(searchCity);
        if (UpdateRef == null){
            Toast.makeText(this, "לא נמצאו עדכונים עבור העיר "+ searchCity, Toast.LENGTH_SHORT).show();
        }else{
            UpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> updates = (Map<String, Object>) dataSnapshot.getValue();
                    String author = null, city = null, //vars = null
                            date = null, imageUrl = null,
                            subject = null, text = null,
                            time = null;
                            int i = 0;

                            if(CityUpdatesList.size() > 0){
                                CityUpdatesList.clear();
                                Log.i(TAG, "onDataChange: Clear updates List");
                            }

                    if (updates != null) {
                        for (Map.Entry<String, Object> entry : updates.entrySet()) {
                            //get user map
                            Map singleCity_Update = (Map) entry.getValue();
                            //get all fields of the Object to strings
                            author = singleCity_Update.get("author").toString();
                            date = singleCity_Update.get("date").toString();
                            city = singleCity_Update.get("city").toString();
                            imageUrl = singleCity_Update.get("imageUrl").toString();
                            subject = singleCity_Update.get("subject").toString();
                            text = singleCity_Update.get("text").toString();
                            time = singleCity_Update.get("time").toString();

                            //adding the note object to our list
                            CityUpdatesList.add(new cityUpdateObject(author, city, date,imageUrl, text, subject, time));

                            i++;
                        }
                        progressDialog.dismiss();
                        Log.i("myInfo","City_updates has obtained");
                        //todo-install the recycler view adapter.!
                        initRecyclerView();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(cityUpdates.this, "לא נרשמו עדכונים עבור עיר זו", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(cityUpdates.this, "אירעה שגיאה בעת העברת העדכונים מהשרתינו", Toast.LENGTH_SHORT).show();
                    Log.i("myerror","  data base error updates  " + databaseError.toString());
                    progressDialog.dismiss();
                }
            });





        }

    }
    ////////////////////////////////////////////////////////////////////////////

    private void initRecyclerView() {
        Log.i(TAG, "initRecyclerView: starting Init Recycler View");
        RecyclerView recyclerView = findViewById(R.id.city_updates_recyclerView_layout);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, (ArrayList<cityUpdateObject>) CityUpdatesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    ////////////////////////////////////////////////////////////////////////////
}
