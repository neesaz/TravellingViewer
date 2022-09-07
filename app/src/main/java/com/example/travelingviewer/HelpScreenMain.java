/*
 *
 * @author:  Nees Abusaada  and Rachel scintett
 * @file : HelpScreenMain.java
 * @assignment: Final Programming Assignment- Traveling Viewer
 * @course: CSC 317; Spring 202
 * @Description : This program is specified for the help button
 * that appears in the corner of the main trip screen for the trip
 * list.This screen will help the user to know more about the applications,
 * the features of the app, and how to use it properly. It has an image
 * of the main trip menu screen to learn how to understand and work it.
 * This program is created from the main trip screen fragment and
 * initialized as an intent.
 */
package com.example.travelingviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class HelpScreenMain extends AppCompatActivity {
    /**
     * This is the onCreate method which is the creation of this activity.
     * Here where all the methods gets called for the once in the activity.
     * It contains the setContentView for the layout.activity_help_screen.
     * It sets the image view to the specific image for the main trip
     * list screen to demonstrate how to work the screen.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen_main);
        ImageView imageView = findViewById(R.id.imageHelp);
        imageView.setImageResource(R.drawable.tripscreen);
    }
}
