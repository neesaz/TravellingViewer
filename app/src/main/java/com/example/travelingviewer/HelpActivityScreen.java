/*
 *
 * @author:  Nees Abusaada  and Rachel scintett
 * @file : HelpActivityScreen.java
 * @assignment: Final Programming Assignment- Traveling Viewer
 * @course: CSC 317; Spring 202
 * @Description : This program is specified for the help button
 * that appears in the main options screen of the application.
 * This screen will help the user to know more about the applications,
 * the features of the app, and how to use it properly. It has an image
 * of each screen of the application and two buttons to go the next image
 * and return back to the other image. This program is created from the
 * option screen fragment and initialized as an intent.
 */

package com.example.travelingviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
public class HelpActivityScreen extends AppCompatActivity {
    static int currentImage = 0;
    Button button;
    Button buttonSecond;

    int[] images = {R.drawable.firstscreen, R.drawable.tripscreen, R.drawable.mainscreen,
            R.drawable.locationmangerscreen,R.drawable.addedlocation,R.drawable.notes,
            R.drawable.imagescreen, R.drawable.imagestwoscreen};

    /**
     * This is the onCreate method which is the creation of this activity.
     * Here where all the methods gets called for the once in the activity.
     * It contains the setContentView for the layout.activity_help_screen.
     * It also calls the method that controls the buttons.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);
        controlButtons();
    }

    /**
     * This method's purpose to set the buttons that moves to the next
     * image and to the back button which goes to the previous image.
     * It uses ImageView to get the image by id and Button to the
     * buttons' id. Also, using setOnClickListener for each button
     * that displays task when the user clicks on it as well as onClick
     * method. Using if statements to set the current image and increment
     * or decrement depends if it is the next or back button when the user
     * clicks on it. Finally, it sets the image that should display in the
     * image.
     */
    public void controlButtons() {
        ImageView imageView = findViewById(R.id.imagemain);
        button = findViewById(R.id.next);
        buttonSecond = findViewById(R.id.back);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImage >= 7 ){
                    currentImage =0;
                }
                else {
                    currentImage++;
                }
                imageView.setImageResource(images[currentImage]);

            }
        });

        buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImage <= 0 ){
                    currentImage =0;

                }
                else if (currentImage > images.length ){
                    currentImage =0;
                }
                else {
                    currentImage--;
                }
                imageView.setImageResource(images[currentImage]);

            }
        });
    }

}