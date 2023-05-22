package com.example.driveclassifier

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context): SQLiteOpenHelper(context, "LOCDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL("CREATE TABLE location(ID INTEGER PRIMARY KEY AUTOINCREMENT,LATITUDE REAL, LONGITUDE REAL, SPEED REAL, DATE TEXT)")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}