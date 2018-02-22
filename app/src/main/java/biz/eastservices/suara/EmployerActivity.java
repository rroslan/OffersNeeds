package biz.eastservices.suara;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import biz.eastservices.suara.Common.Common;
import biz.eastservices.suara.Fragments.HelpFragments;
import biz.eastservices.suara.Fragments.JobsFragments;
import biz.eastservices.suara.Fragments.ServicesFragments;
import biz.eastservices.suara.Fragments.TransportsFragments;
import biz.eastservices.suara.Model.Employer;

public class EmployerActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 7172;


    FirebaseDatabase database;
    DatabaseReference user_tbl, candidates;
    BottomNavigationView bottomNavigationView;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private Location mLastLocation;

    int match_people = 0;

    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.jobs_string));
        setSupportActionBar(toolbar);


        //Request Runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Run-time request permission
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_PERMISSION_REQUEST_CODE);
            } else {
                buildLocationRequest();
                buildLocationCallBack();

                //Create FusedProviderClient
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            }
        }

        //Init View
        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_nav_employer);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return false;
                }
                LocationServices.getFusedLocationProviderClient(getBaseContext()).getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                mLastLocation = location;
                            }
                        });
                switch (item.getItemId()) {
                    case R.id.action_jobs:
                        selectedFragment = JobsFragments.getInstance(mLastLocation);
                        toolbar.setTitle(getResources().getString(R.string.jobs_string));
                        break;
                    case R.id.action_helps:
                        selectedFragment = HelpFragments.getInstance(mLastLocation);
                        toolbar.setTitle(getResources().getString(R.string.helps_string));
                        break;
                    case R.id.action_services:
                        selectedFragment = ServicesFragments.getInstance(mLastLocation);
                        toolbar.setTitle(getResources().getString(R.string.services_string));
                        break;
                    case R.id.action_transports:
                        selectedFragment = TransportsFragments.getInstance(mLastLocation);
                        toolbar.setTitle(getResources().getString(R.string.transports_string));
                        break;
                }
                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_layout, selectedFragment);
                    transaction.commit();
                }
                return true;
            }
        });


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        user_tbl = database.getReference(Common.USER_TABLE_EMPLOYER);


        user_tbl.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {

                            //Need update Setting
                            startActivity(new Intent(EmployerActivity.this, EmployerSettings.class));

                        } else {
                            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            LocationServices.getFusedLocationProviderClient(getBaseContext())
                                    .getLastLocation()
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            mLastLocation = location;

                                            Map<String, Object> update_location = new HashMap<>();
                                            update_location.put("lat", mLastLocation.getLatitude());
                                            update_location.put("lng", mLastLocation.getLongitude());
                                            user_tbl.child(FirebaseAuth.getInstance().getUid())
                                                    .updateChildren(update_location)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(EmployerActivity.this, "Error update location", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }


                                    });

                        }
                    }
                            @Override
                            public void onCancelled (DatabaseError databaseError){

                            }
                        });
                    }


                    private void buildLocationCallBack() {
                        locationCallback = new LocationCallback() {

                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations())
                                    mLastLocation = location;
                                if (mLastLocation != null) {
                                    Map<String, Object> update_location = new HashMap<>();
                                    update_location.put("lat", mLastLocation.getLatitude());
                                    update_location.put("lng", mLastLocation.getLongitude());
                                    user_tbl.child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(update_location)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(EmployerActivity.this, "Error update location", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        };
                    }

                    private void buildLocationRequest() {
                        locationRequest = new LocationRequest();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(5000);
                        locationRequest.setFastestInterval(3000);
                        locationRequest.setSmallestDisplacement(10);
                    }

                    @Override
                    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                        switch (requestCode) {
                            case MY_PERMISSION_REQUEST_CODE:
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                                }
                                break;
                        }
                    }

                    @Override
                    public boolean onCreateOptionsMenu(Menu menu) {
                        getMenuInflater().inflate(R.menu.menu_candidate, menu);
                        return super.onCreateOptionsMenu(menu);
                    }

                    @Override
                    public boolean onOptionsItemSelected(MenuItem item) {
                        if (item.getItemId() == R.id.action_setting)
                            startActivity(new Intent(EmployerActivity.this, EmployerSettings.class));
                        return super.onOptionsItemSelected(item);
                    }


                }
