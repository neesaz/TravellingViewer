/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: MainActivity.java
 * @assignment: Final Programming Assignment- Traveling Viewer
 * @course: CSC 317; Spring 2022
 * @description: The purpose of this app is to  implement a traveling log app.
 * This app is specific to users who want to make traveling their hobby and
 * create a fun way to save travel memories. In order to do that, the user
 * will be able to keep track of places they have been around the world,
 * add pictures of those places to save memories, add contacts to the app
 * to share stuff with other people, and make reflective notes. The application
 * will have multiple screens, buttons, texts, and images, all of which follow
 * a specific style. This program does this by incorporating the use of fragments
 * and activities. The GoogleMaps API is used to display the locations the user
 * pins as well as the locations of the photos and notes taken. In addition,
 * the Wikipedia API is used to display a webpage if the user clicks on a
 * location in the list. The data is contained in ArrayLists which makes
 * it applicable to store in text files (.txt) using internal storage. Content
 * providers are used in order to gather the contacts data for sharing. Implicit
 * intents are used for sending emails, SMS messages and taking photos. In this
 * MainActivity.java there is only the onCreate() method because most of the
 * functionality in this application is done in the fragments. This MainActivity
 * is only responsible for being a container and creating the first fragment
 * called the AnimationFragment.
 */
package com.example.travelingviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    /**
     * The purpose of this method is to be the place where the activity starts
     * for the java program. The onCreate() method is supposed to initialize
     * the activity. The setContentView is set to R.layout.activity_main. In
     * this onCreate() method the animation fragment is created which shows
     * the logo moving and the start button to function as an app loading
     * screen. This method does this by using the getSupportFragmentManager()
     * and the beginTransaction() method in order to create a transaction so
     * the fragment could begin.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnimationFragment animator = new AnimationFragment();
        animator.setContainerActivity(this);
        getSupportFragmentManager().beginTransaction().add(R.id.innerLayout,
                animator).addToBackStack(null).commit();
    }
}