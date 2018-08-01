package com.enrandomlabs.jasensanders.v1.shopenator.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ShopenatorProvider extends ContentProvider {

    // The URI Matcher used by this content provider
    private static final UriMatcher mUriMatcher = buildUriMatcher();

    // SQLite open helper for this provider
    private ShopenatorDBHelper mOpenHelper;

    public static final String ACTION_DATA_UPDATED =
            "com.enrandomlabs.jasensanders.v1.shopenator.ACTION_DATA_UPDATED";



    //For selecting all ITEM LIST items and Deleting/Inserting/Updating all
    static final int ITEM_LIST_ALL = 200;
    static final int ITEM_BY_ID = 201;
    //For Selecting one ITEM in list  with possibly multiple items by API ID code
    static final int ITEM_BY_AID = 202;
    //For Selecting one ITEM by upc code in list
    static final int ITEM_BY_UPC = 203;

    static final int SEARCH_ALL = 3500;

    private static final SQLiteQueryBuilder mQueryBuilder;

    static{
        // Set the tables
        mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(DataContract.ItemEntry.TABLE_NAME);
    }

    // Projections
    private static final String[] mProjection = DataContract.ITEM_COLUMNS;

    // Selections
    private static final String mSqlIdSelection = DataContract.ItemEntry.TABLE_NAME +
            "." + DataContract.ItemEntry._ID + " = ? ";

    private static final String mApiIdSelection = DataContract.ItemEntry.TABLE_NAME +
            "." + DataContract.ItemEntry.COLUMN_API_TYPE_ID + " = ? ";

    private static final String mUpcItemSelection = DataContract.ItemEntry.TABLE_NAME +
            "." + DataContract.ItemEntry.COLUMN_UPC + " = ? ";

    private static final String mItemsAll = null;

    // Search Selections
    private static final String mSearchSelection = DataContract.ItemEntry.COLUMN_TITLE + DataContract.SEARCH_SEG +
            DataContract.ItemEntry.COLUMN_STORE + DataContract.SEARCH_SEG + DataContract.ItemEntry.COLUMN_NOTES + DataContract.SEARCH_SEG +
            DataContract.ItemEntry.COLUMN_DESCRIPTION + DataContract.SEARCH_SEG + DataContract.ItemEntry.COLUMN_R_DATE + DataContract.SEARCH_END_SEG;

    // Search Selection Args
    private String[] searchSelectionAgrsBuilder(String query){
        return new String[]{query, query, query, query, query};
    }

    // Known Queries
    private Cursor getItemById(Uri uri, String[] projection, String sortOrder) {
        String Id = DataContract.ItemEntry.getIdFromUri(uri);

        //By which Column?
        String selection = mSqlIdSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{Id};

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getItemByAPIId(Uri uri, String[] projection, String sortOrder) {
        String Id = DataContract.ItemEntry.getApiIdFromUri(uri);

        //By which Column?
        String selection = mApiIdSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{Id};

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getItemByUPCId(Uri uri, String[] projection, String sortOrder) {
        String Id = DataContract.ItemEntry.getUpcFromUri(uri);

        //By which Column?
        String selection = mUpcItemSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{Id};

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAll(Uri uri, String[] projection, String sortOrder) {

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                mItemsAll,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor searchAll(Uri uri, String[] projection, String sortOrder){
        String q = uri.getLastPathSegment();
        final String query = "%"+q+"%";
        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                mProjection,
                mSearchSelection,
                searchSelectionAgrsBuilder(query),
                null,
                null,
                sortOrder);
    }


    // URI matcher for states above
    static UriMatcher buildUriMatcher() {
        // Setup the Matcher
        final UriMatcher fURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // Add Match possibilities for the states defined above
        fURIMatcher.addURI(authority,DataContract.PATH_ITEM_LIST, ITEM_LIST_ALL );
        fURIMatcher.addURI(authority,DataContract.PATH_ITEMS_AID + "/#",ITEM_BY_AID );
        fURIMatcher.addURI(authority,DataContract.PATH_ITEMS_ID + "/#",ITEM_BY_ID );
        fURIMatcher.addURI(authority, DataContract.PATH_ITEMS_UPC + "/#",ITEM_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_SEARCH_ALL + "/*", SEARCH_ALL);

        return fURIMatcher;
    }



    @Override
    public boolean onCreate() {
        mOpenHelper = new ShopenatorDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final int match = mUriMatcher.match(uri);

        Cursor result;

        switch (match) {

            case ITEM_BY_ID:
                result = getItemById(uri, projection, sortOrder);
                break;
            case ITEM_BY_AID:
                result = getItemByAPIId(uri, projection, sortOrder);
                break;
            case ITEM_BY_UPC:
                result = getItemByUPCId(uri, projection, sortOrder);
                break;
            case ITEM_LIST_ALL:
                result = getAll(uri, projection, sortOrder);
                break;
            case SEARCH_ALL:
                result = searchAll(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = mUriMatcher.match(uri);

        String result;

        switch (match) {

            case ITEM_BY_ID:
                result = DataContract.ItemEntry.CONTENT_ITEM_TYPE;
                break;
            case ITEM_BY_AID:
                result = DataContract.ItemEntry.CONTENT_ITEM_TYPE;
                break;
            case ITEM_BY_UPC:
                result = DataContract.ItemEntry.CONTENT_ITEM_TYPE;
                break;
            case ITEM_LIST_ALL:
                result = DataContract.ItemEntry.CONTENT_TYPE;
                break;
            case SEARCH_ALL:
                result = DataContract.SEARCH_CONTENT_TYPE;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return result;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case ITEM_LIST_ALL: {
                long _id = db.insert(DataContract.ItemEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.ItemEntry.buildTableUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri+ String.valueOf(_id));
                break;

            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = mUriMatcher.match(uri);

        switch (match) {

            case ITEM_LIST_ALL: {
                rowsDeleted = db.delete(DataContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;
        final int match = mUriMatcher.match(uri);

        switch (match) {

            case ITEM_LIST_ALL: {
                rowsUpdated = db.update(DataContract.ItemEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


}
