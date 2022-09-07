/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: WebsiteViewFragment.java
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
 * WebsiteViewFragment.java the website is placed onto the web view so that if
 * the user clicks on a specific location in the locations list a Wikipedia
 * page about that place loads.
 */
package com.example.travelingviewer;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebsiteViewFragment extends Fragment {
    private Activity containerActivity;
    private String webUrl;

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
     * saves those as class variables to use.
     * @param savedInstanceState = A Bundle object used to
     * re-create the activity so that prior information is not
     * lost.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            webUrl = getArguments().getString("websiteUrl");
        }
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the WebView is set to the url passed into the fragment
     * using the loadUrl method().
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
        View view = inflater.inflate(R.layout.fragment_website_view, container, false);
        WebView webView = (WebView)view.findViewById(R.id.mainWebview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(webUrl);
        return view;
    }
}