package com.gmail.senokt16.unify;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.firebase.ui.FirebaseUI;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

/**
 * Created by senok on 25/3/2017.
 */

public class Unify extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
