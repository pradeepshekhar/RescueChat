package com.example.prade.rescuechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by prade on 3/7/2017.
 */

public class DeviceDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ScanDevices.db";
    private static final String TABLE_NAME1 = "FirstHopDevices";
    private static final String TABLE_NAME2 = "SecondHopDevices";

    private static final String COLUMN_ID1 = "id";
    private static final String COLUMN_DEVICENAME1 = "FirstHopDeviceName";
    public static final String COLUMN_DEVICEADDRESS1 = "FirstHopDeviceAddress";
    public static final String COLUMN_DEVICERSSI1 = "FirstHopDeviceRSSI";

    private static final String COLUMN_ID2 = "id";
    private static final String COLUMN_DEVICENAME2 = "SecondHopDeviceName";
    public static final String COLUMN_DEVICEADDRESS2 = "SecondHopDeviceAddress";
    private static final String COLUMN_DEVICENAME3 = "InterDeviceName";
    public static final String COLUMN_DEVICEADDRESS3 = "InterDeviceAddress";
    public static final String COLUMN_DEVICERSSI2 = "InterDeviceRSSI";

    public DeviceDBHandler(Context context){//, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME1 + " ( " +
                COLUMN_ID1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME1 + " TEXT, "+
                COLUMN_DEVICEADDRESS1 + " TEXT, "+
                COLUMN_DEVICERSSI1 +" INTEGER " +
                ");";

        String query2 = "CREATE TABLE " + TABLE_NAME2 + " ( " +
                COLUMN_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME2 + " TEXT, "+
                COLUMN_DEVICEADDRESS2 + " TEXT, "+
                COLUMN_DEVICENAME3 + " TEXT, "+
                COLUMN_DEVICEADDRESS3 + " TEXT, "+
                COLUMN_DEVICERSSI2 +" INTEGER " +
                ");";
        db.execSQL(query);
        db.execSQL(query2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME1);
        onCreate(db);
    }

    //Add a new row
    public void addDevice1(Devices devices){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICENAME1, devices.get_deviceName());
        values.put(COLUMN_DEVICEADDRESS1, devices.get_deviceAddress());
        values.put(COLUMN_DEVICERSSI1, devices.get_deviceRSSI());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME1, null, values);
        db.close();
    }

    public void addDevice2(Devices devices, String interName, String interAddress){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICENAME2, devices.get_deviceName());
        values.put(COLUMN_DEVICEADDRESS2, devices.get_deviceAddress());
        values.put(COLUMN_DEVICENAME3,interName);
        values.put(COLUMN_DEVICEADDRESS3,interAddress);
        values.put(COLUMN_DEVICERSSI2, devices.get_deviceRSSI());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME2, null, values);
        db.close();
    }

    //Delete a product from the table
    public void deleteDevice1(String address){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME1 + " WHERE " + COLUMN_DEVICEADDRESS1 + " = \"" + address + "\";");
    }

    public void deleteDevice2(String address){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME2 + " WHERE " + COLUMN_DEVICEADDRESS2 + " = \"" + address + "\";");
    }

    public void deletetable1(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
    }

    public void deletetable2(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
    }

    public void createtable1(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "CREATE TABLE " + TABLE_NAME1 + " ( " +
                COLUMN_ID1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME1 + " TEXT, "+
                COLUMN_DEVICEADDRESS1 + " TEXT, "+
                COLUMN_DEVICERSSI1 +" INTEGER " +
                ");";
        db.execSQL(query);
    }


    public void createtable2() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query2 = "CREATE TABLE " + TABLE_NAME2 + " ( " +
                COLUMN_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME2 + " TEXT, "+
                COLUMN_DEVICEADDRESS2 + " TEXT, "+
                COLUMN_DEVICENAME3 + " TEXT, "+
                COLUMN_DEVICEADDRESS3 + " TEXT, "+
                COLUMN_DEVICERSSI2 +" INTEGER " +
                ");";
        db.execSQL(query2);
    }

    //get specific row from Table1
    public String [] deviceAt1(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME1,new String[]{COLUMN_DEVICENAME1, COLUMN_DEVICEADDRESS1, COLUMN_DEVICERSSI1},COLUMN_ID1+"=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        String deviceIs [] = {cursor.getString(0), cursor.getString(1), cursor.getString(2)};
        cursor.close();
        return deviceIs;
    }

    //get specific row from table2
    public String [] deviceAt2(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME2,new String[]{COLUMN_DEVICENAME2, COLUMN_DEVICEADDRESS2, COLUMN_DEVICENAME3, COLUMN_DEVICEADDRESS3, COLUMN_DEVICERSSI2},COLUMN_ID1+"=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        String deviceIs [] = {cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4)};
        cursor.close();
        return deviceIs;
    }

    public int getCount1(){
        SQLiteDatabase db = this.getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(db,TABLE_NAME1);
        return count;
    }

    public int getCount2(){
        SQLiteDatabase db = this.getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(db,TABLE_NAME2);
        return count;
    }



    public int isExits1(String address){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_NAME1+" WHERE "+COLUMN_DEVICEADDRESS1+" = \""+address+"\"";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            return Integer.parseInt(cursor.getString(3));
        }
        return 1000;
    }

    public int isExits2(String address){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_NAME2+" WHERE "+COLUMN_DEVICEADDRESS2+" = \""+address+"\"";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            return Integer.parseInt(cursor.getString(5));
        }
        return 1000;
    }

    public void updateRow2(Devices devices, String address, String interName, String interAddress){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "UPDATE " +TABLE_NAME2+" SET "
                +COLUMN_DEVICENAME2+"=" + "'"+devices.get_deviceName()+ "'"+", "
                +COLUMN_DEVICEADDRESS2+"=" + "'" +devices.get_deviceAddress()+"'"+", "
                +COLUMN_DEVICENAME3+"=" +"'"+ interName+"'"+", "
                +COLUMN_DEVICEADDRESS3+"=" +"'"+ interAddress+"'"+", "
                +COLUMN_DEVICERSSI2+" ="+"'"+ devices.get_deviceRSSI()+"'"+" "
                +" WHERE "+COLUMN_DEVICEADDRESS2 +" = \""+address+"\";";
        db.execSQL(query);
        db.close();
    }

    public int rssi1(String address){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_NAME1+" WHERE "+COLUMN_DEVICEADDRESS1+" = \""+address+"\"";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            return Integer.parseInt(cursor.getString(3));
        }
        return 50;
    }
    public String addressInter (String address){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " +TABLE_NAME2+" WHERE "+COLUMN_DEVICEADDRESS2+" = \""+address+"\"";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }

        return cursor.getString(4);
    }
}

