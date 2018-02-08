package biz.eastservices.suara;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import biz.eastservices.suara.Common.Common;

public class CandidateActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference user_tbl;

    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate);

        //Init View
        bottomNavigationView=(BottomNavigationView)
                findViewById(R.id.bottom_nav_candidate);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_setting:
                    {
                        startActivity(new Intent(CandidateActivity.this,CandidateSettings.class));
                    }
                    break;


                }
                return true;
            }
        });

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        user_tbl = database.getReference(Common.USER_TABLE_CANDIDATE);


        user_tbl.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {

                            //Need update Setting
                            startActivity(new Intent(CandidateActivity.this, CandidateSettings.class));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
