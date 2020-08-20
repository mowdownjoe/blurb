package com.mowdowndevelopments.blurb.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;

import timber.log.Timber;

@Database(entities = {Feed.class, Story.class}, version = 1, exportSchema = false)
public abstract class BlurbDb extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "blurb_reader";
    private static BlurbDb instance;

    public abstract BlurbDao blurbDao();

    public static BlurbDb getInstance(Context context) {
        if (instance == null){
            synchronized (LOCK){
                Timber.d("Creating new database instance…");
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        BlurbDb.class, DB_NAME).build();
            }
        }
        return instance;
    }
}
