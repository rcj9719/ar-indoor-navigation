package com.example.rcjoshi.arinphase2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Destination extends AppCompatActivity {

    private TextView mTextMessage;
    //ArrayAdapter<String> mAdapter;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_navigation_prev:
                    //mTextMessage.setText(R.string.title_home);
                    Intent mPrevIntent = new Intent(Destination.this,MainActivity.class);
                    startActivity(mPrevIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_steps:
                    Intent mGuideIntent = new Intent(Destination.this,MainActivity.class);
                    startActivity(mGuideIntent);
                    finish();
                    return true;
                case R.id.bottom_navigation_next:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent mNextIntent = new Intent(Destination.this,NavigateActivity.class);
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
        setContentView(R.layout.activity_destination);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation_dest);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //mAdapter = ArrayAdapter.createFromResource(this,R.array.destinations,);

        ListView listView = (ListView) findViewById(R.id.destination_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sd=getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed=sd.edit();
                String mText = (String)((TextView) view).getText();
                Toast.makeText(getApplicationContext(),mText,Toast.LENGTH_SHORT).show();
                ed.putString("sdDest",mText);
                ed.commit();
            }
        });
        //listView.setAdapter(mAdapter);

    }

}
