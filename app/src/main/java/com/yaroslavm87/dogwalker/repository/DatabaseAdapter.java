package com.yaroslavm87.dogwalker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yaroslavm87.dogwalker.model.Dog;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter implements Repository<List<Dog>> {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private final String LOG_TAG = "myLogs";


    public DatabaseAdapter(Context context){

        Log.d(LOG_TAG, "DatabaseAdapter() constructor call");

        Log.d(LOG_TAG, "DatabaseHelper() constructor call from DatabaseAdapter");
        dbHelper = new DatabaseHelper(context.getApplicationContext(), 1);

        contentValues = new ContentValues();
    }

    @Override
    public ArrayList<Dog> read() {

        Log.d(LOG_TAG, "DatabaseAdapter.read() call");

        databaseOpen();

        ArrayList<Dog> dogList = new ArrayList<>();

        Cursor cursor = getAllEntries();

        while (cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
//            int imageResId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RESOURCE_ID));
//            int lastTimeWalk = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_TIME_WALK));
            Dog dog = new Dog(name);
            dog.setId(id);
            dogList.add(dog);

            //dogList.add(new Dog(id, name, imageResId, lastTimeWalk));
        }

        cursor.close();
        databaseClose();

        return dogList;
    }

    @Override
    public void add(Dog dog) {

        contentValues.clear();
        contentValues.put(DatabaseHelper.COLUMN_NAME, dog.getName());
        contentValues.put(DatabaseHelper.COLUMN_IMAGE_RESOURCE_ID, dog.getImageResId());
        contentValues.put(DatabaseHelper.COLUMN_LAST_TIME_WALK, dog.getLastTimeWalk());

        database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    @Override
    public void update(Dog dog) {
        String whereClause = dbHelper.getStringBuilder()
                .append(DatabaseHelper.COLUMN_ID)
                .append("=")
                .append(dog.getId())
                .toString();

        contentValues.clear();
        contentValues.put(DatabaseHelper.COLUMN_NAME, dog.getName());
        contentValues.put(DatabaseHelper.COLUMN_IMAGE_RESOURCE_ID, dog.getImageResId());
        contentValues.put(DatabaseHelper.COLUMN_LAST_TIME_WALK, dog.getLastTimeWalk());

        database.update(DatabaseHelper.TABLE_NAME, contentValues, whereClause, null);
    }

    @Override
    public void delete(Dog dog) {

        String whereClause = "_id = ?";

        String[] whereArgs = new String[] {String.valueOf(dog.getId())};

        database.delete(DatabaseHelper.TABLE_NAME, whereClause, whereArgs);
    }

        public long getCount(){
        return DatabaseUtils.queryNumEntries(database, DatabaseHelper.TABLE_NAME);
    }

    private void databaseOpen(){

        Log.d(LOG_TAG, "DatabaseAdapter.databaseOpen() call");

        database = dbHelper.getWritableDatabase();
    }

    private void databaseClose(){

        Log.d(LOG_TAG, "DatabaseAdapter.databaseClose() call");

        dbHelper.close();

    }

    private Cursor getAllEntries(){

        Log.d(LOG_TAG, "DatabaseAdapter.getAllEntries() call");

        String[] columns = new String[] {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_NAME
//                DatabaseHelper.COLUMN_IMAGE_RESOURCE_ID,
//                DatabaseHelper.COLUMN_LAST_TIME_WALK
        };

        return  database.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );
    }
}
