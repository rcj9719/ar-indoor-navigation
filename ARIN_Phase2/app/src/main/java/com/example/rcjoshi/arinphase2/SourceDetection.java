package com.example.rcjoshi.arinphase2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SourceDetection extends AppCompatActivity{

    Button mCapture,mDetect,mGallery;

    ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;

    private int mSourceDetectedFlag = 0, mCapturedFlag = 0, mGallerySelectFlag = 0;
    private Bitmap mBitmap;
    Uri uri;
    String picpath = "",mSrc,mDest;
    List<String> mNavInstructions;
    static int mProceedFlag=0;
    private SensorManager sensorManager;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 7;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    //--------------------------Activity Layout-----------------------------------------------------

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_navigation_prev:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mPrevIntent = new Intent(SourceDetection.this, Destination.class);
                    startActivity(mPrevIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_steps:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent mGuideIntent = new Intent(SourceDetection.this, MainActivity.class);
                    startActivity(mGuideIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_next:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent mNextIntent = new Intent(SourceDetection.this, ARNavigation.class);
                    startActivity(mNextIntent);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_detection);

        //Camera Permissions
        if (ContextCompat.checkSelfPermission(SourceDetection.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SourceDetection.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        mCapture = (Button) findViewById(R.id.capturebtnid);
        mDetect = (Button) findViewById(R.id.detectbtnid);
        mGallery = (Button) findViewById(R.id.selectbtnid);

        mCapture.setOnClickListener(view -> takePhoto());

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCapturedFlag == 1 || mGallerySelectFlag == 1) {
                    detectText();
                    mCapturedFlag=0;
                    mGallerySelectFlag=0;
                }
                else
                    Toast.makeText(getApplicationContext(), "No Image Captured", Toast.LENGTH_SHORT).show();
            }
        });
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionREAD_EXTERNAL_STORAGE(SourceDetection.this)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });
        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.cam_fragment);

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //-----------------------Image Capture and gallery select methods-------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            try {
                Context applicationContext = getApplicationContext();
                uri = data.getData();
                picpath = uri.toString();
                mBitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), uri);
                mGallerySelectFlag = 1;
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved from Gallery", Snackbar.LENGTH_LONG);
                snackbar.show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = fragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        mBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        // Make the request to copy.
        PixelCopy.request(view, mBitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(mBitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(SourceDetection.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved", Snackbar.LENGTH_LONG);
                mCapturedFlag=1;
                snackbar.setAction("Open in Photos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(SourceDetection.this,
                            SourceDetection.this.getPackageName() + ".ar.codelab.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            }
            else {
                Toast toast = Toast.makeText(SourceDetection.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
                        .format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "ARINCaptures/" + date + "_source.jpg";
    }


    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    //------------------------Source Detection methods----------------------------------------------

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

    private void processText(FirebaseVisionText mVisionText) {
        SharedPreferences sd=getSharedPreferences("data", Context.MODE_PRIVATE);
        List<FirebaseVisionText.TextBlock> mBlocks = mVisionText.getTextBlocks();
        SharedPreferences.Editor ed=sd.edit();

        String mText;
        if (mBlocks.size() == 0) {
            Toast.makeText(SourceDetection.this, "No Text Found", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        for (FirebaseVisionText.TextBlock mBlock_i : mVisionText.getTextBlocks()) {
            mText = mBlock_i.getText();
            Toast.makeText(SourceDetection.this, mText, Toast.LENGTH_SHORT).show();
            mText = mText.replace("\n", " ");
            mText = detectSource(mText);
            if (mSourceDetectedFlag == 1) {
                Snackbar mSnackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Source detected is "+mText, Snackbar.LENGTH_SHORT);
                mSnackbar.show();
                ed.putString("sdSrc",mText);
                ed.commit();
                mSrc = sd.getString("sdSrc","");
                mDest = sd.getString("sdDest","");
            }
            else {
                Snackbar mSnackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Source detection failed.\nPlease capture landmark again.",
                        Snackbar.LENGTH_SHORT);
                mSnackbar.show();
            }
        }
        if (mSourceDetectedFlag==1){
            DialogFragment mAlertObject = new UserGuideAlert();
            mAlertObject.show(getSupportFragmentManager(),"nav");
        }
    }

    private String detectSource(String mText) {
        String mSource = "";
        int mCnt = 0;
        String[] tags = getResources().getStringArray(R.array.boards);
        for (String tag : tags) {
            String[] pair = tag.split(":");

            String key = pair[0];
            String value = pair[1];
            if (value.toLowerCase().contains(mText.toLowerCase()) ||
                    mText.toLowerCase().contains(value.toLowerCase())) {
                mSource = key;
                mCnt=1;
            }
        }
        if (mCnt == 1) {
            mSourceDetectedFlag = 1;
        }
        return mSource;
    }

    //----------------------Permission checks-------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(SourceDetection.this, "GET_ACCOUNTS Denied",
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
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}