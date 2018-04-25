package com.jastley.warmindfordestiny2.Characters.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jamie1192 on 24/4/18.
 */

public class InventoryItemModel implements Parcelable {

    private String itemHash;
    private String itemInstanceId;
    private String bucketHash;
    //TODO: state (is item locked?)

    //item instance data
    private boolean isEquipped;
    private boolean canEquip;
    private String primaryStatValue;

    //Manifest data
    private String classType;
    private String itemName;
    private String itemIcon;


    public InventoryItemModel(String itemHash, String itemInstanceId, String bucketHash, String primaryStatValue) {
        this.itemHash = itemHash;
        this.itemInstanceId = itemInstanceId;
        this.bucketHash = bucketHash;
        this.primaryStatValue = primaryStatValue;
    }

    public InventoryItemModel() {
    }

    public String getItemHash() {
        return itemHash;
    }

    public void setItemHash(String itemHash) {
        this.itemHash = itemHash;
    }

    public String getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(String itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public String getBucketHash() {
        return bucketHash;
    }

    public void setBucketHash(String bucketHash) {
        this.bucketHash = bucketHash;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(boolean equipped) {
        isEquipped = equipped;
    }

    public boolean isCanEquip() {
        return canEquip;
    }

    public void setCanEquip(boolean canEquip) {
        this.canEquip = canEquip;
    }

    public String getPrimaryStatValue() {
        return primaryStatValue;
    }

    public void setPrimaryStatValue(String primaryStatValue) {
        this.primaryStatValue = primaryStatValue;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemIcon() {
        return itemIcon;
    }

    public void setItemIcon(String itemIcon) {
        this.itemIcon = itemIcon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
