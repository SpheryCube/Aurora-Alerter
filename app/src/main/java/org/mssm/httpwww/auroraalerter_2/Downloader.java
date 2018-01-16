package org.mssm.httpwww.auroraalerter_2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by daniel14 on 10/25/16.
 */

public class Downloader {
    public String url;
    public double latitude;
    public double longitude;
    public double returnedData;
    public Context MyContext;

    public MyAuroraDataClass myAuroraDataObject = new MyAuroraDataClass();

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.

    public Downloader(Context setContext, String setURL, double setLatitude, double setLongitude)
    {
        MyContext = setContext;
        latitude = setLatitude;
        longitude = setLongitude;
        url = setURL;
    }

    public double startDownload()
    {
        ConnectivityManager connMgr = (ConnectivityManager) MyContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            Log.d("Networking", "I can connect to the world!");
            try {
                String probability = downloadUrl(url);
                Log.d("Lifecycle", "The probability returned from downloadURL is: " + probability);
                returnedData = Double.parseDouble(probability);
            } catch (IOException e) {
                Log.d("Lifecycle", "downloadURL exception");
            }
        } else {
            // display error
            Log.d("Networking", "Can't connect to the world!");
        }
        return returnedData;
    }

    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        Log.d("Lifecycle", "onConnectionSuspended start");

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("Networking", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Log.d("Lifecycle", "In readIt method of Downloader");
        myAuroraDataObject.extract_data(stream);

        int probability = myAuroraDataObject.get_prob_from_coordinates(latitude, longitude);

        String probability_as_string = Integer.toString(probability);
        Log.d("Lifecycle", "Probablity " + probability_as_string);

        return probability_as_string;
    }
}
