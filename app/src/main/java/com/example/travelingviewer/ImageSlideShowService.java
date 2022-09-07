/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: ImageSlideShowService.java
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
 * ImageSlideShowService.java the images are rotated in the background every
 * couple of seconds.
 */
package com.example.travelingviewer;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

public class ImageSlideShowService extends IntentService {

    Handler handler;

    /**
     * The purpose of this method is to be a constructor for
     * the ImageSlideShowService class. It is called when an instance
     * of the class is created. At the time of calling the constructor,
     * memory for the object is allocated in the memory. It is a special
     * type of method which is used to initialize the object.
     */
    public ImageSlideShowService() {
        super("ImageSlideShowService");
    }

    /**
     * The purpose of this method is to be the place where the
     * activity starts for the java program. The onCreate()
     * method is supposed to initialize the activity. The
     * purpose of this onCreate() is to initialize a handler and
     * call the super since this java file extends IntentService
     */
    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
    }

    /**
     * The purpose of this method is to invoke on the worker
     * thread with a request to process. Only one Intent is processed at a
     * time, but the processing happens on a worker thread that runs
     * independently from other application logic. If this code takes
     * a long time, it will hold up other requests to the same IntentService,
     * but it will not hold up anything else. Within this onHandleIntent()
     * method it tests a bundle and checks if it is empty. Then it creates a
     * messenger object. Then within the while true loop a for loop is used
     * to iterate through all of the image paths. Each image path is then
     * converted to a bitmap so that the msg object could be set to that.
     * Then this Message is sent through the messenger. The thread is put
     * to sleep for 2 seconds so there is a 2 second delay between images.
     * A counter is used to control when to stop the slide show so that it
     * now occurring infinitely.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        Messenger messenger = (Messenger) bundle.get("MESSENGER");
        int slideShowCount = 0;
        while (true) {
            for(int i = 0; i< ImageSlideShow.imagePathsCollect.size(); i++){
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                //Converts to a bitmap
                Bitmap image = BitmapFactory.decodeFile(
                        ImageSlideShow.imagePathsCollect.get(i), bmOptions);
                Message msg = Message.obtain();
                msg.obj = image;
                try {
                    messenger.send(msg);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
