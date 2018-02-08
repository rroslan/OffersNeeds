package biz.eastservices.suara;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import biz.eastservices.suara.Common.Common;

public class MainActivity extends AppCompatActivity {

    RelativeLayout rootLayout;

    Button btnCandidate, btnEmployer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        btnCandidate = (Button) findViewById(R.id.btn_candidate);
        btnEmployer = (Button) findViewById(R.id.btn_employer);


        btnEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EmployerActivity.class));
            }
        });

        btnCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CandidateActivity.class));
            }
        });

        //Check if not sign-in then navigate Signin page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), Common.SIGN_IN_REQUEST_CODE);
        } else {
            Snackbar.make(rootLayout, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

        }
    }
}
