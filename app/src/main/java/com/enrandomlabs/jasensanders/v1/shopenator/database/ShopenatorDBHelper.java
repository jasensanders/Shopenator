package com.enrandomlabs.jasensanders.v1.shopenator.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.enrandomlabs.jasensanders.v1.shopenator.database.DataContract.ItemEntry;

public class ShopenatorDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION =1;

    private static final String DATABASE_NAME = "shopenatorDB.db";
    private static final String BACKUP_DB_NAME = "ShopenatorBackupDB.db";

    public ShopenatorDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_ITEMLIST_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                ItemEntry._ID + " INTEGER PRIMARY KEY," +
                ItemEntry.COLUMN_API_TYPE_ID + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_PACKAGE_ART + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_BARCODE_IMG + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_SEVEN + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_R_DATE + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_FOURTEEN + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_FIFTEEN + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_SIXTEEN + " TEXT NOT NULL, " +
                "UNIQUE (" + ItemEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";


        db.execSQL(SQL_CREATE_ITEMLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String getBackupDbName(){
        return BACKUP_DB_NAME;
    }

    public static String getDbName(){
        return DATABASE_NAME;
    }

    public void dropTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);

    }

    public boolean isEmpty(SQLiteDatabase db, final String tablename, final String[] columns){

        Cursor c = db.query(tablename, columns, null, null, null, null, BaseColumns._ID + " ASC", null);
        if(c != null && c.moveToFirst()) {
            c.close();
            return false;
        }
        return true;

    }
}
