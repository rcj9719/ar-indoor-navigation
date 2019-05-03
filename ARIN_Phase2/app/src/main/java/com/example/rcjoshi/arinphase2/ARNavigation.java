package com.example.rcjoshi.arinphase2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ARNavigation extends AppCompatActivity implements SensorEventListener, StepListener{

    private final int mListenerRegistered;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int numSteps=0, limNumSteps=-1;
    private int mDestNum=0,mSrcNum=0;
    private int mDestGroup=0,mSrcGroup=0;
    private int mDir=0;
    private int mStepsG1[]={25,15,24,14},mStepsG2[]={7,25,4,24,3,20},mStepsCross=7;
    private int mAryPtrSrc,mAryPtrDest;

    public ARNavigation() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        numSteps=0;
        sensorManager.registerListener(ARNavigation.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mListenerRegistered=1;


        //initialise a new string array
        String[] mEachInstruction = new String[]{};
        // Create a List from String Array elements
        final List<String> mAllInstructionList = new ArrayList<String>(Arrays.asList(mEachInstruction));

        String mSavedSrc="Hello 000",mSavedDest="Hello 000";
        SharedPreferences sd=getSharedPreferences("data",Context.MODE_PRIVATE);

        mSavedSrc = sd.getString("sdSrc","");
        mSavedDest = sd.getString("sdDest","");

        Toast.makeText(getApplicationContext(),"Src: "+mSavedSrc
                +"\nDestination: "+mSavedDest,Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getApplicationContext(),"Dest:" +
                    mSavedDest.substring(mSavedDest.length() - 3),Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),"Src:" +
                    mSavedSrc.substring(mSavedSrc.length() - 3),Toast.LENGTH_SHORT).show();
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
            mAllInstructionList.add("Cross the passage");
        }
        if (mSrcGroup==1 && mSrcNum>mDestNum) {
            mDir=-1;
            for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i+=mDir)
            {
                mAllInstructionList.add(mStepsG1[i] + " steps towards Entrance");
                Toast.makeText(getApplicationContext(), mStepsG1[i] +
                        " steps towards Entrance", Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==1 && mSrcNum<mDestNum){ //103,104
            mDir=1;
            for (int i=mAryPtrSrc; i<mAryPtrDest; i+=mDir)
            {
                mAllInstructionList.add(mStepsG1[i] + " steps towards 105");
                Toast.makeText(getApplicationContext(),mStepsG1[i]+
                        " steps towards 105",Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==2 && mSrcNum<mDestNum){
            mDir=1;
            for (int i=mAryPtrSrc; i<mAryPtrDest; i+=mDir)
            {
                mAllInstructionList.add(mStepsG2[i] + " steps towards Entrance");
                Toast.makeText(getApplicationContext(),mStepsG2[i]+
                        " steps towards Entrance",Toast.LENGTH_SHORT).show();
            }
        }
        else if (mSrcGroup==2 && mSrcNum>mDestNum){
            mDir=-1;
            for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i+=mDir)
            {
                mAllInstructionList.add(mStepsG2[i] + " steps towards 105");
                Toast.makeText(getApplicationContext(),mStepsG2[i]+
                        " steps towards 105",Toast.LENGTH_SHORT).show();
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
        numSteps++;
        //mNumStepsMsg.setText("Steps : " + numSteps);
        if (numSteps==limNumSteps) {
            //mListenerRegistered = 0;
            sensorManager.unregisterListener(ARNavigation.this);
            numSteps=0;
        }
    }
}
