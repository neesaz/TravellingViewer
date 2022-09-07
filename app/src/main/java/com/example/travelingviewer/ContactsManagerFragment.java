/*
 * @author: Rachel Stinnett and Nees Abusaada
 * @file: ContactsManagerFragment.java
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
 * ContactsManagerFragment.java the contacts are gathered and organized so
 * that the user can share the images or notes through text or email. This
 * program does this by using content providers.
 */
package com.example.travelingviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsManagerFragment extends Fragment {
    private ArrayList<String> contactList = new ArrayList<>();
    private ArrayList<String> contactIdList = new ArrayList<>();
    int[] contactsListImages = new int[]{
            R.drawable.contacts};
    private Activity containerActivity;
    private String noteEntered;
    private String email = null;
    private String phoneNumber = null;
    private int saveIndex;
    private String imageFilePath;
    private String imageInfo;
    private  View view;

    /**
     * The purpose of this method is to create a context of an activity within
     * the fragment to keep track of the container activity from main. This
     * makes the process of completing certain tasks a lot easier when there is
     * a context of the main activity.
     * @param containerActivity = An activity that represents the container
     * activity which is main.
     */
    public void setContainerActivity(Activity containerActivity) {
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
            noteEntered = getArguments().getString("entered");
            imageFilePath = getArguments().getString("photoPath");
            imageInfo = getArguments().getString("dateLocation");
        }
    }

    /**
     * The purpose of this method is to be called to have the
     * fragment instantiate its user interface view. This method
     * first inflates the view which is an important aspect apart
     * of all onCreateView() methods in fragments. In this method
     * the getContentProviderShare() and getDisplayScreen() is called.
     * And a List of Hashmaps is created for the simple adapter.
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
        view = inflater.inflate(R.layout.fragment_contacts_manager, container, false);
        getContentProviderShare();
        List<HashMap<String, String>> aList =
                new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < contactList.size(); i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("row_image", Integer.toString(contactsListImages[0]));
            hm.put("row_text", contactList.get(i));
            aList.add(hm);
        }
        getDisplayScreen(aList);
        return view;
    }

    /**
     * The purpose of this method is to gather the contacts from the user's device
     * and save them into ArrayLists to be used for email and text. This method
     * does this using getContentResolver().query() and a while loop to iterate
     * and gather the data. This is where the content provider is used.
     */
    public void getContentProviderShare(){
        Cursor cursor = containerActivity.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            //Gets the contact ID and Name.
            int idNum = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idNum);
            int givenNum = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String given = cursor.getString(givenNum);
            //ArrayList will be used in the next fragment for ArrayAdapter
            contactList.add(contactId + " - " + given);
            contactIdList.add(contactId);
        }
        cursor.close();
    }

    /**
     * The purpose of this method is to organize the list of hashmaps to be
     * used for the simple adapter list of contacts that has the contact
     * name with an image. This method does this by using a simple adapter
     * and then setting the listView to that adapter. Then an alert dialogue
     * is used for the user to specify what method of sharing they want
     * to use email or text.
     * @param aList = A List of Hashmaps that has the images and text.
     */
    public void getDisplayScreen(List<HashMap<String, String>> aList){
        String[] from = {"row_image", "row_text"};
        int[] to = {R.id.imageView3, R.id.textView3};
        SimpleAdapter simpleAdapter =
                new SimpleAdapter(containerActivity.getBaseContext(), aList,
                        R.layout.custom_layout_text3, from, to);
        ListView listView = (ListView) view.findViewById(R.id.contactsListView);
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                saveIndex = pos;
                new AlertDialog.Builder(containerActivity)
                        .setTitle(R.string.sharingChoose)
                        .setMessage(R.string.chooseMethodShare)
                        .setPositiveButton(R.string.chooseSMS, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                shareViaSMS();
                            }
                        })
                        .setNegativeButton(R.string.chooseGMAIL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                shareViaEmail();
                            }
                        })
                        .setIcon(R.drawable.sharingicon)
                        .show();
            }
        });
    }

    /**
     * The purpose of this method is to gather the phone numbers from the contacts
     * gathered into the contactIDList. This method does this by using getContentResolver().
     * query() and a while loop. This allows the phone number data to be gathered and saved
     * for text message. This is an example of a content provider.
     */
    public void shareViaSMS(){
        Cursor phones = containerActivity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactIdList.get(saveIndex),
                null, null);
        while (phones.moveToNext()) {
            //Gets the phone number
            int numberPhone = phones.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER);
            phoneNumber = phones.getString(numberPhone);
        }
        phones.close();
        smsIntentHelper();
    }

    /**
     * The purpose of this method is to be a helper for the shareViaSms() method. This
     * method is responsible for sending either a note or an image via text message.
     * This method sends the note as a message by using an SMS manager and an
     * Intent.ACTION_SENDTO with the sendTextMessage() method to pass the notes string.
     * This method sends the image as a message by using the SMS manager and an
     * Intent.ACTION_SENDTO with the sendTextMessage() method to pass the image info
     * string. Then a putExtra is used with Uri.parse() to get the image file path
     * to send.
     */
    public void smsIntentHelper(){
        if(noteEntered != null){
            //Uses SMSManager to compose and send.
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, noteEntered, null, null);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType(getString(R.string.smsString));
            smsIntent.setData(Uri.parse("sms:" + phoneNumber));
            startActivity(smsIntent);
        }
        else if(imageFilePath != null){
            //Uses SMSManager to compose and send.
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, imageInfo, null, null);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType(getString(R.string.smsString));
            smsIntent.setData(Uri.parse("sms:" + phoneNumber));
            //Adds the image to send.
            smsIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(imageFilePath));
            startActivity(smsIntent);
        }
    }

    /**
     * The purpose of this method is to gather the emails  from the contacts gathered into the
     * contactIDList. This method does this by using getContentResolver(). query() and a while loop.
     * This allows the email data to be gathered and saved to send the emails. This is an example
     * of a content provider. This method then uses if statements to check if it is a note
     * or image being sent and calls the helper methods to handle those cases.
     */
    public void shareViaEmail(){
        Cursor emails = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = "
                        + contactIdList.get(saveIndex), null, null);
        while (emails.moveToNext()) {
            int emailNum = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            email = emails.getString(emailNum);
        }
        emails.close();
        //Checks if it is an email or note being sent.
        if(noteEntered != null){
            emailIntentHelper1();
        }
        else if(imageFilePath != null){
            emailIntentHelper2();
        }
    }

    /**
     * The purpose of this method is to be a helper for the shareViaEmail() method.
     * This method is called if the user is trying to send a note. This method
     * uses an Intent.ACTION_SEND and put extras to add an email subject and
     * then the note entered. The startActivity() method is used to initialize
     * the intent. The reason this method is separated into 2 if statements if
     * because one handles if the saved contact has an email or not.
     */
    public void emailIntentHelper1(){
        if (email == null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.plainText));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.travellerNote));
            intent.putExtra(Intent.EXTRA_TEXT, noteEntered);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.plainText));
            //Adds an email
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {email});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.travellerNote));
            intent.putExtra(Intent.EXTRA_TEXT, noteEntered);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    /**
     * The purpose of this method is to be a helper for the shareViaEmail() method.
     * This method is called if the user is trying to send an image. This method
     * uses an Intent.ACTION_SEND and put extras to adds the image file and the
     * image info. The startActivity() method is used to initialize the intent.
     * The reason this method is separated into 2 if statements if because one
     * handles if the saved contact has an email or not.
     */
    public void emailIntentHelper2(){
        if (email == null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.vndEmail));
            intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(imageFilePath));
            intent.putExtra(Intent.EXTRA_TEXT, imageInfo);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.vndEmail));
            //Adds an email
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {email});
            intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(imageFilePath));
            intent.putExtra(Intent.EXTRA_TEXT, imageInfo);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }
}