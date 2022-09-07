/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: LocationFragment.java
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
 * NotesFragment.java  is where the user can add notes or reflections to the
 * list view of notes. The user can add or delete the notes, share the notes
 * via sms or email, and view the location of where the note was taken.
 */
package com.example.travelingviewer;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;

public class NotesFragment extends Fragment {

    private View view;
    private Activity containerActivity;
    public ArrayList<String> notesList = new ArrayList<>();
    public ArrayList<Double> notesLong = new ArrayList<>();
    public ArrayList<Double> notesLat = new ArrayList<>();
    private ListView notesListView;
    private String saveDate;
    private String noteEntered;
    private double cityLat;
    private double cityLng;
    private String cityName;
    private String cityYear;
    private double currLat;
    private double currLng;
    private Boolean deleteClicked = false;

    /**
     * This private async task class functions as a helper to call the methods
     * to not have a lot running on the Main UI thread.
     */
    private class CallHelperTask extends AsyncTask<String, Integer, String> {

        /**
         * The purpose of this method is to call all of the methods in the background
         * thread. This method does this by calling all of the helper methods in
         * the background. This shows how this async task is more of a helper
         * than an actual task being completed.
         * @param strings = A string that is passed into the async task.
         * @return string = A string that will be used in onPostExecute.
         */
        @Override
        protected String doInBackground(String... strings) {
            checkAddButton();
            checkDeleteButton();
            checkShareButton();
            checkMapButton();
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
    public void setContainerActivity(Activity containerActivity){
        this.containerActivity = containerActivity;
    }

    /**
     * This method is responsible for gathering the information
     * that was saved into the bundle in the previous fragment.
     * This method does this by using the getArguments().getString()
     * method to gather the key value pairs saved. This method
     * saves those as class variables to use. The uploading
     * data methods are also called here so that it loads data
     * before the other lifecycles are called.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityLat = getArguments().getDouble("tripLat");
            cityLng = getArguments().getDouble("tripLng");
            cityName = getArguments().getString("tripClicked");
            cityYear = getArguments().getString("tripYear");
        }
        uploadFileData();
        uploadFileData2();
        uploadFileData3();
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the location helper task is created and executed in order
     * to call all of the helper methods in the background. In
     * addition the set up locations request method is called
     * here.
     * @param inflater = An inflater used to inflate the fragment.
     * @param container = The view container from main activity that can
     *  have elements added to it or replaced.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     * @return view = A view returned so that the fragment can
     * be correctly placed onto the container.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);
        notesListView = view.findViewById(R.id.notesListView);
        saveDate = LocalDate.now().toString();
        CallHelperTask callHelperTask = new CallHelperTask();
        callHelperTask.execute();
        setupLocationRequest();
        return view;
    }

    /**
     * The purpose of this method is to add a string that represents a note
     * to a list view of notes. This method does this by checking if the
     * add button was clicked after text was entered into the edit text. This
     * is done by using a .setOnClickListener() method. If the button was
     * clicked then the note is added to the notes ArrayList as well
     * as the location the note was taken.Then the ArrayAdapter is created
     * with the ArrayList and the list view sets the adapter.
     */
    public void checkAddButton(){
        Button addButton = view.findViewById(R.id.addButton2);
        EditText editText = view.findViewById(R.id.editText2);
        addButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //Fix date to save correctly
                noteEntered = editText.getText().toString() + " - " + saveDate;
                notesList.add(noteEntered);
                notesLong.add(currLng);
                notesLat.add(currLat);
                ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                        R.layout.custom_layout_text2, R.id.textView2, notesList);
                notesListView.setAdapter(arrayAdapter);
            }
        });
    }

    /**
     * The purpose of this method is to delete the note that was
     * clicked on and remove it from the list view. This method
     * does this by checking to see if the delete button was clicked
     * and then an element in the list view was clicked by using
     * .setOnClickListener() and .setOnItemClickListener(). This
     * method also uses if statements and a boolean to keep track
     * of the correct button to ArrayList click combination. If this
     * is the case then the note and its location coordinates are
     * removed from the ArrayList. A new ArrayAdapter is created to
     * update the list view to reflect this change by setting the adapter.
     */
    public void checkDeleteButton(){
        Button deleteButton = view.findViewById(R.id.deleteButton2);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            notesList.remove(notesList.get(i));
                            notesLat.remove(notesLat.get(i));
                            notesLong.remove(notesLong.get(i));
                            ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                                    R.layout.custom_layout_text2, R.id.textView2, notesList);
                            notesListView.setAdapter(arrayAdapter);
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * The purpose of this method is to share the note that was
     * added to the list view if it was clicked on. This method
     * does this by checking if the share button was clicked
     * and then if an element of the ArrayList was clicked after.
     * This method does this by using .setOnClickListener() and
     * .setOnItemClickListener(). This method also uses if
     * statements and a boolean to keep track of the correct
     * button to ArrayList click combination. If this is the
     * case then this method creates the ContactsManagerFragment
     * to handle the sharing. This method does this by correctly
     * initializing the fragment using .getFragmentManager() etc.
     */
    public void checkShareButton(){
        Button shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            ContactsManagerFragment contactsFragment = new ContactsManagerFragment();
                            contactsFragment.setContainerActivity(containerActivity);
                            Bundle args = new Bundle();
                            args.putString("entered", notesList.get(i));
                            contactsFragment.setArguments(args);
                            getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                                    contactsFragment).addToBackStack(null).commit();
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * This method is responsible for displaying the note location on the
     * map when the user clicks on the map button then the note in the
     * list view. This method does this by checking if the share button
     * was clicked and then if an element of the ArrayList was clicked after.
     * This method does this by using .setOnClickListener() and
     * .setOnItemClickListener(). This method also uses if statements and
     * a boolean to keep track of the correct button to ArrayList click
     * combination.If this is the case then a MapsFragment is created
     * and arguments are passed into the Fragment. Then it is instantiated
     * correctly with the methods .getFragmentManager() etc.
     */
    public void checkMapButton(){
        Button shareButton = view.findViewById(R.id.mapButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            MapsFragment mapsFragment = new MapsFragment();
                            mapsFragment.setContainerActivity((AppCompatActivity) containerActivity);
                            Bundle args = new Bundle();
                            args.putStringArrayList("locationList", LocationFragment.copyLocationsList);
                            args.putDouble("cityLong", cityLng);
                            args.putDouble("cityLat", cityLat);
                            args.putString("cityName", cityName);
                            args.putDouble("noteLat", notesLat.get(i));
                            args.putDouble("noteLong", notesLong.get(i));
                            args.putString("noteName", " Note/Memory" + (i+1));
                            mapsFragment.setArguments(args);
                            getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                                    mapsFragment).addToBackStack(null).commit();
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * This method sets up the the location request that the user requires.
     * It uses the LocationListener and gets the location data with adding
     * it into ArrayList.It has multiple methods to change the status and
     * enables and disable the location. With sending the request to update
     * the location and passing in the GPS provider, time of the location,
     * distance of the location that helps it finding the correct location.
     */
    public void setupLocationRequest(){
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                currLat = location.getLatitude();
                currLng = location.getLongitude();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            @Override
            public void onProviderEnabled(String s) { }
            @Override
            public void onProviderDisabled(String s) { }
        };
        LocationManager mLocationManager = (LocationManager)
                containerActivity.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100 /*LOCATION_REFRESH_TIME*/,
                1 /*LOCATION_REFRESH_DISTANCE*/,
                mLocationListener);
    }

    /**
     * The purpose of this method is to load the ArrayAdapter and
     * set the list view to that adapter whenever the screen is
     * navigated back to in the fragment life cycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                R.layout.custom_layout_text2, R.id.textView2, notesList);
        notesListView.setAdapter(arrayAdapter);

    }

    /**
     * The purpose of this method is to read in the notesList that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the notesList is reset to the data that was read from
     * the file.
     */
    public void uploadFileData(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"Notes.txt");
        if(!saveFile.exists()){
            return;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(saveFile);
            String readLine = "";
            ArrayList<String> temp = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((readLine = bufferedReader .readLine()) != null) {
                temp.add(readLine);

            }
            if(temp.size() != 0){
                notesList = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to read in the notes latitude that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the notesLat is reset to the data that was read from
     * the file.
     */
    public void uploadFileData2(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+getString(R.string.notesLatTxt));
        if(!saveFile.exists()){
            return;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(saveFile);
            String readLine = "";
            ArrayList<Double> temp = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((readLine = bufferedReader .readLine()) != null) {
                temp.add(Double.parseDouble(readLine));

            }
            if(temp.size() != 0){
                notesLat = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to read in the notes longitude that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the notesLong is reset to the data that was read from
     * the file.
     */
    public void uploadFileData3(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+getString(R.string.notesLongTxt));
        if(!saveFile.exists()){
            return;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(saveFile);
            String readLine = "";
            ArrayList<Double> temp = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((readLine = bufferedReader .readLine()) != null) {
                temp.add(Double.parseDouble(readLine));

            }
            if(temp.size() != 0){
                notesLong = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to call the helper methods
     * that save the necessary data to be reloaded when the
     * usr navigates away and back to this screen in the
     * fragment lifecycle. This allows the locations to
     * be saved when the user leaves the app.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroy1();
        onDestroy2();
        onDestroy3();
    }

    /**
     * The purpose of this method is to be a helper for onDestroy().
     * This is where the notes list is initially saved when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    public void onDestroy1(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"Notes.txt");
        try {
            PrintWriter writer = new PrintWriter(saveFile);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFile, true));
            for(int i = 0; i < notesList.size(); i++){
                bufferedWriter.write(notesList.get(i) + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to be a helper for onDestroy().
     * This is where the notes latitude list is initially saved when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    public void onDestroy2(){
        File saveFile2 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+getString(R.string.notesLatTxt));
        try {
            PrintWriter writer2 = new PrintWriter(saveFile2);
            writer2.print("");
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(saveFile2, true));
            for(int i = 0; i < notesLat.size(); i++){
                bufferedWriter2.write(String.valueOf(notesLat.get(i)) + "\n");
            }
            bufferedWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to be a helper for onDestroy().
     * This is where the notes longitude list is initially saved when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    public void onDestroy3(){
        File saveFile3 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+getString(R.string.notesLongTxt));
        try {
            PrintWriter writer3 = new PrintWriter(saveFile3);
            writer3.print("");
            writer3.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter3 = new BufferedWriter(new FileWriter(saveFile3, true));
            for(int i = 0; i < notesLong.size(); i++){
                bufferedWriter3.write(String.valueOf(notesLong.get(i)) + "\n");
            }
            bufferedWriter3.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}