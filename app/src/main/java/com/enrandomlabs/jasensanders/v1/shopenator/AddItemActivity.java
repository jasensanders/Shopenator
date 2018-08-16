package com.enrandomlabs.jasensanders.v1.shopenator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class AddItemActivity extends AppCompatActivity {

    //Message Receiver Keys
    public static final String SERVICE_EVENT_UPC = "com.enrandomlabs.jasensanders.v1.shopenator.SERVICE_EVENT_ITEM";
    public static final String SERVICE_EXTRA_UPC = "com.enrandomlabs.jasensanders.v1.shopenator.SERVICE_EXTRA_ITEM";

    private static final String FRAGMENT_VIEW_KEY = "mContent";

    private Fragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Intent intent = getIntent();
        Uri data = intent.getData();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState != null){
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_VIEW_KEY);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.add_new_container, mContent)
                    .commit();
        }else {

            if (data != null) {
                // Loading an Item we already have
                mContent = AddNewFragment.newInstance(data);

            } else {
                // Adding a new item
                mContent = AddNewFragment.newInstance(null);

            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.add_new_container, mContent)
                    .commit();

        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance on rotation
        getSupportFragmentManager().putFragment(outState, FRAGMENT_VIEW_KEY, mContent);

    }
}
