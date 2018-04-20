package com.jastley.warmindfordestiny2.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.jastley.warmindfordestiny2.database.models.Account;
import com.jastley.warmindfordestiny2.database.models.Collectables;

/**
 * Created by jamie on 9/4/18.
 */

@Database(entities = {Account.class, Collectables.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract AccountDAO getAccountDAO();
    public abstract CollectablesDAO getCollectablesDAO();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room
                    .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "bungieAccount")
//                    .inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class)
                    .build();
        }

        return INSTANCE;
    }
}
