package com.jastley.warmindfordestiny2.Characters.interfaces;

public interface SuccessListener {
    void inProgress();
    void onSuccess(int position, boolean wasSuccessful, String message);
}
