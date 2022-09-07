/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: OptionsScreenFragment.java
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
 * OptionsScreenFragment.java allows the user to choose which screen to continue
 * to either the Locations, Memories, Notes, or Help Screen. So this program
 * functions as a menu and keeps track of what buttons have been clicked to
 * initiate the next fragment.
 */
package com.example.travelingviewer;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;

public class OptionsScreenFragment extends Fragment {

    private String tripSelected;
    private AppCompatActivity containerActivity;
    private View inflatedView;
    public double tripLng;
    public double tripLat;

    /**
     * This private class is an async task that functions as a
     * helper task in order to call the methods to run in the
     * background and not take up too much space on the
     * main UI thread.
     */
    private class HelperTask extends AsyncTask<String, Integer, String> {
        /**
         * The purpose of this method is to call the helper methods
         * used to start the different fragments. This is done to
         * not have a lot of elements running on the main UI thread.
         * @param strings = A string that could be used in the task.
         * @return string = A string that could be used in onPostExecute.
         */
        @Override
        protected String doInBackground(String... strings) {
            loadLocationFragment();
            loadMemoriesFragment();
            loadNotesFragment();
            loadHelpIntent();
            return null;
        }
    }

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
     * This method is responsible for gathering the information
     * that was saved into the bundle in the previous fragment.
     * This method does this by using the getArguments().getString()
     * method to gather the key value pairs saved. This method
     * saves those as class variables to use.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripSelected= getArguments().getString("tripClicked");
        }
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the options screen helper task is created and executed in order
     * to call all of the helper methods in the background.
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
        inflatedView = inflater.inflate(R.layout.fragment_options_screen, container, false);
        HelperTask helperTask = new HelperTask();
        helperTask.execute();
        return inflatedView;
    }

    /**
     * The purpose of this method is to start the next location fragment
     * if the user clicks on the location button. This method first
     * gets the longitude and latitude of overall trip location to
     * have a pin at the center of the city. Then this method checks
     * if the button was clicked with setOnClickListener(). Then
     * the LocationFragment is created and passed in arguments and
     * instantiated using the proper methods like getFragmentMangaer(),
     * etc.
     */
    public void loadLocationFragment(){
        Geocoder tripLocation = new Geocoder(containerActivity);
        ArrayList<Address> addresses = null;
        try {
            addresses = (ArrayList<Address>) tripLocation.getFromLocationName(tripSelected, 5);
            Address check =  addresses.get(0);
            tripLng = check.getLongitude();
            tripLat = check.getLatitude();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button locButton = inflatedView.findViewById(R.id.locationButton);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationFragment locationFragment = new LocationFragment();
                locationFragment.setContainerActivity(containerActivity);
                Bundle args = new Bundle();
                args.putString("tripClicked", tripSelected.split(", ")[0]);
                args.putString("tripYear", tripSelected.split(", ")[1]);
                args.putDouble("tripLng",tripLng);
                args.putDouble("tripLat",tripLat);
                args.putString("tripName", tripSelected);
                locationFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.innerLayout, locationFragment).
                        addToBackStack(null).commit();
            }
        });
    }

    /**
     * The purpose of this method is to load the memories fragment
     * if the memories button is clicked. This method does this by
     * using the setOnClickListener() to check if the button is
     * clicked. If so then the Memories Fragment is created and
     * passed in arguments. The Memories Fragment is instantiated
     * with the correct methods using getFragmentManager(),etc.
     */
    public void loadMemoriesFragment(){
        Button memButton = inflatedView.findViewById(R.id.photosButton);
        memButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MemoriesFragment memoriesFragment = new MemoriesFragment();
                memoriesFragment.setContainerActivity(containerActivity);
                Bundle args = new Bundle();
                args.putString("tripClicked", tripSelected.split(", ")[0]);
                args.putString("tripYear", tripSelected.split(", ")[1]);
                args.putDouble("tripLng",tripLng);
                args.putDouble("tripLat",tripLat);
                memoriesFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                        memoriesFragment).addToBackStack(null).commit();

            }
        });
    }

    /**
     * The purpose of this method is to load the notes fragment
     * if the notes button is clicked. This method does this by
     * using the setOnClickListener() to check if the button is
     * clicked. If so then the Notes Fragment is created and
     * passed in arguments. The Notes Fragment is instantiated
     * with the correct methods using getFragmentManager(),etc.
     */
    public void loadNotesFragment(){
        Button notesButton = inflatedView.findViewById(R.id.notesButton);
        notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotesFragment notesFragment = new NotesFragment();
                notesFragment.setContainerActivity(containerActivity);
                Bundle args = new Bundle();
                args.putString("tripClicked", tripSelected.split(", ")[0]);
                args.putString("tripYear", tripSelected.split(", ")[1]);
                args.putDouble("tripLng",tripLng);
                args.putDouble("tripLat",tripLat);
                notesFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                        notesFragment).addToBackStack(null).commit();
            }
        });
    }

    /**
     * The purpose of this method is to load the Help Activity
     * Screen if the help button is clicked. This method does this
     * by using the setOnClickListener() to check if the button is
     * clicked. If so then the Help Activity screen is created and
     * loaded using an intent and the startActivity() method.
     */
    public void loadHelpIntent(){
        Button locationButton = inflatedView.findViewById(R.id.helpButton2);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HelpActivityScreen.class);
                startActivity(intent);

            }
        });
    }

}