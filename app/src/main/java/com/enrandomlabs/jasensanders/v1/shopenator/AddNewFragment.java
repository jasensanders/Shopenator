package com.enrandomlabs.jasensanders.v1.shopenator;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enrandomlabs.jasensanders.v1.shopenator.barcode.BarcodeActivity;
import com.enrandomlabs.jasensanders.v1.shopenator.database.DataContract;
import com.enrandomlabs.jasensanders.v1.shopenator.services.UPCService;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddNewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AddNewFragment.class.getSimpleName();

    // Fragment initialization parameters a Uri or null
    private static final String ARG_PARAM1 = "param1";

    //Message Receiver Keys
    public static final String SERVICE_EVENT_UPC_LOOKUP = "SERVICE_EVENT_UPC_LOOKUP";
    public static final String SERVICE_EXTRA_UPC_LOOKUP = "SERVICE_EXTRA_UPC_LOOKUP";

    //Error
    private static final String NOT_FOUND = UPCService.NOT_FOUND;
    private static final String SERVER_ERROR = UPCService.SERVER_ERROR;
    private static final String INTERNAL_ERROR = UPCService.INTERNAL_ERROR;

    // Barcode Flag
    private static final int RC_BARCODE_CAPTURE = 9001;

    // Loader type
    private static final int ITEM_LOADER = 1001;

    // Barcode image prefix
    private static final String BARCODE_IMG_PREFIX = "http://www.searchupc.com/drawupc.aspx?q=";

    // SavedInstanceState Keys
    private static final String SAVED_UPC = "com.enrandomlabs.jasensanders.v1.shopenator.SAVED_UPC";
    private static final String SAVED_VALUES = "com.enrandomlabs.jasensanders.v1.shopenator.SAVED_VALUES";

    private Uri mItemData;
    private boolean mIsDetailView = false;

    private TextView mError;

    private LinearLayout mInputUpcLayout;

    private BroadcastReceiver mMessageReceiver;

    private String mImageSource;
    private String mBarcodeImage;
    private String mUPCString;
    private boolean mOwned = false;

    private EditText mUpc;
    private EditText mTitle;
    private EditText mStore;
    private EditText mPrice;
    private EditText mUrl;
    private EditText mDescription;
    private EditText mNotes;

    private ImageView mImage;

    private ProgressBar mSpin;

    private Button mScan;
    private Button mSubmit;
    private Button mSave;
    private Button mDelete;



    public AddNewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     *
     * @return A new instance of fragment AddNewFragment.
     */
    public static AddNewFragment newInstance(Uri param1) {
        AddNewFragment fragment = new AddNewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    //TODO: LIFECYCLE STATE TRACKING
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemData = getArguments().getParcelable(ARG_PARAM1);
            if(mItemData != null) {
                mUPCString = DataContract.ItemEntry.getUpcFromUri(mItemData);
                mIsDetailView = true;
            }else {mIsDetailView = false;}

        }
        if(savedInstanceState != null){
            mUPCString = savedInstanceState.getString(SAVED_UPC);
            mBarcodeImage = BARCODE_IMG_PREFIX + mUPCString;

        }
        registerMessageReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_add_new, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        if(toolbar != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get references to views, set views based on viewType: Detail or New.
        initializeViews(root, mIsDetailView);

        if(mIsDetailView){
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }

        if(savedInstanceState != null){

            onRestoreState(savedInstanceState);
        }

        return root;

    }

    private class ServiceMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringArrayExtra(SERVICE_EXTRA_UPC_LOOKUP) != null ) {
                //Kill the progress bar
                if(mSpin != null) {
                    mSpin.setVisibility(View.GONE);
                }
                //Report to user with results
                String[] received = intent.getStringArrayExtra(SERVICE_EXTRA_UPC_LOOKUP);
                if (received[0].equals(NOT_FOUND)|| received[0].equals(SERVER_ERROR) || received[0].equals(INTERNAL_ERROR) ) {

                    Log.e(LOG_TAG, "SendApologies Error Received: "+ received[0] + received[2]);
                    Toast.makeText(getActivity(), received[1], Toast.LENGTH_SHORT).show();

                }else{

                    // If Error message showing, clear it.
                    if(mError.getVisibility() == View.VISIBLE){
                        mError.setVisibility(View.GONE);
                    }
                    // We got Data! Lets show what we got!!!
                    updateViews(null, received);


                }

            }

        }
    }

    private void registerMessageReceiver(){

        //If there isn't a messageReceiver yet make one.
        if(mMessageReceiver == null) {
            mMessageReceiver = new ServiceMessageReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_EVENT_UPC_LOOKUP);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, filter);

    }
    private void unRegisterMessageReceiver(){

        if(mMessageReceiver != null){
            try {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
            }catch(Exception e){
                Log.e(LOG_TAG, "messageReciever unregisterd already", e);
            }
        }
    }

    @Override
    public void onPause() {
        unRegisterMessageReceiver();
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        registerMessageReceiver();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_UPC, mUPCString);
        outState.putStringArray(SAVED_VALUES, loadItemData(mIsDetailView));
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        return new CursorLoader(getActivity(),
                mItemData,
                DataContract.ITEM_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if(data.moveToFirst()){

            updateViews(data, null);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }



    private void initializeViews(View view, final boolean mIsDetailView){

        //Todo: Make extra UPC TextView for displaying the upc.

        // Get References
        mError = view.findViewById(R.id.error);

        mInputUpcLayout = view.findViewById(R.id.inputContainer);
        mUpc = view.findViewById(R.id.upc);
        mScan = view.findViewById(R.id.scan_button);
        mSubmit = view.findViewById(R.id.submit);

        mSpin = view.findViewById(R.id.ProgressBarWait);

        mImage = view.findViewById(R.id.artView);
        mTitle = view.findViewById(R.id.add_view_Title);
        mStore = view.findViewById(R.id.store);
        mPrice = view.findViewById(R.id.price);
        mUrl = view.findViewById(R.id.url);
        mDescription = view.findViewById(R.id.description);
        mNotes = view.findViewById(R.id.notes);

        mSave = view.findViewById(R.id.saveButton);
        mDelete = view.findViewById(R.id.deleteButton);


        // Set Visibility and Button Listeners
        // based on view type
        if(mIsDetailView){
            // If Detail view, hide UPC input views
            mInputUpcLayout.setVisibility(View.GONE);
            mUpc.setVisibility(View.GONE);
            mScan.setVisibility(View.GONE);
            mSubmit.setVisibility(View.GONE);

            mUPCString = DataContract.ItemEntry.getUpcFromUri(mItemData);

            // Show delete button, set listener
            mDelete.setVisibility(View.VISIBLE);
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Delete item
                    deleteFromDB(mItemData);

                }
            });
        }else{
            mScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Launch barcode scan activity
                    launchBarcodeScanner();

                }
            });
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Verify UPC and submit
                    onSubmitUpc();
                }
            });
        }

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] item = loadItemData(mIsDetailView);

                // Save or Update to database
                if(mIsDetailView){

                    ContentValues updateValues = Utility.createStringContentValues(item, DataContract.UPDATE_ITEM_COLUMNS);
                    updateDB(updateValues);

                }else{

                    // Verify minimum input. First! Must have UPC and Title

                    if(Utility.verifyUPC(item[1]) && !item[3].equals("")){
                        ContentValues insertValues = Utility.createStringContentValues(item, DataContract.INSERT_ITEM_COLUMNS);
                        insertToDB(insertValues);

                    }else{

                        // Apologise to user
                        Toast message = Toast.makeText(getActivity(), "Please enter a valid UPC and Product Name", Toast.LENGTH_LONG);
                        message.setGravity(Gravity.CENTER, 0, 0);
                        message.show();
                    }

                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {

            if (resultCode == CommonStatusCodes.SUCCESS) {

                if (data != null) {

                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BarcodeObject);
                    //Update the EditText and String Variable
                    mUPCString = barcode.displayValue;
                    mItemData = DataContract.ItemEntry.buildUPCUri(barcode.displayValue);
                    mUpc.setText(barcode.displayValue);
                    mBarcodeImage = BARCODE_IMG_PREFIX + mUPCString;

                } else {

                    //Tell the user the Scan Failed, then log event.
                    mError.setText(R.string.barcode_failure);
                    mError.setVisibility(View.VISIBLE);

                }
            } else {

                //Tell the user the Activity Failed
                mError.setVisibility(View.VISIBLE);
                mError.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateViews(Cursor data, String[] received){

        // If we are loading values from database
        if(data != null) {
            // Update the views with data
            mUPCString = data.getString(DataContract.COL_UPC);
            mImageSource = data.getString(DataContract.COL_ART);
            mBarcodeImage = data.getString(DataContract.COL_BAR_IMG);
            mOwned = (data.getString(DataContract.COL_STATUS).equals(DataContract.STATUS_OWN));

            Glide.with(getActivity()).load(mImageSource).fitCenter().into(mImage);
            mTitle.setText(data.getString(DataContract.COL_TITLE));
            mStore.setText(data.getString(DataContract.COL_STORE));
            mPrice.setText(data.getString(DataContract.COL_PRICE));
            mUrl.setText(data.getString(DataContract.COL_URL));
            mDescription.setText(data.getString(DataContract.COL_DESC));
            mNotes.setText(data.getString(DataContract.COL_NOTES));
        }

        if(received != null){
            mTitle.setText(received[0]);
            mImageSource = received[1];
            Glide.with(getActivity()).load(mImageSource).fitCenter().into(mImage);
            mBarcodeImage = BARCODE_IMG_PREFIX + mUpc.getText();

        }


    }

    private void onRestoreState(Bundle savedInstanceState){
        String[] values = savedInstanceState.getStringArray(SAVED_VALUES);

        // If this is a detail view load most current values
            if(values.length == 9){
                mUpc.setText(mUPCString);
                mImageSource = values[2];
                mOwned = (values[6].equals(DataContract.STATUS_OWN));
                Glide.with(getActivity()).load(mImageSource).fitCenter().error(R.drawable.gears_placeholder2).into(mImage);
                mTitle.setText(values[1]);
                mStore.setText(values[4]);
                mPrice.setText(values[3]);
                mUrl.setText(values[8]);
                mDescription.setText(values[7]);
                mNotes.setText(values[5]);


            }else{
                // Otherwise it a new item, add latest values.
                mUpc.setText(mUPCString);
                mImageSource = values[4];
                Glide.with(getActivity()).load(mImageSource).fitCenter().error(R.drawable.gears_placeholder2).into(mImage);
                mOwned = values[11].equals(DataContract.STATUS_OWN);
                mTitle.setText(values[3]);
                mStore.setText(values[9]);
                mPrice.setText(values[6]);
                mUrl.setText(values[13]);
                mDescription.setText(values[12]);
                mNotes.setText(values[10]);


            }

    }

    private String[] loadItemData(boolean mIsDetailView){
        ArrayList <String> data = new ArrayList<>();

        // Get today's date
        DateFormat df = DateFormat.getDateInstance();
        String addDate = df.format(Calendar.getInstance().getTime());

        // Verify ImageSource
        if(mImageSource == null){
            mImageSource = DataContract.PLACEHOLDER;
        }

        // pull item data and put in String array based on view type

        if(!mIsDetailView) {

            data.add(DataContract.SEARCHUPC_API_ID);
            mUPCString = mUpc.getText().toString();
            data.add(mUPCString);

        }

        data.add(mImageSource);
        data.add(mTitle.getText().toString());
        data.add(mImageSource);

        if(!mIsDetailView) {


            mBarcodeImage = BARCODE_IMG_PREFIX + mUPCString;
            data.add(mBarcodeImage);

        }

        data.add(mPrice.getText().toString());

        if(!mIsDetailView) {

            data.add(addDate);
            data.add(addDate);

        }
        data.add(mStore.getText().toString());
        data.add(mNotes.getText().toString());

        if(!mIsDetailView){

            data.add(DataContract.STATUS_RESEARCH);

        }else{

            if(mOwned){

                data.add(DataContract.STATUS_OWN);

            }else{

                data.add(DataContract.STATUS_RESEARCH);
            }
        }

        data.add(mDescription.getText().toString());
        data.add(mUrl.getText().toString());

        if(!mIsDetailView){

            data.add(DataContract.PLACEHOLDER);
            data.add(DataContract.PLACEHOLDER);

        }

        return data.toArray(new String[data.size()]);
    }

    private void insertToDB(ContentValues insert){


        Uri returned = getActivity().getContentResolver().insert(DataContract.ItemEntry.CONTENT_URI, insert);

        if(returned != null){
            Toast.makeText(getActivity(), mTitle.getText().toString() + getString(R.string.itemSaved), Toast.LENGTH_LONG).show();
        }



    }

    private void updateDB(ContentValues updateValues){

        Uri update = DataContract.ItemEntry.buildUPCUri(mUPCString);
        String saveSelection = DataContract.ItemEntry.COLUMN_UPC + " = ?";
        int rowsUpdated;

        //Attempt update
        rowsUpdated = getActivity().getContentResolver().update(update,updateValues,
                saveSelection,
                new String[]{mUPCString});
        if(rowsUpdated == 1){
            Toast.makeText(getActivity(), getString(R.string.detailItemUpdated), Toast.LENGTH_SHORT).show();
        }

    }

    private boolean deleteFromDB(Uri item){
        boolean deleted = false;

        //Determine from which list we are deleting
        Uri deleteItem = item;
        String deleteSelection = DataContract.ItemEntry.COLUMN_UPC + " = ?";

        //Attempt delete
        int rowsDeleted = getActivity().getContentResolver().delete(deleteItem,
                deleteSelection,
                new String[]{mUPCString});
        //Notify User
        if(rowsDeleted == 1){
            Toast.makeText(getActivity(), getString(R.string.detailItemRemoved), Toast.LENGTH_SHORT).show();
            deleted = true;
        }

        return deleted;
    }

    public void launchBarcodeScanner(){

        // If an error message hasn't cleared, then clear it.
        if(mError.getVisibility() == View.VISIBLE){
            mError.setVisibility(View.GONE);
        }

        Intent intent = new Intent(getActivity(), BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    private void onSubmitUpc(){

        String upc = mUpc.getText().toString();

        // Check upc for correctness and make sure 10 digit isbn becomes 13 digit.
        upc = Utility.rectifyUPC(upc);

        if(upc != null){

            // Start the progress bar
            if(mSpin != null) {
                mSpin.setIndeterminate(true);
                mSpin.setVisibility(View.VISIBLE);
            }

            // Launch service
            UPCService.startService(getActivity(), upc);
        }
        else{

            Toast message = Toast.makeText(getActivity(), "That is not a Valid UPC/ISBN", Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, 0, 0);
            message.show();
        }
    }
}
