package com.example.rcjoshi.mlkittext;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button mCapture,mDetect;
    ImageView mImageView;
    TextView mTextView;
    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_IMAGE_CAPTURE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        //int rotation = getRotationCompensation("CameraID",)
        //CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        mImageView = (ImageView) findViewById(R.id.imageViewid);
        mCapture = (Button) findViewById(R.id.capturebtnid);
        mDetect = (Button) findViewById(R.id.detectbtnid);
        mTextView = (TextView) findViewById(R.id.textView2);

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

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
            Toast.makeText(MainActivity.this,"No Text Found",Toast.LENGTH_SHORT).show();
            return;
        }
        for (FirebaseVisionText.TextBlock mBlock_i: mVisionText.getTextBlocks()) {
            String mText = mBlock_i.getText();
            mTextView.setTextSize(24);
            mTextView.setText(mText);
        }
    }
}
