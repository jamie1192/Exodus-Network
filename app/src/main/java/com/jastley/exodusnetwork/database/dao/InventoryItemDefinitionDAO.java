package com.jastley.exodusnetwork.database.dao;


import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jastley.exodusnetwork.database.models.DestinyInventoryItemDefinition;

import java.util.List;

import io.reactivex.Maybe;

/**
 * Created by jamie on 9/4/18.
 */

@Dao
public interface InventoryItemDefinitionDAO {

    @Query("SELECT * FROM DestinyInventoryItemDefinition")
    Maybe<List<DestinyInventoryItemDefinition>> getAllCollectables();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DestinyInventoryItemDefinition destinyInventoryItemDefinition);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DestinyInventoryItemDefinition> collectables);

    @Query("SELECT * FROM DestinyInventoryItemDefinition WHERE id = :itemKey")
    Maybe<DestinyInventoryItemDefinition> getItemByKey(String itemKey);

    @Query("SELECT * FROM DestinyInventoryItemDefinition WHERE id IN (:itemKey)")
    Maybe<List<DestinyInventoryItemDefinition>> getItemsListByKey(List<String> itemKey);

    @Update
    void update(DestinyInventoryItemDefinition destinyInventoryItemDefinitionRow);

    //Paging
    @Query("SELECT * FROM DestinyInventoryItemDefinition")
    DataSource.Factory<Integer, DestinyInventoryItemDefinition> getPagedInventoryItems();

}
