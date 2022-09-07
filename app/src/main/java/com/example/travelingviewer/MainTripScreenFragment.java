/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: MainTripScreenFragment.java
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
 * MainTripScreenFragment.java is where the user can add or delete trips, move
 * onto the next options screen, oro view an image slideshow. All of the
 * trip data such as the tripList is saved using internal storage text file
 * when the user closes the app, and it is uploaded when the user re-loads
 * and opens the app.
 */
package com.example.travelingviewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class MainTripScreenFragment extends Fragment {

    public ListView mainListView;
    private static ArrayList<String> tripList = new ArrayList<>();
    private String tripSelected;
    private AppCompatActivity containerActivity;
    private View view;
    private Boolean deleteClicked = false;

    /**
     * The purpose of this private async task is to be a helper
     * task that calls the methods so that there is not too
     * many elements running on the main UI for performance
     * reasons.
     */
    private class MethodHelperTask extends AsyncTask<String, Integer, String> {
        /**
         * The purpose of this method is to call the helper methods
         * used to deal with the list view. This is done to
         * not have a lot of elements running on the main UI thread.
         * @param strings = A string that could be used in the task.
         * @return string = A string that could be used in onPostExecute.
         */
        @Override
        protected String doInBackground(String... strings) {
            checkListView();
            addTrip();
            removeTrip();
            checkHighlights();
            openHelpScreen();
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
     * But since there were no data saved from the previous
     * fragment, this method just calls the uploadFileData()
     * method instead. This loads all of the tripList information
     * that was saved when the user closed the app.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uploadFileData();
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
        view = inflater.inflate(R.layout.fragment_main_trip_screen, container, false);
        mainListView = view.findViewById(R.id.listView);
        MethodHelperTask helperTask = new MethodHelperTask();
        helperTask.execute();
        return view;
    }

    /**
     * The purpose of this method is to add the trip entered into the trip
     * List View. This method does this by first checking what text
     * was entered when the add button was clicked using a
     * setOnClickListener() method. Then this method uses the
     * checkTripEntered() to make sure the place entered exists on
     * the map and is valid to not cause errors. Then if it is a
     * valid place it is added to the tripList and then the ArrayAdapter
     * is created and the list view is set to that.
     */
    public void addTrip(){
        Button addButton = view.findViewById(R.id.addButton);
        EditText editText = view.findViewById(R.id.editText);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks to make sure the place is valid.
                Boolean error = checkTripEntered();
                if(error.equals(false)){
                    String tripEntered = editText.getText().toString();
                    tripList.add(tripEntered);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                            R.layout.custom_layout_text, R.id.textView1, tripList);
                    mainListView.setAdapter(arrayAdapter);
                }
            }
        });
    }

    /**
     * The purpose of this method is to delete a trip that the delete
     * button and then a list view element was clicked on. This method
     * does this by first checking when the delete button was clicked
     * and with what element in the list view by using a setOnClickListener()
     * with a setOnItemClickListener(). A boolean is used to make sure the
     * correct click combination occurs. If this is the case then the
     * trip is removed from the trip list and the array adapter is updated
     * and the list view is set to that.
     */
    public void removeTrip(){
        Button removeButton = view.findViewById(R.id.deleteButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            tripList.remove(tripList.get(i));
                            ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                                    R.layout.custom_layout_text2, R.id.textView2, tripList);
                            mainListView.setAdapter(arrayAdapter);
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * The purpose of this method is to check which trip to go
     * to the next screen with which is the Options screen.
     * This method does this by first checking when the next
     * button was clicked and with what element in the list view
     * by using a setOnClickListener() with a setOnItemClickListener().
     * A boolean is used to make sure the correct click combination occurs.
     * If that is the case then the OptionScreenFragment is created and
     * gets arguments added. Then it is instantiated using the correct
     * methods such as getFragmentManager().
     */
    public void checkListView(){
        Button nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            tripSelected = tripList.get(i);
                            OptionsScreenFragment optionsScreenFragment = new OptionsScreenFragment();
                            optionsScreenFragment.setContainerActivity(containerActivity);
                            Bundle args = new Bundle();
                            args.putString("tripClicked", tripSelected);
                            optionsScreenFragment.setArguments(args);
                            getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                                    optionsScreenFragment).addToBackStack(null).commit();
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * The purpose of this method is to load the ImageSlideShow activity
     * if the user clicks the highlights button. This method does this
     * by using a setOnClickListener() and then creating the intent
     * for the ImageSlideShow class. The intent is started using the
     * startActivity().
     */
    public void checkHighlights(){
        Button highlightsButton = view.findViewById(R.id.highlightsButton);
        highlightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(containerActivity, ImageSlideShow.class);
                intent.putExtra("tripList", tripList);
                startActivity(intent);
            }
        });
    }

    /**
     * The purpose of this method is check if the trip initially
     * entered by the user to pin is an actual location that exists
     * in the map. This method does this by using geocoder to convert
     * the location to longitude and latitude. If an error occurs
     * the catch portion displays an Alert Dialog box that allows
     * the user to try again to enter the location.
     * @return boolean = A boolean that determines if there was an error.
     */
    public Boolean checkTripEntered(){
        Boolean error = false;
        Geocoder tripLocation = new Geocoder(containerActivity);
        EditText editText = view.findViewById(R.id.editText);
        String tripEntered = editText.getText().toString().split(", ")[0];
        ArrayList<Address> addresses = null;
        try {
            addresses = (ArrayList<Address>) tripLocation.getFromLocationName(tripEntered, 5);
            Address check =  addresses.get(0);
        } catch (Exception e) {
            error = true;
            new AlertDialog.Builder(containerActivity)
                    .setTitle(R.string.locationError)
                    .setMessage(R.string.placeEntered)
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(R.drawable.warningicon).show();
        }
        return error;
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
                R.layout.custom_layout_text,R.id.textView1,tripList);
        mainListView.setAdapter(arrayAdapter);

    }

    /**
     * The purpose of this method is to read in the tripList that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the tripList is reset to the data that was read from
     * the file.
     */
    public void uploadFileData(){
        File saveFile = new File(containerActivity.getFilesDir(), getString(R.string.appText));
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
                tripList = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to save the trip list when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        File saveFile = new File(containerActivity.getFilesDir(), getString(R.string.appText));
        try {
            PrintWriter writer = new PrintWriter(saveFile);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFile, true));
            for(int i = 0; i < tripList.size(); i++){
                bufferedWriter.write(tripList.get(i) + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * The purpose of this method is to load the Help Activity
     * Screen if the help button is clicked. This method does this
     * by using the setOnClickListener() to check if the button is
     * clicked. If so then the Help Activity screen is created and
     * loaded using an intent and the startActivity() method.
     */
    public void openHelpScreen(){
        Button locationButton = view.findViewById(R.id.helpButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HelpScreenMain.class);
                startActivity(intent);

            }
        });
    }

}
