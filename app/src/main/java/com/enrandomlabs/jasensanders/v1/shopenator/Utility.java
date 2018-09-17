package com.enrandomlabs.jasensanders.v1.shopenator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class Utility {


    public static final String BACKUP_FOLDER = "/Shopenator";

    public static void setStringPreference(Context c, String key, String value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void deletePreference(Context c, String key){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    public static String getStringPreference(Context c, String key, String deFault){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

        return settings.getString(key, deFault);
    }

    // Check first run of any activity of fragment by passing its name into String "which"
    // If "which" is null it will check if this is the first run of the whole app.
    public static boolean isFirstRun(Context context, final String which){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        final String FIRST_RUN_FLAG;
        if(which == null) {

            FIRST_RUN_FLAG = context.getPackageName() + "_FIRST_RUN_FLAG";
        }
        else{

            FIRST_RUN_FLAG = which + "_FIRST_RUN_FLAG";
        }
        if(settings.getBoolean(FIRST_RUN_FLAG, true)){

            editor.putBoolean(FIRST_RUN_FLAG, false).apply();
            return true;

        }
        return false;

    }

    public static long getFirstRunTime(Context context){
        String name = context.getPackageName();
        long installed;
        try {
            installed = context
                    .getPackageManager()
                    .getPackageInfo(name, 0)
                    .firstInstallTime
            ;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return 0;
        }
        return installed;
    }

    //Checks for internet connectivity
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    //Checks if the String is numeric  Does not check for size.
    public static boolean isNum(String s) {

        return isHex(s) || isDec(s) || isBin(s) || isInt(s);

    }

    public static boolean isHex(String h){

        if((h.charAt(0) == '0' && h.charAt(1) == 'x') ){
            return h.substring(2,h.length()).matches("([0-9]|[A-F]|[a-f])+");
        }

        if(h.charAt(0) == '#'){
            return h.substring(1,h.length()).matches("([0-9]|[A-F]|[a-f])+");
        }
        return false;
    }

    public static boolean isDec(String d){

        // Strip off sign if present
        if(d.charAt(0) == '-'){
            d = d.substring(1);
        }

        // Check for digits, allow only one dot
        boolean dot = false;
        int len = d.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(d.charAt(i)) ) {
                if (d.charAt(i) == '.' ){
                    if(!dot) {
                        dot = true;
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }

        }

        return dot;
    }

    public static boolean isBin(String b){
        for(int i =0; i<b.length(); i++){
            if(b.charAt(i) != '0' && b.charAt(i) != '1'){
                return false;
            }
        }
        return true;
    }

    public static boolean isInt(String in){

        int start = 0;
        if(in.charAt(0) == '-'){
            start = 1;
        }

        int len = in.length();
        for (int i = start; i < len; ++i) {
            if (!Character.isDigit(in.charAt(i)) ) {
                return false;
            }
        }

        return true;
    }


    public static String stringArrayToString(String[] inString, String separator){
        String space = " ";
        StringBuilder result = new StringBuilder();

        if(separator.equals("") || separator.equals(" ")){
            space = "";
        }
        int i = 0;
        for(String add: inString){
            if(i != inString.length -1) {
                String toAdd = add + separator + space;
                result.append(toAdd);
                i++;
            }else{
                result.append(add);
            }
        }

        return result.toString();
    }

    public static String reverse(String input){

        char[] arr = input.toCharArray();
        int end = input.length()-1;
        for (int i=0; i<end; i++){
            char tmp = arr[i];
            arr[i] = arr[end];
            arr[end]=tmp;
            end--;
        }
        return new String(arr);
    }

    public static boolean copyFile(File src, File dst){

        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }catch (IOException e ){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean moveFile(File src, File dst){

        boolean success = false;
        // If you can't copy original, then don't delete it.
        if(copyFile(src, dst)){
            success = deleteFile(src);
        }
        return success;

    }

    public static boolean deleteFile(File filePathIncludingFilename) {
        boolean result;
        try {
            result = filePathIncludingFilename.delete();
        }catch (SecurityException e){
            e.printStackTrace();
            return false;
        }
        return result;

    }

    public static void restart(Activity activity, int delayInMilliseconds, Class<?> launcherClass){
        //Activity to kill and context to start from: activity
        //Delay in MS: delayInMilliseconds
        //Launcher Class that starts your app: launcherClass

        Intent mStartActivity = new Intent(activity, launcherClass);
        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + delayInMilliseconds, mPendingIntent);
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            activity.finish();
        }else {
            activity.finishAffinity();
        }

    }

    public static void exit(Activity activity){
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            activity.finish();
        }else {
            activity.finishAffinity();
        }
    }

    public static boolean APIgreaterThan(int apiLevel){
        return Build.VERSION.SDK_INT > apiLevel;
    }

    public static boolean APIlessThan(int apiLevel){
        return Build.VERSION.SDK_INT < apiLevel;
    }



    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean backupDatabaseToExternal(Context context, String databaseName, String backupDatabaseName){

        if(isExternalStorageWritable()) {
            try {

                File exportDir;
                File folioDir;

                //Build output directory path
                exportDir = Environment.getExternalStorageDirectory();
                folioDir = new File(exportDir, BACKUP_FOLDER);

                //Get Database file
                File dbFile = context.getDatabasePath(databaseName);
                File outFile;

                //Make Sure output directory exists
                folioDir.mkdirs();

                //make outfile
                outFile = new File(folioDir, backupDatabaseName);
                outFile.createNewFile();

                //Copy data from DB to backup file.
                copyFile(dbFile, outFile);
                //Log.v("Backup Database Size", String.valueOf(dbFile.length()));
                //Log.v("New Database Size", String.valueOf(outFile.length()));

                if (dbFile.length() == outFile.length()) {

                    return true;
                }

            } catch (IOException e) {
                Log.e("Shopenator_Backup", "Backup DataBase Failed", e);
                e.printStackTrace();

            }
        }
        return false;

    }

    public static boolean restoreDatabaseFromExternal(Context context, String databaseName, String backupDatabaseName){

        if(isExternalStorageWritable()) {
            FileChannel src;
            FileChannel dst;
            try {

                File exportDir;
                File backupDir;
                //Build output directory path
                exportDir = Environment.getExternalStorageDirectory();
                backupDir = new File(exportDir, BACKUP_FOLDER);

                //Get Database File
                File CurrentDB = context.getDatabasePath(databaseName);
                File BackupDB;


                //Make Sure output directory exists
                if (exportDir.exists()) {

                    //If this is a reinstall, the Database file may not exist
                    //so create it.
                    if(!CurrentDB.exists()){
                        CurrentDB.createNewFile();
                    }

                    //Get outfile
                    BackupDB = new File(backupDir, backupDatabaseName);
                    //Make sure Database files exist
                    if (BackupDB.exists()&& CurrentDB.exists()) {
                        src = new FileInputStream(BackupDB).getChannel();
                        dst = new FileOutputStream(CurrentDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        return true;

                    }
                }
                return false;

            } catch (IOException e) {
                Log.e("Shopenator_Backup", "Restore from Backup DataBase Failed", e);
                e.printStackTrace();

            }
        }
        return false;
    }

    public static String addDateToYear(String date){
        String[] temp;
        try{
            temp = date.split(", ");
            return temp[1];
        }catch (NullPointerException e){
            //Log.e("Utility class","Error splitting addDate year");
            return "XXXX";
        }

    }

    public static ContentValues createStringContentValues(String[] data, String[] rowProjection){

        ContentValues insertItem = new ContentValues();
        if(data.length == rowProjection.length) {
            for (int i = 0; i < data.length; i++) {
                insertItem.put(rowProjection[i], data[i]);
            }
        }
        return insertItem;

    }

    public static String rectifyUPC(String upc){
        if(verifyUPC(upc)){

            if(upc.length() == 10 ){

                return isbn10to13(upc);
            }
            if(upc.length() == 12 || upc.length() == 13){
                return upc;
            }
        }
        return null;
    }


    public static boolean verifyUPC(String upc){


        if(isInt(upc)) {
            if (upc.length() == 10) {

                //check digit
                int sum = 0;
                int multiple = 10;
                for (int i = 0; i < 10; i++) {
                    sum = sum + (Character.getNumericValue(upc.charAt(i)) * multiple);
                    multiple--;
                }

                return sum % 11 == 0;

            }
            if (upc.length() == 12) {


                int multiple = 3;
                // Initialize sum
                int sum = 0;

                for (int i = 0; i < 12; i++) {
                    //even index is the odd value when zero indexed
                    if (i % 2 == 0) {
                        sum = sum + (Character.getNumericValue(upc.charAt(i)) * multiple);
                    } else {
                        sum = sum + Character.getNumericValue(upc.charAt(i));
                    }
                }

                return sum % 10 == 0;

            }
            if (upc.length() == 13) {
                int multiple = 3;
                // Initialize sum
                int sum = 0;

                for (int i = 0; i < 13; i++) {
                    //even numbered index is odd sequentially.
                    if (i % 2 == 0) {
                        sum = sum + Character.getNumericValue(upc.charAt(i));
                    } else {
                        sum = sum + (Character.getNumericValue(upc.charAt(i)) * multiple);
                    }
                }

                return sum % 10 == 0;

            }
        }

        return false;
    }

    public static String isbn10to13(String isbn10){
        if(isbn10.length()==10) {

            //prep new number - add "978" to front and drop last digit
            String temp = "978" + isbn10.substring(0,9);


            int checksum = 38; // term value sum of "978"

            int term1 = Character.getNumericValue(isbn10.charAt(0))*3;
            int term2 = Character.getNumericValue(isbn10.charAt(1));
            int term3 = Character.getNumericValue(isbn10.charAt(2))*3;
            int term4 = Character.getNumericValue(isbn10.charAt(3));
            int term5 = Character.getNumericValue(isbn10.charAt(4))*3;
            int term6 = Character.getNumericValue(isbn10.charAt(5));
            int term7 = Character.getNumericValue(isbn10.charAt(6))*3;
            int term8 = Character.getNumericValue(isbn10.charAt(7));
            int term9 = Character.getNumericValue(isbn10.charAt(8))*3;
            checksum = checksum + term1 + term2 + term3 + term4 + term5 + term6 + term7 + term8
                    + term9;
            int checkdigit = checksum%10;
            if(checkdigit != 0) {
                checkdigit = 10 - checkdigit;
            }

            temp = temp + String.valueOf(checkdigit);
            return temp;
        }else{
            return null;
        }
    }

    public static String[] upcEtoA(String in){

        //TODo convert upc-e to upc-a;
        //ToDo update verify/rectify to verify 6 digit upc-e values.
        return null;
    }




}
