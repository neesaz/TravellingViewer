/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: ImageAdapter.java
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
 * ImageAdapter.java this allows bitmap images to be used as an adapter instead
 * of drawable images in an ArrayAdapter. This program functions as a new
 * adapter object.
 */
package com.example.travelingviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<Bitmap>{

    private ArrayList<Bitmap> items;
    private Context c;

    /**
     * This method is the constructor for the ImageAdapter class. This
     * method initiates the context and arraylist of bitmap items.
     * @param context = The AppComaptActivity context of the program.
     * @param layoutResourceId = The layout that should be adapted to
     * the list view.
     * @param imageViewResourceId = The imageView instead the layout.
     * @param items = The ArrayList of bitmap images.
     */
    public ImageAdapter(AppCompatActivity context, int layoutResourceId,
                        int imageViewResourceId, ArrayList<Bitmap> items) {
        super(context, layoutResourceId,imageViewResourceId,items);
        c = context;
        this.items = items;
    }

    /**
     * The purpose of this method is to get the right view to be displayed and
     * adapted to the list view. This method does this by using a layout
     * inflater that gets the system service. Then this method sets the
     * image view from the layout to the bitmap. Then the view is
     * returned to be adapted.
     * @param position = A integer that represents the layout position.
     * @param convertView = A view that can be converted for the list view.
     * @param parent = The contect of the parent view group.
     * @return view = A view to be adapted to the list view.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_layout_text5, null);
        }
        Bitmap id = items.get(position);
        ImageView iv = (ImageView) v.findViewById(R.id.imageViewLayout);
        if (iv != null) {
            iv.setImageBitmap(id);
        }
        return v;
    }
}

