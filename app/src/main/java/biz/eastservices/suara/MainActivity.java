package biz.eastservices.suara;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import biz.eastservices.suara.Common.Common;

public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener  {




    RelativeLayout rootLayout;

    Button btnCandidate, btnEmployer,btnSendEmail,btnRefresh;

    TextView txtStatus;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 7172;
    private static int UPDATE_INTERVAL = 5000; // SEC
    private static int FATEST_INTERVAL = 3000; // SEC
    private static int DISPLACEMENT = 10; // METERS

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Common.SIGN_IN_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {


                txtStatus.setText("Email Verification : "+FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());

                if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                {
                    btnSendEmail.setEnabled(true);
                    btnCandidate.setEnabled(false);
                    btnEmployer.setEnabled(false);
                }

                Snackbar.make(rootLayout, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                    //Request Runtime permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //Run-time request permission
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                            }, MY_PERMISSION_REQUEST_CODE);
                        } else {
                            if (checkPlayServices()) {
                                buildGoogleApiClient();
                                createLocationRequest();
                            } else {
                                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else {
                    Snackbar.make(rootLayout,"Please verify your email",Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
            }
            else{
                Snackbar.make(rootLayout,"We couldn't sign you in.Please try again later", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        btnCandidate = (Button) findViewById(R.id.btn_candidate);
        btnEmployer = (Button) findViewById(R.id.btn_employer);
        btnSendEmail = (Button)findViewById(R.id.btnSendEmail);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);

        txtStatus = (TextView)findViewById(R.id.txt_status);



        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser()
                        .sendEmailVerification()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout, "Vertification email send to "+FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
                                btnRefresh.setEnabled(true);

                            }
                        });
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().reload()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                txtStatus.setText("Email Verification : "+FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());

                                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                                {
                                    btnCandidate.setEnabled(true);
                                    btnEmployer.setEnabled(true);
                                }
                            }
                        });
            }
        });


        btnEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                                    startActivity(new Intent(MainActivity.this, EmployerActivity.class));
                                else
                                {
                                    Toast.makeText(MainActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });



        btnCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                                    startActivity(new Intent(MainActivity.this, CandidateActivity.class));
                                else
                                {
                                    Toast.makeText(MainActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });




        //Check if not sign-in then navigate Signin page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), Common.SIGN_IN_REQUEST_CODE);
        } else {

            txtStatus.setText("Email Verification : "+FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());

            if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
            {
                btnSendEmail.setEnabled(true);

                btnCandidate.setEnabled(false);
                btnEmployer.setEnabled(false);

            }

            Snackbar.make(rootLayout, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

                            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                //Request Runtime permission
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        //Run-time request permission
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        }, MY_PERMISSION_REQUEST_CODE);
                                    } else {
                                        if (checkPlayServices()) {
                                            buildGoogleApiClient();
                                            createLocationRequest();
                                        } else {
                                            Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                            else {
                               Snackbar.make(rootLayout,"Please verify your email",Snackbar.LENGTH_SHORT)
                                       .show();
                            }
                        }

        }









    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Common.currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Common.currentLocation = location;
        Log.d("Location",location.getLatitude()+"/"+location.getLongitude());
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        super.onStop();
    }
}
