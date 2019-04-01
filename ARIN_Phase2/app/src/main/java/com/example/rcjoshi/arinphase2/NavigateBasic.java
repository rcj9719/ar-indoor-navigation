package com.example.rcjoshi.arinphase2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class NavigateBasic extends AppCompatActivity {

    private TextView mSrcMessage,mDestMessage;
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
                    Intent mPrevIntent = new Intent(NavigateBasic.this, SourceIdentification.class);
                    startActivity(mPrevIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_steps:
                    Intent mGuideIntent = new Intent(NavigateBasic.this, MainActivity.class);
                    startActivity(mGuideIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_next:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_basic);

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
            Toast.makeText(getApplicationContext(),"Dest:"+mSavedDest.substring(mSavedDest.length() - 3),Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),"Src:"+mSavedSrc.substring(mSavedSrc.length() - 3),Toast.LENGTH_SHORT).show();
            mSrcNum = Integer.parseInt(mSavedSrc.substring(mSavedSrc.length() - 3));
            //mSrcNum=0;
            if (mSrcNum<=105)
                mSrcGroup=1;
            else
                mSrcGroup=2;
        }

        // 25 15 24 14
        // 7 24 3 24 3 20
        mAryPtrSrc = mSrcNum-101;   // 110-101=9
        mAryPtrDest = mDestNum-101; // 111-101=10
        if (mAryPtrSrc>3) mAryPtrSrc-=4;    // 5
        if (mAryPtrDest>3) mAryPtrDest-=4;  // 6

        if (mSrcGroup==mDestGroup)
        {
            Toast.makeText(getApplicationContext(),"Same Group",Toast.LENGTH_SHORT).show();
            if (mSrcGroup==1 && mSrcNum>mDestNum) { // srcnum=103 destnum = 101
                mDir=-1;
                for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i+=mDir)
                {
                    Toast.makeText(getApplicationContext(),"Steps:"+mStepsG1[i]+"Towards Entrance",Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(),"Direction: Towards Entrance",Toast.LENGTH_SHORT).show();
            }
            else if (mSrcGroup==1 && mSrcNum<mDestNum){
                mDir=1;
                for (int i=mAryPtrSrc; i<mAryPtrDest; i+=mDir)
                {
                    Toast.makeText(getApplicationContext(),"Steps:"+mStepsG1[i]+"Towards 105",Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(),"Direction: Towards 105",Toast.LENGTH_SHORT).show();
            }
            else if (mSrcGroup==2 && mSrcNum<mDestNum){
                mDir=1;
                for (int i=mAryPtrSrc; i<mAryPtrDest; i+=mDir)
                {
                    Toast.makeText(getApplicationContext(),"Steps:"+mStepsG2[i]+"Towards Entrance",Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(),"Direction: Towards Entrance",Toast.LENGTH_SHORT).show();
            }
            else if (mSrcGroup==2 && mSrcNum>mDestNum){
                mDir=-1;
                for (int i=mAryPtrSrc-1; i>=mAryPtrDest; i+=mDir)
                {
                    Toast.makeText(getApplicationContext(),"Steps:"+mStepsG2[i]+"Towards 105",Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(),"Direction: Towards 105",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Cross the Passage",Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
//3 24 4 25