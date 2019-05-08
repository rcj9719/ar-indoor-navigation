package com.example.rcjoshi.arinphase2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ARNavigation extends AppCompatActivity implements SensorEventListener, StepListener{

    ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;

    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int numSteps=0;

    Button mGallery;

    //List<String> mAllInstructionList;
    Path[] mAllInstructionList = new Path[10];
    static int mInstructionNum=0;
    private int mInstructionCnt=0;

    private int mListenerRegistered=0;
    int mDestNum=0,mSrcNum=0;
    int mDestGroup=0,mSrcGroup=0;
    int mDir=0;
    int mStepsG1[]={25,15,24,14},mStepsG2[]={7,25,4,24,3,20},mStepsCross=7;
    int mAryPtrSrc,mAryPtrDest;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_navigation_prev:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mPrevIntent = new Intent(ARNavigation.this, SourceDetection.class);
                    startActivity(mPrevIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_steps:
                    //mTextMessage.setText(R.string.title_dashboard);
                    Intent mGuideIntent = new Intent(ARNavigation.this, MainActivity.class);
                    startActivity(mGuideIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_next:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent mNextIntent = new Intent(ARNavigation.this, ARNavigation.class);
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

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.cam_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });

        startNavigation();

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //--------------------------Pedometer Navigation logic------------------------------------------

    public void startNavigation() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        numSteps=0;
        sensorManager.registerListener(ARNavigation.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mListenerRegistered=1;

        //initialise a new string array
        //String[] mEachInstruction = new String[]{};
        // Create a List from String Array elements
        //mAllInstructionList = new ArrayList<String>(Arrays.asList(mEachInstruction));
        //mAllInstructionList = new Path[10];

        String mSavedSrc="Hello 000",mSavedDest="Hello 000";
        SharedPreferences sd=getSharedPreferences("data",Context.MODE_PRIVATE);

        mSavedSrc = sd.getString("sdSrc","");
        mSavedDest = sd.getString("sdDest","");

        Toast.makeText(getApplicationContext(),"Src: "+mSavedSrc+"\nDestination: "+mSavedDest,Toast.LENGTH_SHORT).show();

        if (mSavedDest.equals("Washroom"))
        {   Toast.makeText(getApplicationContext(),"washroom selected",Toast.LENGTH_SHORT).show();
            mDestNum = 108; mDestGroup=2;   }
        else if (mSavedDest.equals("Entrance"))
        {   mDestNum = 111; mDestGroup=2;   }
        else if (mSavedDest.equals("HOD Cabin"))
        {   mDestNum = 109; mDestGroup=2;   }
        else
        {
            //mDestMessage.setText("Destination selected: "+mSavedDest.substring(mSavedDest.length() - 3));
            Toast.makeText(getApplicationContext(),"Dest:" +mSavedDest.substring(mSavedDest.length() - 3),Toast.LENGTH_SHORT).show();
            mDestNum = Integer.parseInt(mSavedDest.substring(mSavedDest.length() - 3));
            if (mDestNum<=105)
                mDestGroup=1;
            else
                mDestGroup=2;
        }

        if (mSavedSrc.equals("Washroom"))
        {   mSrcNum = 108; mSrcGroup=2;   }
        else if (mSavedSrc.equals("Entrance"))
        {   mSrcNum = 111; mSrcGroup=2;   }
        else if (mSavedSrc.equals("HOD Cabin"))
        {   mSrcNum = 109; mSrcGroup=2;   }
        else
        {
            //mSrcMessage.setText("Source selected: "+mSavedSrc.substring(mSavedSrc.length() - 3));
            Toast.makeText(getApplicationContext(),"Src:" +mSavedSrc.substring(mSavedSrc.length() - 3),Toast.LENGTH_SHORT).show();
            mSrcNum = Integer.parseInt(mSavedSrc.substring(mSavedSrc.length() - 3));
            //mSrcNum=0;
            if (mSrcNum<=105)
                mSrcGroup=1;
            else
                mSrcGroup=2;
        }

        mAryPtrSrc = mSrcNum-101;
        mAryPtrDest = mDestNum-101;
        if (mAryPtrSrc>3) mAryPtrSrc-=4;
        if (mAryPtrDest>3) mAryPtrDest-=4;

        if (mSrcGroup!=mDestGroup)
        {
            mSrcNum = correspondLoc(mSrcNum);
            if (mSrcGroup==1) mSrcGroup=2;
            else if (mSrcGroup==2) mSrcGroup=1;
            //mAllInstructionList.add("Cross the passage");
            mAllInstructionList[0] = new Path();
            mAllInstructionList[0].setPath(0,7);
            mInstructionCnt++;
        }
        if (mSrcGroup==1 && mSrcNum>mDestNum) {
            mDir=-1;
            for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i+=mDir)
            {
                //mAllInstructionList.add(""+mDir+mStepsG1[i]);
                mAllInstructionList[mInstructionCnt] = new Path();
                mAllInstructionList[mInstructionCnt].setPath(mDir,mStepsG1[i]);
                mInstructionCnt++;
                Toast.makeText(getApplicationContext(), "Toward Entrance, take steps " +mStepsG1[i], Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==1 && mSrcNum<mDestNum){ //103,104
            mDir=1;
            for (int i=mAryPtrSrc; i<mAryPtrDest; i+=mDir)
            {
                //mAllInstructionList.add(""+mDir+mStepsG1[i]);
                mAllInstructionList[mInstructionCnt] = new Path();
                mAllInstructionList[mInstructionCnt].setPath(mDir,mStepsG1[i]);
                mInstructionCnt++;
                Toast.makeText(getApplicationContext(),"Toward 105, take steps " +mStepsG1[i],Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==2 && mSrcNum<mDestNum){
            mDir=-1;
            for (int i=mAryPtrSrc; i<mAryPtrDest; i-=mDir)
            {
                //mAllInstructionList.add(""+mDir+mStepsG2[i]);
                mAllInstructionList[mInstructionCnt] = new Path();
                mAllInstructionList[mInstructionCnt].setPath(mDir,mStepsG2[i]);
                mInstructionCnt++;
                Toast.makeText(getApplicationContext(),"Toward Entrance, take steps " +mStepsG2[i], Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==2 && mSrcNum>mDestNum){
            mDir=1;
            for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i-=mDir)
            {
                //mAllInstructionList.add(""+mDir+mStepsG2[i]);
                mAllInstructionList[mInstructionCnt] = new Path();
                mAllInstructionList[mInstructionCnt].setPath(mDir,mStepsG2[i]);
                mInstructionCnt++;
                Toast.makeText(getApplicationContext(),"Toward 105, take steps " + mStepsG2[i], Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int correspondLoc(int val) {
        switch (val){
            case 101: return 111;
            case 102: return 110;
            case 103: return 107;
            case 104: return 106;
            case 106: return 104;
            case 107: return 103;
            case 110: return 102;
            case 111: return 101;
        }
        return -1;
    }

    //----------------------------------Sensor Management-------------------------------------------

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccelerometer(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        if(numSteps==0)
            addObject(Uri.parse("andy.sfb"));
        numSteps++;
        mGallery = (Button) findViewById(R.id.selectbtnid);
        mGallery.setText("Ped:"+numSteps);
        if (numSteps==mAllInstructionList[mInstructionNum].getSteps() && mInstructionNum<mInstructionCnt){
            mInstructionNum++;
            numSteps=0;
        }
        if (mInstructionNum==mInstructionCnt) {
            sensorManager.unregisterListener(ARNavigation.this);
            Toast.makeText(getApplicationContext(),"Destination has arrived",Toast.LENGTH_SHORT);
        }
    }

    //-----------------------------AR Object placement----------------------------------------------

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(fragment, hit.createAnchor(), model);
                    break;
                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    //---------------------------AR green dot center detection methods------------------------------

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }

    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    //----------------------------------------------------------------------------------------------

}