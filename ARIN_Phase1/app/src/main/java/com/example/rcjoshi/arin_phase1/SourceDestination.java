package com.example.rcjoshi.arin_phase1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class SourceDestination extends AppCompatActivity {

    Uri uri;
    String picpath="";
    Button mCapture,mDetect,mGallery;
    ImageView mImageView;
    TextView mSourceText;
    Bitmap mBitmap;
    int mCapturedFlag = 0, mGallerySelectFlag = 0, mSourceDetectedFlag = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 7;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.previousbtnid:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.userguidebtnid:
                    Intent mGuideIntent = new Intent(SourceDestination.this,MainActivity.class);
                    startActivity(mGuideIntent);
                    finish();
                    return true;
                case R.id.nextbtnid:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent mNextIntent = new Intent(SourceDestination.this,Navigate.class);
                    startActivity(mNextIntent);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_destination);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Camera Permissions
        if (ContextCompat.checkSelfPermission(SourceDestination.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SourceDestination.this,
                        new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE_CAPTURE);
        }

        mImageView = (ImageView) findViewById(R.id.imageViewid);
        mCapture = (Button) findViewById(R.id.capturebtnid);
        mDetect = (Button) findViewById(R.id.detectbtnid);
        mGallery = (Button) findViewById(R.id.selectbtnid);
        mSourceText = (TextView) findViewById(R.id.textView2);

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionREAD_EXTERNAL_STORAGE(SourceDestination.this)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCapturedFlag == 1 || mGallerySelectFlag == 1)
                    detectText();
                else
                    Toast.makeText(getApplicationContext(),"No Image Captured",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mBitmap);
            mCapturedFlag = 1;
        }
        /*
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picpath = cursor.getString(columnIndex);
                cursor.close();
                // Set the Image in ImageView after decoding the String
                mImageView.setImageBitmap(BitmapFactory
                        .decodeFile(picpath));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        */

        if (resultCode==RESULT_OK && requestCode==7)
        {
            try {
                Context applicationContext = getApplicationContext();
                uri = data.getData();
                mBitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(),uri);
                mImageView.setImageBitmap(mBitmap);
                picpath = uri.toString();
                mGallerySelectFlag = 1;

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private void detectText() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {
                        processText(result);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
    }

    private void processText(FirebaseVisionText mVisionText){
        List<FirebaseVisionText.TextBlock> mBlocks = mVisionText.getTextBlocks();
        if (mBlocks.size()==0){
            Toast.makeText(SourceDestination.this,"No Text Found",Toast.LENGTH_SHORT).show();
            return;
        }
        //String mText = "";
        for (FirebaseVisionText.TextBlock mBlock_i: mVisionText.getTextBlocks()) {
            String mText = mBlock_i.getText();
            Toast.makeText(SourceDestination.this,mText,Toast.LENGTH_SHORT).show();
            //mSourceText.setTextSize(20);
            //mSourceText.setText(mText);
            mText = mText.replace("\n", " ");
            mText=detectSource(mText);
            if (mSourceDetectedFlag == 1){
                mSourceText.setTextSize(20);
                mSourceText.setText(mText);
            }
            else
                Toast.makeText(SourceDestination.this,"Please try again",Toast.LENGTH_SHORT).show();
        }
        //mText = detectSource(mText);
        if (mSourceDetectedFlag == 1){

            }
         //   else
          //      Toast.makeText(SourceDestination.this,"Please try again",Toast.LENGTH_SHORT).show();
       // mSourceText.setTextSize(20);
        //mSourceText.setText(mText);
    }

    private String detectSource(String mText){
        String mSource = "";
        int mCnt = 0;
        String[] tags = getResources().getStringArray(R.array.boards);
        for(String tag : tags) {
            String[] pair = tag.split(":");

            String key = pair[0];
            String value = pair[1];
            if(value.toLowerCase().contains(mText.toLowerCase()) || mText.toLowerCase().contains(value.toLowerCase())) {
                mSource = key;
                mCnt++;
            }
        }
        if (mCnt == 1) {
            mSourceDetectedFlag = 1;
        }
        return mSource;
        /*
        int ipos=0;
        //ipos=getResources().getStringArray(R.array.boards).length;
        //Toast.makeText(SourceDestination.this,ipos,Toast.LENGTH_SHORT).show();
        for (ipos=0;ipos<getResources().getStringArray(R.array.boards).length;ipos++)
        {
            String temp = getResources().getStringArray(R.array.boards)[ipos];
            Toast.makeText(SourceDestination.this,temp,Toast.LENGTH_SHORT).show();
        }
        */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(SourceDestination.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

}
