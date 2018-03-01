package biz.eastservices.suara;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import biz.eastservices.suara.Common.Common;
import biz.eastservices.suara.Model.Employer;
import br.com.sapereaude.maskedEditText.MaskedEditText;
import de.hdodenhof.circleimageview.CircleImageView;

public class EmployerSettings extends AppCompatActivity {

    CircleImageView circleImageView;
    MaterialEditText txtName;
    Button btnSave;//,btnViewList;

    RadioButton rdiJobs, rdiHelp, rdiService, rdiTransport,rdiSell,rdiRent;

    MaskedEditText txtPhone;

    private Uri filePath;

    int selectCategory=-1;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseDatabase database;
    DatabaseReference employers;

    Employer employer = new Employer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_settings);
        //Init FireStorage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
        employers = database.getReference(Common.USER_TABLE_EMPLOYER);

        //Init view
        circleImageView = (CircleImageView)findViewById(R.id.profile_image);

        txtName = (MaterialEditText)findViewById(R.id.edt_name);
        txtPhone = (MaskedEditText) findViewById(R.id.edt_phone);

        rdiJobs = (RadioButton) findViewById(R.id.rdi_job);
        rdiService = (RadioButton) findViewById(R.id.rdi_services);
        rdiTransport = (RadioButton) findViewById(R.id.rdi_transport);
        rdiRent =  (RadioButton) findViewById(R.id.rdi_rent);
        rdiSell =  (RadioButton) findViewById(R.id.rdi_sell);

        rdiJobs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectCategory = 0;
            }
        });

        rdiService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectCategory = 1;
            }
        });
        rdiTransport.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectCategory = 2;
            }
        });
        rdiSell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    selectCategory = 3;
            }
        });

        rdiRent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    selectCategory=4;
            }
        });



        btnSave = (Button)findViewById(R.id.btn_save);
        //btnViewList = (Button)findViewById(R.id.btn_view_list);

        //Event
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set select image when click to avatar
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create new object user information
                employer.setName(txtName.getText().toString());
                employer.setPhone(new StringBuilder("601").append(txtPhone.getRawText()).toString());
                employer.setCategory(Common.convertTypeToCategory(selectCategory));
                //employer.setWhatsapp(txtWhatsApp.getText().toString());
                //employer.setWaze(txtWaze.getText().toString());
                if(Common.currentLocation != null) {
                    employer.setLat(Common.currentLocation.getLatitude());
                    employer.setLng(Common.currentLocation.getLongitude());
                }


                employers.child(FirebaseAuth.getInstance().getUid())
                        .setValue(employer)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EmployerSettings.this, "Information updated !", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EmployerSettings.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ERROR",e.getMessage());
                            }
                        });

            }
        });

        employers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            employer = dataSnapshot.getValue(Employer.class);

                            //Set image
                            Picasso.with(getBaseContext())
                                    .load(employer.getProfileImage())
                                    .error(R.drawable.ic_terrain_black_24dp)
                                    .placeholder(R.drawable.ic_terrain_black_24dp)
                                    .into(circleImageView);

                            txtName.setText(employer.getName());
                            txtPhone.setText(employer.getPhone());

                            if(employer.getCategory() != null) {
                                if (Common.convertCategoryToType(employer.getCategory()) == 0)
                                    rdiJobs.setChecked(true);
                                else if (Common.convertCategoryToType(employer.getCategory()) == 1)
                                    rdiService.setChecked(true);
                                else if (Common.convertCategoryToType(employer.getCategory()) == 2)
                                    rdiTransport.setChecked(true);
                                else if (Common.convertCategoryToType(employer.getCategory()) == 3)
                                    rdiSell.setChecked(true);
                                else if (Common.convertCategoryToType(employer.getCategory()) == 4)
                                    rdiRent.setChecked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void uploadImageAndGetUrl() {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(EmployerSettings.this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child("images/"+ imageName);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(EmployerSettings.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    employer.setProfileImage(uri.toString());


                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EmployerSettings.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            circleImageView.setImageURI(data.getData());

            uploadImageAndGetUrl();
        }
    }
}
