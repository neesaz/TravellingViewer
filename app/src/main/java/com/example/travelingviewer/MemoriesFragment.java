/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: MemoriesFragment.java
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
 * MemoriesFragment.java is where the user is able to take a picture within the app
 * and it saves to a list view. This user can either delete the image from the
 * list view, view the image info, view the image location on the map, or share
 * the image via email or sms. This is achieved with the use of a camera intent
 * and saving the image as a bitmap.
 */
package com.example.travelingviewer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MemoriesFragment extends Fragment {
    private AppCompatActivity containerActivity = null;
    private View inflatedView = null;
    public static String currentPhotoPath = "image.jpeg";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public ListView memListView;
    public Date lastModDate;
    private Boolean deleteClicked = false;
    private double cityLat;
    private double cityLng;
    private String cityName;
    private String cityYear;
    private double currLat;
    private double currLng;
    private  String phoneLocInfo;
    public ArrayList<Bitmap> savedImages = new ArrayList<>();
    public ArrayList<String> imageInfo = new ArrayList<>();
    public ArrayList<String> imagePaths = new ArrayList<>();
    private ArrayList<Double> imageLong = new ArrayList<>();
    private ArrayList<Double> imageLat = new ArrayList<>();

    /**
     * The purpose of this private async task is to a helper to call
     * all of the button click methods in the background so that
     * there is not too much running on the main thread.
     */
    private class HelperActivityTask extends AsyncTask<Bitmap, Integer, Bitmap> {

        /**
         * The purpose of this method is to call all of the methods in the background
         * thread. This method does this by calling all of the helper methods in
         * the background. This shows how this async task is more of a helper
         * than an actual task being completed.
         * @param bitmaps = An image that is passed into the async task.
         * @return bitmap = A image that will be used in onPostExecute.
         */
        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            onClickDelete();
            cameraButton();
            onClickInfo();
            onClickShare();
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
    public void setContainerActivity(AppCompatActivity containerActivity) {
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
     * @param savedInstance = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (getArguments() != null) {
            cityLat = getArguments().getDouble("tripLat");
            cityLng = getArguments().getDouble("tripLng");
            cityName = getArguments().getString("tripClicked");
            cityYear = getArguments().getString("tripYear");
        }
        uploadFileData();
        uploadFileData2();
        uploadFileData3();
        uploadFileData4();
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the helper task is created and executed in order to call all
     * of the helper methods in the background.
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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_memories, container, false);
        memListView = inflatedView.findViewById(R.id.memoriesListView);
        HelperActivityTask helperTask = new HelperActivityTask();
        helperTask.execute();
        setupLocationRequest();
        return inflatedView;
    }

    /**
     * This method is specified for the delete button. It finds the
     * id of the button then use an onClick passing View as a parameter.
     * It calls the setOnItemClickListener with checking that one item is
     * clicked by using if statements then get the index of the row that
     * was added in the list and delete it from the list. Otherwise, the
     * deleteClicked boolean variable still sited to false.
     */
    public void onClickDelete(){
        Button button = inflatedView.findViewById(R.id.deleteButtonMem);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                memListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            savedImages.remove(savedImages.get(i));
                            imageInfo.remove(imageInfo.get(i));
                            imagePaths.remove(imagePaths.get(i));
                            imageLat.remove(imageLat.get(i));
                            imageLong.remove(imageLong.get(i));
                            ImageAdapter imageAdapter = new
                                    ImageAdapter(containerActivity, R.layout.custom_layout_text5,
                                    R.id.imageViewLayout, savedImages);
                            memListView.setAdapter(imageAdapter);
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * This method is a helper to the camera button where it finds th e
     * id's button and use setOnClickListener to know when the user clicks
     * on the button. Then, it will call another method that gets an access
     * to the camera and let the user takes a picture and display it in the
     * screen.
     */
    public void cameraButton(){
        Button button = inflatedView.findViewById(R.id.addImageCamera);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    /**
     * This is a private method that allows the user to go the camera and
     * take picture then display it in the screen. First, it calls an
     * intent using media store with an action image. Using if statement
     * to check if it taking the correct intent and making sure it is not
     * null then will create a file where the image should go. Using try
     * to create an image file and catch to avoid the errors that will
     * happen during the creation of the file. Using another if statement
     * to check if the file id not null then use URL for it. Finally,passing
     * these information into the starting activity for result.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(containerActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                if(photoFile != null){
                    lastModDate = new Date(photoFile.lastModified());
                    imageInfo.add(phoneLocInfo + "-" + lastModDate);
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(containerActivity,
                        getString(R.string.providerString),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * This method is returning a file and its purpose to create a file
     * for the image. Using string to create the file name and a File to
     * get the directory of the picture. Also, passing an arguments to
     * the createTempFile with the type of the image and name. Also, it
     * saves a file.
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.format)).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = containerActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        if(image.getAbsolutePath() != null){
            imagePaths.add(image.getAbsolutePath());
        }
        return image;
    }

    /**
     * This method is onActivityResult which uses if statement to check if the
     * request to an image to be captured. Using BitmapFactory and Bitmap to
     * get the current path of the photo. Then, it adds the image into the arrayList
     * that has all the images. It uses the image adapter and set the adapter as well
     * based on the layout and the images that were added into the arrayList.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            //The imageNum is used to distinguish which image in the collage to change.
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            if(imageBitmap != null && currentPhotoPath != null){
                savedImages.add(imageBitmap);
                imageLong.add(currLng);
                imageLat.add(currLat);
                ImageAdapter imageAdapter = new
                        ImageAdapter(containerActivity, R.layout.custom_layout_text5,
                        R.id.imageViewLayout, savedImages);
                memListView.setAdapter(imageAdapter);
            }
        }
    }

    /**
     * This method is specified to showing the image's information.
     * It finds the id of the button then using setOnClickListener to
     * enable action when the user clicks on it and it checked if it
     * is clicked it will set it to true. Using the onItemClick method
     * for the button. Also, using if statement and creating new AlertDialog
     * to build a title, message, and with setting the button. Also, setting
     * the icon. Otherwise, for the if statement it will be setting to false.
     */
    public void onClickInfo(){
        Button button = inflatedView.findViewById(R.id.viewInfoButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                memListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            new AlertDialog.Builder(containerActivity)
                                    .setTitle(getString(R.string.imageInfo))
                                    .setMessage(getString(R.string.hereGather) +
                                            getString(R.string.takenAt) + imageInfo.get(i))
                                    .setPositiveButton(R.string.chooseOk, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setIcon(R.drawable.viewicon)
                                    .show();
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * This method is specified to showing the image's information.
     * It finds the id of the button then using setOnClickListener to
     * enable action when the user clicks on it and it checked if it
     * is clicked it will set it to true. If the button is clicked
     * it will check the row of the listview and find its id. Also,
     * getting the file path with calling the save image helper method.
     * Finally, it gets amp shareFragment passing in the file path.
     */
    public void onClickShare(){
        Button shareButton = inflatedView.findViewById(R.id.shareImageButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClicked = true;
                memListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(deleteClicked == true){
                            ListView listView = inflatedView.findViewById(R.id.memoriesListView);
                            String filePath = saveImageHelper(listView);
                            getMapShareFragment(filePath, i);
                        }
                        deleteClicked = false;
                    }
                });
            }
        });
    }

    /**
     * This method is specified for the mapShare. It builds a title of viewing and
     * sets a message. The user will have a small screen has a share and maps option.
     * When the user clicks on it it creates a contact fragment
     * and puts in the strings needed. Then, it calls the get Fragment manger with
     * starting transaction with replacing the innerLayout with committing it.
     * After setting the share button, it will go to the map button of the image.
     * Then, setting onClick method for the map option which will get the image
     * shown on map that creates a fragment. It puts the strings needed for the
     * screen. It calls the fragment with setting the getFragmentManager, starting
     * the transaction, replacing the inner layout.
     * @param filePath
     * @param imageID
     */
    public void getMapShareFragment(String filePath, int imageID){
        new AlertDialog.Builder(containerActivity).setTitle(R.string.viewingScreen).setMessage(R.string.shareOr)
                .setPositiveButton(R.string.chooseShare, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContactsManagerFragment contactsFragment = new ContactsManagerFragment();
                        contactsFragment.setContainerActivity(containerActivity);
                        Bundle args = new Bundle();
                        args.putString("photoPath",filePath);
                        args.putString("dateLocation", imageInfo.get(imageID));
                        contactsFragment.setArguments(args);
                        getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                                contactsFragment).addToBackStack(null).commit();
                    }
                }).setNegativeButton(R.string.chooseMaps, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsFragment mapsFragment = new MapsFragment();
                        mapsFragment.setContainerActivity(containerActivity);
                        Bundle args = new Bundle();
                        args.putDouble("cityLong", cityLng);
                        args.putDouble("cityLat", cityLat);
                        args.putString("cityName", cityName);
                        args.putStringArrayList("locationList", LocationFragment.copyLocationsList);
                        args.putDouble("imageLat", imageLat.get(imageID));
                        args.putDouble("imageLong", imageLong.get(imageID));
                        args.putString("imageName", "Image Capture" + (imageID+1));
                        mapsFragment.setArguments(args);
                        getFragmentManager().beginTransaction().replace(R.id.innerLayout,
                                mapsFragment).addToBackStack(null).commit();
                    }
                })
                .setIcon(R.drawable.viewicon)
                .show();
    }

    /**
     * This method returns a string of the file path. It creates a bitmap and sets
     * a new canvas with drawing it in the listview. After that it changes the from
     * array to compress the image to a specific form to be able to display the image.
     * It adds the information to the file path and call the media store of the an image
     * to be able to insert it.
     * @param listView
     * @return
     */
    public String saveImageHelper(ListView listView){
        Bitmap bitmap = Bitmap.createBitmap(listView.getWidth(),
                listView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        listView.draw(canvas);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String filePath = MediaStore.Images.Media.insertImage(
                containerActivity.getContentResolver(),
                bitmap,getString(R.string.imageTitle),null);
        return filePath;
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationChanged(android.location.Location location) {
                currLat = location.getLatitude();
                currLng = location.getLongitude();
                phoneLocInfo = String.valueOf(currLat) + "-" + String.valueOf(currLng);

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
     * This method is called when the activity goes into foreground,
     * but it will never be executed before the onCreate. It has the
     * image Adapter and it uses a custom layout with an id of an image.
     * The list sets an adapter based on the image adapter that was
     * created.
     */
    @Override
    public void onResume() {
        super.onResume();
        ImageAdapter imageAdapter = new
                ImageAdapter(containerActivity, R.layout.custom_layout_text5,
                R.id.imageViewLayout, savedImages);
        memListView.setAdapter(imageAdapter);
    }

    /**
     * This method upload a file of data. It starts with calling an object
     * file and using if statement to check the id the file doesn't exist then
     * it wil return. Then it creates File reader and by using try and catch
     * to open the file and use a buffer reader to read the file as will as
     * while loop to read every single line. Using if statement to check if
     * the line is not null , which is empty it will add it into the array.
     * Using another if statement to check if there is a data in the ArrayList
     * then it uses a for loop to  iterate through the uploaded images ArrayList.
     * It uses BitmapFactory and Bitmap to compose the image and add it into
     * uploadedImages ArrayList. Then it sets the savedImages ArrayList to
     * be equal to the uploaded images ArrayList. Finally, it closes the file
     * and catches all the errors that could happen during the process of
     * opening the file.
     */
    public void uploadFileData(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesPaths"+".txt");
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
                ArrayList<Bitmap> uploadedImages = new ArrayList<>();
                ArrayList<String> temp2 = new ArrayList<>();
                for(int i = 0; i< temp.size(); i++){
                    if(temp.get(i) != null){
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap imageBitmap = BitmapFactory.decodeFile(temp.get(i), bmOptions);
                        if(imageBitmap != null){
                            temp2.add(temp.get(i));
                            uploadedImages.add(imageBitmap);
                        }
                    }
                }
                savedImages = uploadedImages;
                imagePaths = temp2;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method upload a file of data. It starts with calling an object
     * file and using if statement to check the id the file doesn't exist then
     * it wil return. Then it creates File reader and by using try and catch
     * to open the file and use a buffer reader to read the file as will as
     * while loop to read every single line. Using if statement to check if
     * the line is not null , which is empty it will set the image info
     * to the temporary ArrayList. Finally, it closes the file
     * and catches all the errors that could happen during the process of
     * opening the file.
     */
    public void uploadFileData2(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesInfo"+".txt");
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
                imageInfo = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method upload a file of data. It starts with calling an object
     * file and using if statement to check the id the file doesn't exist then
     * it wil return. Then it creates File reader and by using try and catch
     * to open the file and use a buffer reader to read the file as will as
     * while loop to read every single line. Using if statement to check if
     * the line is not null , which is empty it will set the image latitude
     * to the temporary ArrayList. Finally, it closes the file
     * and catches all the errors that could happen during the process of
     * opening the file.
     */
    public void uploadFileData3(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+ "MemoriesLat"+".txt");
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
                imageLat = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method upload a file of data. It starts with calling an object
     * file and using if statement to check the id the file doesn't exist then
     * it wil return. Then it creates File reader and by using try and catch
     * to open the file and use a buffer reader to read the file as will as
     * while loop to read every single line. Using if statement to check if
     * the line is not null , which is empty it will set the image longitude
     * to the temporary ArrayList. Finally, it closes the file
     * and catches all the errors that could happen during the process of
     * opening the file.
     */
    public void uploadFileData4(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesLong"+".txt");
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
                imageLong = temp;
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is gets called when the activity had been in
     * the foreground or visible to the user, and once it gets called
     * it creates a new instance  of the activity. It is used to save the
     * images on the app. Using File to create a file with city name and year.
     * It create a file for the paths and info and called a help method to
     * write the file.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroy1();
        onDestroy2();
        onDestroy3();
        onDestroy4();
    }

    /**
     * This method is gets called when the activity had been in
     * the foreground or visible to the user, and once it gets called
     * it creates a new instance  of the activity. It is used to save the
     * images on the app. Using File to create a file with city name and year.
     * It create a file for the paths and info and called a help method to
     * write the file. Then a buffered writer with a for loop is used to
     * write each image path into the file.
     */
    public void onDestroy1(){
        File saveFile = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesPaths"+".txt");
        try {
            PrintWriter writer = new PrintWriter(saveFile);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFile, true));
            for(int i = 0; i < imagePaths.size(); i++){
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap testBitmap = BitmapFactory.decodeFile(imagePaths.get(i), bmOptions);
                if(testBitmap != null){
                    bufferedWriter.write(imagePaths.get(i) + "\n");
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is gets called when the activity had been in
     * the foreground or visible to the user, and once it gets called
     * it creates a new instance  of the activity. It is used to save the
     * images on the app. Using File to create a file with city name and year.
     * It create a file for the paths and info and called a help method to
     * write the file. Then a buffered writer with a for loop is used to
     * write each image info into the file.
     */
    public void onDestroy2(){
        File saveFile2 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesInfo"+".txt");
        try {
            PrintWriter writer2 = new PrintWriter(saveFile2);
            writer2.print("");
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(saveFile2, true));
            for(int i = 0; i < imageInfo.size(); i++){
                bufferedWriter2.write(imageInfo.get(i) + "\n");
            }
            bufferedWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is gets called when the activity had been in
     * the foreground or visible to the user, and once it gets called
     * it creates a new instance  of the activity. It is used to save the
     * images on the app. Using File to create a file with city name and year.
     * It create a file for the paths and info and called a help method to
     * write the file. Then a buffered writer with a for loop is used to
     * write each image latitude into the file.
     */
    public void onDestroy3(){
        File saveFile3 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesLat"+".txt");
        try {
            PrintWriter writer3 = new PrintWriter(saveFile3);
            writer3.print("");
            writer3.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter3 = new BufferedWriter(new FileWriter(saveFile3, true));
            for(int i = 0; i < imageLat.size(); i++){
                bufferedWriter3.write(String.valueOf(imageLat.get(i)) + "\n");
            }
            bufferedWriter3.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is gets called when the activity had been in
     * the foreground or visible to the user, and once it gets called
     * it creates a new instance  of the activity. It is used to save the
     * images on the app. Using File to create a file with city name and year.
     * It create a file for the paths and info and called a help method to
     * write the file. Then a buffered writer with a for loop is used to
     * write each image longitude into the file.
     */
    public void onDestroy4(){
        File saveFile4 = new File(containerActivity.getFilesDir(), "TravelingViewer"
                +cityName+cityYear+"MemoriesLong"+".txt");
        try {
            PrintWriter writer4 = new PrintWriter(saveFile4);
            writer4.print("");
            writer4.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter bufferedWriter4 = new BufferedWriter(new FileWriter(saveFile4, true));
            for(int i = 0; i < imageLong.size(); i++){
                bufferedWriter4.write(String.valueOf(imageLong.get(i)) + "\n");
            }
            bufferedWriter4.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


