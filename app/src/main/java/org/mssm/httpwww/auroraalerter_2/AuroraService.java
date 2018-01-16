package org.mssm.httpwww.auroraalerter_2;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by daniel14 on 10/3/16.
 */



public class AuroraService extends IntentService {

    //Fields that are passed in by intent.
    private double latitude;
    private double longitude;
    private String url;
    private String LocationName;
    private double probability_threshold;


   // Time now = new Time();

    final static String MY_ACTION = "myAction";

    public AuroraService()
    {
        super("AuroraService");
        Log.d("Lifecycle", "In constructor of AuroraService");
    }

    @Override
    protected void onHandleIntent(Intent my_intent)
    {
        Log.d("Lifecycle", "In onHandleIntent of AuroraService");

        LocationName = my_intent.getStringExtra("LocationName");
        latitude = my_intent.getDoubleExtra("Latitude", 0);
        longitude = my_intent.getDoubleExtra("Longitude", 0);
        probability_threshold = my_intent.getDoubleExtra("probabilityThreshold", 20);
        url = my_intent.getStringExtra("URL");

        Downloader myDownloader = new Downloader(this, url, latitude, longitude);

        //Download URL here.
        double probability = myDownloader.startDownload();

        //Store the latest probabilty in SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Update sharedpreferences data.
        MainActivity.putDouble(prefs.edit(), "Probability", probability);                   //We have a special function for putting doubles into our preference
        prefs.edit().putString("LocationName", LocationName);
        prefs.edit().apply();


        //Send data to different places
        sendNotification(probability);      //Put data into an intent
        sendDataToMainActivity();    //Transfer data via shared preferences but still send an intent.
        Log.d("Lifecycle", "We got to the end of Aurora Service's onHandleIntent method.");
    }

    public void sendNotification(double probability)
    {
        Log.d("Lifecycle", "I'm in sendNotification of AuroraService");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        @SuppressWarnings("deprecation")

        Intent notificationIntent = new Intent(this, MainActivity.class);       //Bring up MainActivity when notification is clicked.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder myBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc)
                        .setContentTitle("Aurora Update")
                        .setContentText("There is currently a " + Double.toString(probability) + "% chance of seeing the aurora in " + LocationName + ".");

        myBuilder.setContentIntent(pendingIntent);
        int mNotificationID = 001;

        if (probability > probability_threshold) {notificationManager.notify(mNotificationID, myBuilder.build());}
        else { Log.d("Lifecycle", "The downloaded probability is less than the user set (or default) threshold, so no notification will be published.");}
    }

    public void sendDataToMainActivity()
    {
        Log.d("Lifecycle", "I'm in sendDataToMainActivity of AuroraService");
        Intent intentForMainActivity = new Intent();
        intentForMainActivity.setAction(MY_ACTION);
        sendBroadcast(intentForMainActivity);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}



