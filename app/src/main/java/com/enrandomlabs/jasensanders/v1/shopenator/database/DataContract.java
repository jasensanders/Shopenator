package com.enrandomlabs.jasensanders.v1.shopenator.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataContract {
    public static final String CONTENT_AUTHORITY ="com.enrandomlabs.jasensanders.v1.shopenator";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ITEM_LIST = "items";
    public static final String PATH_ITEMS_UPC = "items/upc";
    public static final String PATH_ITEMS_AID = "items/apiId";
    public static final String PATH_ITEMS_ID = "items/id";
    public static final String PATH_SEARCH_ALL = "search";

    //Segments
    public static final String SEG_SEARCH = "search";
    public static final String SEG_UPC = "upc";
    public static final String SEG_AID = "apiId";
    public static final String SEG_ID = "id";

    // SQL constants
    public static final String ASC = " ASC";
    public static final String DESC = " DESC";

    public static final String SEARCH_SEG = " LIKE ? OR ";
    public static final String SEARCH_END_SEG = " LIKE ? ";

    //Search Content Type
    public static final String SEARCH_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_ALL;

    //Search Content URI
    public static final Uri SEARCH_CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_ALL).build();

    public static Uri buildSearchUri(String query){
        return SEARCH_CONTENT_URI.buildUpon().appendPath(query).build();
    }

    public static final String[] ITEM_COLUMNS = {
            ItemEntry._ID,
            ItemEntry.COLUMN_API_TYPE_ID,
            ItemEntry.COLUMN_UPC,
            ItemEntry.COLUMN_THUMB,
            ItemEntry.COLUMN_TITLE,
            ItemEntry.COLUMN_PACKAGE_ART,
            ItemEntry.COLUMN_BARCODE_IMG,
            ItemEntry.COLUMN_SEVEN,
            ItemEntry.COLUMN_R_DATE,
            ItemEntry.COLUMN_ADD_DATE,
            ItemEntry.COLUMN_STORE,
            ItemEntry.COLUMN_NOTES,
            ItemEntry.COLUMN_STATUS,
            ItemEntry.COLUMN_DESCRIPTION,
            ItemEntry.COLUMN_FOURTEEN,
            ItemEntry.COLUMN_FIFTEEN,
            ItemEntry.COLUMN_SIXTEEN
    };

    // Item Column Indexes
    public static final int COL_ID = 0;
    public static final int COL_API_ID = 1;
    public static final int COL_UPC = 2;
    public static final int COL_THUMB = 3;
    public static final int COL_TITLE = 4;
    public static final int COL_ART = 5;
    public static final int COL_BAR_IMG = 6;
    public static final int COL_SEVEN = 7;
    public static final int COL_DATE = 8;
    public static final int COL_ADD_DATE = 9;
    public static final int COL_STORE = 10;
    public static final int COL_NOTES = 11;
    public static final int COL_STATUS = 12;
    public static final int COL_DESC =13;
    public static final int COL_FOURTEEN = 14;
    public static final int COL_FIFTEEN = 15;
    public static final int COL_SIXTEEN = 16;


    public static final class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "itemlist";

        // ID number as a string
        public static final String COLUMN_API_TYPE_ID = "API_ID";
        // String UPC code for item
        public static final String COLUMN_UPC = "UPC";
        // Url for the thumb image
        public static final String COLUMN_THUMB = "THUMB";
        // String item name
        public static final String COLUMN_TITLE = "TITLE";
        // Url list of package art
        public static final String COLUMN_PACKAGE_ART = "PACKAGE_ART";
        // Url for the upc barcode image
        public static final String COLUMN_BARCODE_IMG = "BARCODE_IMG";
        // String title of movie
        public static final String COLUMN_SEVEN = "SEVEN";
        // Item Release Date String in the format yyyy-mm-dd
        public static final String COLUMN_R_DATE = "RELEASE_DATE";
        // Date Item Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        // String Store item was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        // String of notes about item 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of movie either WISH or OWN
        public static final String COLUMN_STATUS = "STATUS";
        // String of the Short description
        public static final String COLUMN_DESCRIPTION = "DESCRIPTION";
        //String of Trailer youtube urls. comma separated.
        public static final String COLUMN_FOURTEEN = "FOURTEEN";
        //String of movie/tv genres. comma separated.
        public static final String COLUMN_FIFTEEN = "FIFTEEN";
        //String imdb number
        public static final String COLUMN_SIXTEEN = "SIXTEEN";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM_LIST;

        public static Uri buildTableUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildIdUri(String _Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_ID)
                    .appendPath(_Id).build();
        }

        public static Uri buildApiIdUri(String API_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_AID)
                    .appendPath(API_Id).build();
        }

        // Build URI based on UPC
        public static Uri buildUPCUri(String UPC){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_UPC)
                    .appendPath(UPC).build();
        }

        // Build Uri for all owned Items
        public static Uri buildUriAll(){
            return CONTENT_URI;
        }

        public static Uri buildSearchUri(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_SEARCH).build();
        }

        public static String getIdFromUri(Uri uri){return uri.getLastPathSegment();}

        public static String getApiIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}
    }


}
