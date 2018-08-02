package com.enrandomlabs.jasensanders.v1.shopenator.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.shopenator.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UPCService extends IntentService {
    private static final String LOG_TAG = UPCService.class.getSimpleName();

    //Error
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String SERVER_ERROR = "ERROR";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_UPC = "com.enrandomlabs.jasensanders.v1.shopenator.services.action.ACTION_GET_UPC";


    // TODO: Rename parameters
    private static final String EXTRA_UPC = "com.enrandomlabs.jasensanders.v1.shopenator.services.extra.EXTRA_UPC";


    public UPCService() {
        super("UPCService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startService(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UPCService.class);
        intent.setAction(ACTION_GET_UPC);
        intent.putExtra(EXTRA_UPC, param1);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_UPC.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_UPC);
                handleActionUpcLookup(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpcLookup(String param1) {

        String upcJson = requestUPCdata(param1);
        String[] results = parseUPCdataJson(upcJson);

        //TODO: setup broadcast receiver first.
        //sendDataBack(results);
    }

    private String requestUPCdata(String upc){

        if(upc == null){return null;}

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String upcJsonStr;

        try{
            final String SEARCHUPC_BASE_URL = "https://secure25.win.hostgator.com/searchupc_com/handlers/upcsearch.ashx?";
            final String REQUEST_TYPE = "request_type";
            final String TYPE_JSON = "3";
            final String ACCESS_TOKEN = "access_token";
            final String UPC_KEY = BuildConfig.SEARCH_UPC_KEY;
            final String UPC = "upc";

            Uri builtUri = Uri.parse(SEARCHUPC_BASE_URL).buildUpon()
                    .appendQueryParameter(REQUEST_TYPE, TYPE_JSON)
                    .appendQueryParameter(ACCESS_TOKEN, UPC_KEY)
                    .appendQueryParameter(UPC, upc)
                    .build();


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            upcJsonStr = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "IO Error UPC Request", e);

            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException f){
                    Log.e(LOG_TAG, "Error Closing Stream UPC Request", f);

                }
            }
        }
        return upcJsonStr;
    }

    private String[] parseUPCdataJson(String jsonString){
        final String ROOT = "0";
        final String PRODUCTNAME = "productname";
        final String IMAGEURL = "imageurl";

        try {
            JSONObject upcJson = new JSONObject(jsonString);
            JSONObject root = upcJson.getJSONObject(ROOT);
            String productName = root.getString(PRODUCTNAME);
            String imageUrl = root.getString(IMAGEURL);
            return new String[]{productName, imageUrl};
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error Parsing UPC JSON", j );
            return null;
        }
    }

//    private void sendApologies(String message){
//        sendDataBack( new String[]{message, param1, "none", NOT_FOUND});
//    }
//
//    private void sendError(String message){
//        sendDataBack(new String[]{message, param1, "none", SERVER_ERROR});
//    }
//
//    private void sendDataBack(String[] movieData){
//        Intent messageIntent = new Intent(AddNewActivity.SERVICE_EVENT_MOVIE);
//        messageIntent.putExtra(AddNewActivity.SERVICE_EXTRA_MOVIE,movieData);
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
//    }
}
