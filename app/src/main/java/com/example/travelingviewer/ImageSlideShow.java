/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: ImageSlideShow.java
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
 * ImageSlideShow.java all the images taken and stored within the app are
 * gathered to be displayed in a slide show collage of 4 images. This program
 * does this by using the ImageSlideShowService.java which is used to rotate
 * the images in the background every couple of seconds.
 */
package com.example.travelingviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ImageSlideShow extends AppCompatActivity {
    private static int[] ids = {R.id.image_1, R.id.image_2, R.id.image_3, R.id.image_4};
    private int index = 0;
    public static ArrayList<String> imagePathsCollect;

    /**
     * The purpose of this method is to be the place where the activity starts
     * for the java program. The onCreate() method is supposed to initialize
     * the activity. The setContentView is set to R.layout.activity_main.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slide_show);
    }

    /**
     * The purpose of this onResume() method is to load the slide show service every
     * time this actvity screen is reloaded. This method gets the array list of
     * trips passed in and calls a helper method to gather the image paths. Then
     * a handler is used to be able to update the imageViews and then an intent
     * is used to called to start the ImageSlideShowService() with a messenger
     * that contains a handler passed in.
     */
    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> tripList = getIntent().getExtras().getStringArrayList("tripList");
        uploadPaths(tripList);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //Get the image object
                ImageView imageView = (ImageView) findViewById(getNextId());
                imageView.setImageBitmap((Bitmap) msg.obj);
            }
        };

        Intent intent = new Intent(this, ImageSlideShowService.class);
        intent.putExtra("MESSENGER", new Messenger(handler));
        startService(intent);

    }

    /**
     * This method is a helper that helps the imageViews become
     * updated one after the other after a couple seconds pass.
     * @return int id = An integer that represents the imageView
     * ID.
     */
    private int getNextId() {
        int id = ids[index++];
        index %= 4;
        return id;
    }

    /**
     * The purpose of this method is to upload the image paths of all of the
     * image files saved into the app. This method does this by iterating
     * through the trip list array list since the text file follow the naming
     * convention based on the trip. Then this method checks if the file exists.
     * If so, then the file is read and the image paths strings are organized
     * into a temp array list. Once it is done reading the temp is checked
     * to make sure it contains elements then image paths are set to the
     * temp.
     * @param tripList = An ArrayList of strings that has the trip name
     * and trip data(month-year).
     */
    public void uploadPaths(ArrayList<String> tripList ){
        ArrayList<String> temp = new ArrayList<>();
        for(int i = 0; i< tripList.size(); i++){
            String cityName = tripList.get(i).split(", ")[0];
            String cityYear = tripList.get(i).split(", ")[1];
            File saveFile = new File(this.getFilesDir(), "TravelingViewer"
                    +cityName+cityYear+"MemoriesPaths"+".txt");
            //Only continues if the file exists.
            if(saveFile.exists()){
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(saveFile);
                    String readLine = "";
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    while ((readLine = bufferedReader .readLine()) != null) {
                        temp.add(readLine);
                    }
                    fileReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(temp.size() != 0){
                imagePathsCollect = temp;
            }

        }
    }
}