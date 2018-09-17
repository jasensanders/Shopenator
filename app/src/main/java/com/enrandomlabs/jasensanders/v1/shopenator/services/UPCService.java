package com.enrandomlabs.jasensanders.v1.shopenator.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.shopenator.AddItemActivity;
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
 * helper methods.
 */
public class UPCService extends IntentService {
    private static final String LOG_TAG = UPCService.class.getSimpleName();

    //Error
    public static final String NOT_FOUND = LOG_TAG + "_NOT_FOUND";
    public static final String SERVER_ERROR = LOG_TAG + "_SERVER_ERROR";
    public static final String INTERNAL_ERROR = LOG_TAG + "_INTERNAL_ERROR";

    // Messages
    private static final String MESSAGE_NOT_FOUND = "Sorry, The upc was not found in our database.";
    private static final String MESSAGE_SERVER_ERROR = "Sorry, something went wrong with the server.";
    private static final String MESSAGE_INTERNAL_ERROR = "Sorry, the internal data turned up empty.";


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

    public static void startService(Context context, String param1) {
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
        if(results != null){
            if(results[0].equals(" ") || results[0].equals("")){
                sendError(NOT_FOUND, MESSAGE_NOT_FOUND, param1);
            }
            else if(results[0].equals(INTERNAL_ERROR)){
                sendError(INTERNAL_ERROR, MESSAGE_INTERNAL_ERROR, param1);
            }
            else if(results[0].equals(SERVER_ERROR)){
                sendError(SERVER_ERROR, MESSAGE_SERVER_ERROR , results[2]);
            }else{
                sendDataBack(results);
            }
        }else{
            // There's an error.
            // we need to know which one and tell the user and log it.
            sendError(INTERNAL_ERROR, MESSAGE_INTERNAL_ERROR, param1);
        }


    }

    private String requestUPCdata(String upc){

        if(upc == null){return INTERNAL_ERROR + ", " + "001";}

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

            //get result code
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 200) {

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return SERVER_ERROR + ", " + "000";
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
                    return SERVER_ERROR + ", " + "000";
                }
                upcJsonStr = buffer.toString();
            }else{
                return SERVER_ERROR + ", " + String.valueOf(responseCode);
            }
        }catch (IOException e){
            Log.e(LOG_TAG, "IO Error UPC Request", e);

            return SERVER_ERROR + ", " + "000";
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
        if(jsonString != null) {
            if(jsonString.startsWith(SERVER_ERROR) || jsonString.startsWith(INTERNAL_ERROR)){
                return jsonString.split(", ");
            }
            final String ROOT = "0";
            final String PRODUCTNAME = "productname";
            final String IMAGEURL = "imageurl";

            try {
                JSONObject upcJson = new JSONObject(jsonString);
                JSONObject root = upcJson.getJSONObject(ROOT);
                String productName = root.getString(PRODUCTNAME);
                String imageUrl = root.getString(IMAGEURL);
                return new String[]{productName, imageUrl};
            } catch (JSONException j) {
                Log.e(LOG_TAG, "Error Parsing UPC JSON", j);
                return new String[]{INTERNAL_ERROR, "001"};
            }
        }else{
            return new String[]{INTERNAL_ERROR, "001"};
        }
    }

    private void sendError(String type, String message,  String code){
        sendDataBack(new String[]{type, message, code});
    }

    private void sendDataBack(String[] data){
        Intent messageIntent = new Intent(AddItemActivity.SERVICE_EVENT_UPC);
        messageIntent.putExtra(AddItemActivity.SERVICE_EXTRA_UPC, data);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }
}
