


/**
 *  completion Date 9 March 2019,
 *    Author@_Alok_Kumar
 *    Indian Institute of Technology(IIT) Delhi
 *   Under improvement
 *   I have tried my best to maximise the comments for the code for better understanding
 */



package com.example.alokj.uploading_files;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class uploading_files extends AppCompatActivity {
   Button selectfile,upload;
   TextView notification;
   DatabaseReference databaseReference; // for storing URLs(as data) of uploaded files
   StorageReference storageReference; // for uploading files
    Uri pdfUri; // Uri are URLs meant for local storage(path of file to local storage)
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploading_files);
        selectfile=findViewById(R.id.selectFile);
        upload=findViewById(R.id.upload);
        notification=findViewById(R.id.notification);
        databaseReference=FirebaseDatabase.getInstance().getReference();
        storageReference=FirebaseStorage.getInstance().getReference();

        /**
         * Now we need to do- what should happen when we click "SelectFile" Button.
         * we should check permission to read the SDCard/Internal Storage
         */
       
        selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * To check permission to read the SDCard/Internal Storage we put
                * this condition and see if this true or not and then for both case
                * we take action separately
                * */
                //CASE-1 if permission guaranteed
                if(ContextCompat.checkSelfPermission(uploading_files.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectPdf();
                }
                //CASE 2 asking user to grant the permission if app doesn't
                // get permission
                else
                {
                    ActivityCompat.requestPermissions(uploading_files.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},9);
                    /*
                    * Note if the user successfully grant the permission using this above line
                    * then acknowledgment(proof for true i.e. whether user successfully
                    * guaranteed the permission or not)
                    * will be done in Another Method("onRequestPermissionsResult")
                    * NOTE this method will be invoked(called) due to (by) this above line
                    * */
                }
            }
        });

        
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfUri!=null)
                {
                    uploadfile(pdfUri);
                }
                else
                {
                    Toast.makeText(uploading_files.this,"Please select a file first!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void uploadfile(Uri pdfUri) {
        /*
         * within this we will upload our file
         *
         */
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading File...");
        progressDialog.setProgress(0);
        progressDialog.show();
          final String temp_filename=System.currentTimeMillis()+"";
        // final StorageReference storageReference=storage.getReference();//root path of storage where file will be stored
        storageReference.child("Folder_of_uploadefile").child(temp_filename).putFile(pdfUri)
        /*
         * up to this our file is successfully uploaded But
         * we need acknowledgement that whether the file successfully uploaded or not
         * it might happen that our internet connection was off
         * THIS very 1st below method will be invoked if our file is successfully uploaded
         * THIS very 2nd below method will be invoked if our file is NOT uploaded
         * THIS very 3rd below method will be invoked and help to TRACK the STATUS
         * of our file getting uploaded
         */
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url= storageReference.getDownloadUrl().toString();//return the URL of uploaded file
                //Now we will store the URL in the RealTime DatabaseReference
                //DatabaseReference databaseReference=database.getReference();
                databaseReference.child(databaseReference.push().getKey()).setValue(url)
                        /*
                         * again acknowledgement for complete Successful
                         * This time we will just do "TOAST" in stead of Override....LOL...
                         */
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(uploading_files.this,"Congrats!! your file uploaded",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(uploading_files.this,"your file didn't uploaded :(",Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //this invoked if StorageReference was unable to upload file on FireBaseStorage
                Toast.makeText(uploading_files.this,"your file didn't uploaded :(",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                // we are going to Track the progress of our upload i.e.
                //we are going to show a progressBar/ProgressDialog to user
                int currentProgress=(int) (100*(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount()));
                progressDialog.setProgress(currentProgress);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPdf();
        }
        else
        {
            Toast.makeText(uploading_files.this,"Please Provide access",Toast.LENGTH_LONG).show();
            //ActivityCompat.requestPermissions(uploading_files.this,permissions,requestCode);
        }
    }

    private void selectPdf() {
        // to offer user to select a file from file manager we use "Intent"
        Intent intent=new Intent();
        intent.setType("application/pdf");//intent is targeted for pdf file
        intent.setAction(Intent.ACTION_GET_CONTENT); //intent is made to fetch file
         startActivityForResult(intent,69);//to launch the Intent
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * this method will be automatically invoked by the Android due to the
         * method in selectPdf("startActivityForResult")
         * to check whether our user has successfully selected the file or not
         * (as earlier:-  RequestPermission Method)
         * "RequestCode"- to check whether this is invoked by the particular method
         * we are interested in or not
         * "ResultCode"-to check whether user has successfully completed the operation or not
         *"data"-whether the user has selected the file or not
         */
        if(requestCode==69 &&resultCode==RESULT_OK&& data!=null)
        {
            //we will fetch Uri/Url for the selected file
            pdfUri=data.getData();
            notification.setText("The File selected "+data.getData().getLastPathSegment());
        }
        else
        {
            Toast.makeText(uploading_files.this,"please select a file",Toast.LENGTH_SHORT).show();
        }
    }
}

