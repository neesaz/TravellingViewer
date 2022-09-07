/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: MapsFragment.java
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
 * MapsFragment.java is where the user can actually view a google map that has
 * pins on it representing the locations added, a photo taken, or a note taken.
 * This is done using the methods provided by Android Studio as well as the
 * GoogleMap API which needed a key to be set up.
 */
package com.example.travelingviewer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class MapsFragment extends Fragment {
    private Double cityLng;
    private Double cityLat;
    private String cityName;
    private AppCompatActivity containerActivity;
    private ArrayList<String> locationsList;
    private Double imageLat;
    private Double imageLong;
    private String imageName;
    private Double noteLat;
    private Double noteLong;
    private String noteName;

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
            locationsList = getArguments().getStringArrayList("locationList");
            cityLat = getArguments().getDouble("cityLat");
            cityLng = getArguments().getDouble("cityLong");
            cityName = getArguments().getString("cityName");
            imageLat = getArguments().getDouble("imageLat");
            imageLong = getArguments().getDouble("imageLong");
            imageName = getArguments().getString("imageName");
            noteLat = getArguments().getDouble("noteLat");
            noteLong = getArguments().getDouble("noteLong");
            noteName = getArguments().getString("noteName");
        }
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments.
     * @param inflater = An inflater used to inflate the fragment.
     * @param container = The view container from main activity that can
     *  have elements added to it or replaced.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     * @return view = A view returned so that the fragment can
     * be correctly placed onto the container.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    /**
     * The private class variable is where the map becomes available
     * and ready to use. This allows pins to be add or the camera
     * to be moved. Google Play services ahd to be installed
     * on the device.
     */
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * The purpose of this method is to make the google map
         * available for the user to use and manipulate. This
         * method does this by first adding a marker to the
         * original location of the overall trip coordinates.
         * Then this method does a for loop for the locations
         * list to add a marker to all the places the user
         * added to the list. Then if statements are used
         * in order to add the image or note depending on
         * where this fragment was called. A marker
         * is added to represent the location where the
         * image or note was captured.
         * @param googleMap = An instance of a Google Map.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng city = new LatLng(cityLat, cityLng);
            googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(60)).position(city).title("Marker in " + cityName));
            if(locationsList!= null){
                for(int i = 0; i < locationsList.size(); i++){
                    String location = locationsList.get(i);
                    String[] temp = location.split(", ");
                    String markName = temp[0];
                    Double markLng = Double.valueOf(temp[2]);
                    Double markLat = Double.valueOf(temp[1]);
                    LatLng place = new LatLng(markLat, markLng);
                    googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(300)).position(place).title("Marker in " + markName));
                }
            }
            if(imageLat != 0.0 && imageLong != 0.0 && imageName != null){
                LatLng taken = new LatLng(imageLat, imageLong);
                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(140)).position(taken).title("Marker in " + imageName));
            }
            if(noteLat != 0.0 && noteLong != 0.0 && noteName != null){
                LatLng taken = new LatLng(noteLat, noteLong);
                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(260)).position(taken).title("Marker in " + noteName));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(city));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(city, 9F));
        }
    };

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreatedView() methods in fragments. This is
     * where the fragmep for the map is found and asynced.
     * @param view = Represents the current view.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}