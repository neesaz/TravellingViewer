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
 * LocationFragment.java is where the user can save locations in a city to be pinned
 * on a map. The user can view the map of pinned locations, share the location using
 * email or text, or view a Wikipedia page of that location.
 */
package com.example.travelingviewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LocationFragment extends Fragment {
    private View view;
    private String tripName;
    private String tripYear;
    private Double tripLng;
    private Double tripLat;
    private AppCompatActivity containerActivity;
    public ArrayList<String> locationsList = new ArrayList<>();
    public ArrayList<String> displayList = new ArrayList<>();
    public static ArrayList<String> copyLocationsList = new ArrayList<>();
    private ListView locationListView;
    public String locationEntered;
    private Boolean deleteClicked = false;

    /**
     * The purpose of this private class is to be an async task that
     * processes the Wikipedia JSON object to gather the website
     * URL that will be used for the web view in the WebsiteViewFragment.
     */
    private class WikipediaJsonTask extends AsyncTask<String, Integer, String> {
        /**
         * The purpose of this method is is to process the Wikipedia JSON object
         * in the background so that the right information could be gathered
         * to view the website in a web view. This method does this by
         * establishing a URL connection and then reading the input stream.
         * Then this method parses through the JSON Object to find the
         * url of the website. This is done by using getJSONObject()
         * method. The url is then returned.
         * @param strs = A string that represents the location to provide
         *  information about in Wikipedia.
         * @return string = A string that represetns the website url.
         */
        @Override
        protected String doInBackground(String... strs) {
            String json = "";
            String line = "";
            String pageUrl ="";
            try {
                URL getUrl = new URL(getString(R.string.wikipediaURLPart1) +
                        strs[0] + getString(R.string.wikipediaURLPart2));
                URLConnection myUrl = getUrl.openConnection();
                myUrl.setRequestProperty("user-agent", getString(R.string.requestProperty));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myUrl.getInputStream()));
                while ((line = bufferedReader .readLine()) != null) {
                    json += line;
                }
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONObject query = jsonObject.getJSONObject("query");
                JSONObject pages = query.getJSONObject("pages");
                HashMap<String, String> map = new HashMap<String, String>();
                Iterator<?> keys = pages.keys();
                String firstKey = "";
                //Finds the first key to then parse through.
                while(keys.hasNext()){
                    firstKey = (String)keys.next();
                    break;
                }
                JSONObject pageNum = pages.getJSONObject(firstKey);
                pageUrl = pageNum.getString("fullurl");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pageUrl;
        }

        /**
         * The purpose of this method is to take the string url and create the
         * web view fragment to pass the url into so that the website can be
         * displayed. This method does this by using the correct fragment
         * creation conventions such as setting the arguments and getting
         * the fragment manager, etc.
         * @param result = A string that represents the url of the website.
         */
        @Override
        protected void onPostExecute(String result){
            WebsiteViewFragment websiteViewFragment = new WebsiteViewFragment();
            websiteViewFragment.setContainerActivity(containerActivity);
            Bundle args = new Bundle();
            args.putString("websiteUrl", result);
            websiteViewFragment.setArguments(args);
            getFragmentManager().beginTransaction().
                    replace(R.id.innerLayout,websiteViewFragment).
                    addToBackStack(null).commit();
        }
    }

    /**
     * This private async task class functions as a helper to call the methods
     * to not have a lot running on the Main UI thread.
     */
    private class LocationHelperTask extends AsyncTask<String, Integer, String> {
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
            handleAddButton();
            handleDeleteButton();
            handleShareButton();
            handleMapButton();
            handleListViewClicks();
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
            tripName = getArguments().getString("tripName");
            tripLng = getArguments().getDouble("tripLng");
            tripLat = getArguments().getDouble("tripLat");
            tripYear = getArguments().getString("tripYear");
        }
        uploadFileData();
        uploadFileData2();
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the location helper task is created and executed in order
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
        view = inflater.inflate(R.layout.fragment_location, container, false);
        locationListView = view.findViewById(R.id.locationListView);
        LocationHelperTask locationHelperTask = new LocationHelperTask();
        locationHelperTask.execute();
        return view;
    }

    /**
     * The purpose of this method is to add a location to the locations list view
     * to represent the pined locations. This method does this by checking if the
     * add button was clicked after text was entered into the edit text. This
     * is done by using a .setOnClickListener() method. If the button was clicked
     * then the location that was entered is checked using a helper method
     * to make sure it exists. Then it is translated to longitude and
     * latitude locations using geocoder. Then the location entered
     * is formatted to be saved with the date and longitude and
     * latitude into ArrayLists. Then the ArrayAdapter is created
     * with the ArrayList and the list view sets the adapter.
     */
    public void handleAddButton(){
        Button addButton = view.findViewById(R.id.addButton3);
        EditText editText = view.findViewById(R.id.editText3);
        addButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String locationCheck = editText.getText().toString();
                Boolean error = checkTripEntered();
                if(error.equals(false)){
                    //Uses to translate locations into long/lat
                    Geocoder tripLocation = new Geocoder(containerActivity);
                    ArrayList<Address> addresses = null;
                    try {
                        addresses = (ArrayList<Address>) tripLocation.getFromLocationName(locationCheck, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address check =  addresses.get(0);
                    Double locationLat = check.getLongitude();
                    Double locationLong = check.getLatitude();
                    locationEntered = locationCheck + ", " + locationLong + ", " +
                            locationLat + ", " + LocalDate.now().toString();
                    locationsList.add(locationEntered);
                    displayList.add(locationCheck + ", " + LocalDate.now().toString());
                    ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                            R.layout.custom_layout_text4, R.id.textView4, displayList);
                    locationListView.setAdapter(arrayAdapter);
                    copyLocationsList = new ArrayList<String>(locationsList);
                }
            }
        });
    }

    /**
     * The purpose of this method is to delete the location that was
     * added to the list view of locations that represent a map
     * pin to view. This method does this by checking to see if
     * the delete button was clicked and then an element in the
     * list view was clicked by using .setOnClickListener() and
     * .setOnItemClickListener(). This method also uses if
     * statements and a boolean to keep track of the correct
     * button to ArrayList click combination. If this is the case
     * then the location is removed from the ArrayLists and a new
     * ArrayAdapter is created to update the list view to reflect
     * this change by setting the adapter.
     */
    public void handleDeleteButton(){
        Button deleteButton = view.findViewById(R.id.deleteButton3);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            locationsList.remove(locationsList.get(i));
                            displayList.remove(displayList.get(i));
                            //copyLocationsList.remove(copyLocationsList.get(i));
                            ArrayAdapter arrayAdapter = new ArrayAdapter(containerActivity.getBaseContext(),
                                    R.layout.custom_layout_text4, R.id.textView4, displayList);
                            locationListView.setAdapter(arrayAdapter);
                            copyLocationsList = new ArrayList<String>(locationsList);
                        }
                        deleteClicked = false;

                    }
                });
            }
        });
    }

    /**
     * The purpose of this method is to share the location that was
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
    public void handleShareButton(){
        Button shareButton = view.findViewById(R.id.shareLocationButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            ContactsManagerFragment contactsFragment = new ContactsManagerFragment();
                            contactsFragment.setContainerActivity(containerActivity);
                            Bundle args = new Bundle();
                            args.putString("entered", locationsList.get(i));
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
     * This method is responsible for displaying the pinned locations on the
     * map when the user clicks on the view on map button. This method does
     * this by using a .setOnClickListener() to check if the button was
     * clicked. Then this method creates a MapFragment and passes in
     * the location data needed to display the pined locations into
     * the bundle. Then the fragment is correctly initialized using
     * the .getFragmentManager(), etc.
     */
    public void handleMapButton(){
        Button mapButton = view.findViewById(R.id.viewMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setContainerActivity(containerActivity);
                Bundle args = new Bundle();
                args.putStringArrayList("locationList", locationsList);
                args.putDouble("cityLong", tripLng);
                args.putDouble("cityLat", tripLat);
                args.putString("cityName", tripName);
                mapsFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                        mapsFragment).addToBackStack(null).commit();
            }
        });
    }

    /**
     * This method is responsible for displaying the website that
     * provides more information about a pinned location using
     * Wikipedia. This method does this by keeping track on if
     * the button is clicked and if an element of the list
     * view is clicked. This method does this by using
     * .setOnClickListener() and .setOnItemClickListener(). If
     * statements and a boolean are used to keep track of the
     * order the button is clicked on and the list view element.
     * If this is the case then the specific place is accessed
     * from the locationslist and passed into the WikipediaJsonTask
     * which handles the website fragment within there.
     */
    public void handleListViewClicks(){
        Button webButton = view.findViewById(R.id.webPageButton);
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            String place = locationsList.get(i);
                            String[] temp = place.split(", ");
                            String searchWord = temp[0];
                            WikipediaJsonTask wikiTask = new WikipediaJsonTask();
                            wikiTask.execute(searchWord);
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * The purpose of this method is check if the location initially
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
        EditText editText = view.findViewById(R.id.editText3);
        String tripEntered = editText.getText().toString();
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
                R.layout.custom_layout_text4, R.id.textView4, displayList);
        locationListView.setAdapter(arrayAdapter);
    }

    /**
     * The purpose of this method is to read in the locationsList that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the locationsList is reset to the data that was read from
     * the file.
     */
    public void uploadFileData(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +tripName+tripYear+getString(R.string.displayText));
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
                locationsList = temp;
                copyLocationsList = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to read in the DisplayList that was
     * previously saved during onDestroy. This method does this by
     * first accessing the file. Then it uses file reader and a
     * buffered reader to read through each line using a while loop.
     * Then the DisplayList is reset to the data that was read from
     * the file.
     */
    public void uploadFileData2(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +tripName+tripYear+getString(R.string.displayText));
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
                displayList = temp;
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

    }

    /**
     * The purpose of this method is to be a helper for onDestroy().
     * This is where the locations list is initially saved when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    public void onDestroy1(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +tripName+tripYear+getString(R.string.displayText));
        try {
            PrintWriter writer = new PrintWriter(saveFile);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFile, true));
            for(int i = 0; i < locationsList.size(); i++){
                bufferedWriter.write(locationsList.get(i) + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to be a helper for onDestroy().
     * This is where the locations list is initially saved when the
     * user navigates away from the screen causing the fragment
     * lifecycle to reach this point. This method does this by
     * creating a new text file and clearing it so it is not
     * overly written. Then this method uses a buffered writer
     * and a for loop to write the data into the text file.
     */
    public void onDestroy2(){
        File saveFile2 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +tripName+tripYear+getString(R.string.displayText));
        try {
            PrintWriter writer2 = new PrintWriter(saveFile2);
            writer2.print("");
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(saveFile2, true));
            for(int i = 0; i < displayList.size(); i++){
                bufferedWriter2.write(displayList.get(i) + "\n");
            }
            bufferedWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}