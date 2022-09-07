/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: AnimationFragment.java
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
 * AnimationFragment.java is where the app logo image is animated programmatically.
 * The next fragment is also called when the start image is clicked.
 */
package com.example.travelingviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class AnimationFragment extends Fragment {
    public   View view;
    private AppCompatActivity containerActivity;

    /**
     * The purpose of this method is to create a context of an activity within
     * the fragment to keep track of the container activity from main. This
     * makes the process of completing certain tasks a lot easier when there is
     * a context of the main activity.
     * @param containerActivity = An activity that represents the container
     * activity which is main.
     */
    public void setContainerActivity(AppCompatActivity containerActivity){
        this.containerActivity = containerActivity;
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the startAndroidMethod is call and the sendMessage() method
     * in order to animate the screen and start the next fragment.
     * @param inflater = An inflater used to inflate the fragment.
     * @param container = The view container from main activity that can
     *  have elements added to it or replaced.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     * @return view = A view returned so that the fragment can
     * be correctly placed onto the container.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_animation, container, false);
        startAndroidAnimation();
        sendMessage();
        return view;
    }

    /**
     * The purpose of this method is to animate the imageView3 which is the
     * app travel logo programmatically. This method does this by getting
     * the ImageView dimensions and the PropertyValuesHolder method to
     * translate the image around teh screen. A duration is also set
     * so that the image moves during a certain period of time to
     * cause the diagonal motion.
     */
    private void startAndroidAnimation() {
        ImageView iv = view.findViewById(R.id.imageView3);
        iv.setAlpha(1.0F);
        int width = getWidthInPixels();
        int height = getHeightInPixels();

        iv.measure(0,0);
        PropertyValuesHolder pX = PropertyValuesHolder.ofFloat("translationX"
                ,width- iv.getLayoutParams().width);
        PropertyValuesHolder pY = PropertyValuesHolder.ofFloat("translationY"
                ,height-iv.getMeasuredHeight()-200);
        PropertyValuesHolder pA = PropertyValuesHolder.ofFloat("alpha"
                ,1.0F);
        ObjectAnimator animatorXY = ObjectAnimator.ofPropertyValuesHolder(iv,pX,pY,pA);
        animatorXY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                PropertyValuesHolder pX = PropertyValuesHolder.ofFloat("translationX",0);
                PropertyValuesHolder pY = PropertyValuesHolder.ofFloat("translationY",0);
                final  ObjectAnimator other = ObjectAnimator.ofPropertyValuesHolder(iv,pX,pY);
                other.setDuration(2000).start();
            }
        });
        animatorXY.setDuration(5000).start();
    }

    /**
     * The purpose of this method is to get the display height and return
     * that number as an integer. This method does this by using the
     * DisplayMetrics and the getMetrics() method along with height Pixels.
     * @return int number = An integer that represents the height.
     */
    protected int getHeightInPixels() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        containerActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * The purpose of this method is to get the display width and return
     * that number as an integer. This method does this by using the
     * DisplayMetrics and the getMetrics() method along with width Pixels.
     * @return int number = An integer that represents the width.
     */
    protected int getWidthInPixels() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        containerActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * The purpose of this method is to call the next MainTripScreen Fragment is
     * the imageView5 start button drawing is clicked. This method does this
     * by using setOnClickListener() and then initiating the fragment with
     * the correct methods.
     */
    public void sendMessage() {
        ImageView getStarted = view.findViewById(R.id.imageView5);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainTripScreenFragment screenFragment = new MainTripScreenFragment();
                screenFragment.setContainerActivity(containerActivity);
                //Creates the transaction
                getFragmentManager().beginTransaction().
                        replace(R.id.innerLayout,screenFragment).
                        addToBackStack(null).commit();
            }
        });

    }
}