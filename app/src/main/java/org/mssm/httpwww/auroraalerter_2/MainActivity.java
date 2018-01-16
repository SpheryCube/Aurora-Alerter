package org.mssm.httpwww.auroraalerter_2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

//Start doing everything in camel case.


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    public Map<String, Coordinates> Places = new HashMap<String, Coordinates>();

    public boolean access_fine_location;

    Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    MyReceiver myReceiver;

    public int notificationFrequency = 30;      //The minimum safe frequency to avoid weird thread issues I will set as 30 seconds. Deny user input less than 30 seconds.
    public double probability_threshold = -1;   //(Eventually) The user can customize this. The app will only broadcast notifications if the probability found for the aurora in a given location is at or above this threshold.
    public String location = "Limestone";       //Default Location
    public String lat = "0";
    public String lon = "0";

    public TextView programMessagesTV;
    public TextView currentLocationTV;
    public TextView coordinatesDisplayTV;
    public TextView probabilityDisplayTV;


    public boolean alarmOn = false;
    public enum states_enum {

    }


    // Use shared preferences to store the last location and last probability.

    //Functionality to support doubles in shared preferences. The static keyword makes these methods global.
    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {

                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());                Log.d("Lifecycle", "I'm in onReceive of MyReceiver in MainActivity");   //We never get here

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                double probability = getDouble(preferences, "Probability", 0);                 //We have a special function for getting doubles from our preference

                probabilityDisplayTV.setText(timeStamp + ": There is currently a " + probability + "% chance of seeing the aurora in " + location + ".");
        }
    }

    public MainActivity() {
        Log.d("Lifecycle", "In constructor of MainActivity");

        //Northern is positive, southern is negative. Western is negative, southern is positive.
        //Preprogrammed locations.

        Coordinates Limestone = new Coordinates(46.9086, -67.8258);
        Places.put("Limestone", Limestone);
        //Coordinates of Qaanaaq, a northern town in Greenland
        Coordinates Qaanaaq = new Coordinates(77.28, -69.1350);
        Places.put("Qaanaaq", Qaanaaq);
        //Coordinates of Franz Josef Land, a northern russian island
        Coordinates Franz_Josef_Land = new Coordinates(81.0, 55.0);
        Places.put("Franz_Josef_Land", Franz_Josef_Land);
        Coordinates FairBanks = new Coordinates(64.842217, -147.753461);
        Places.put("FairBanks", FairBanks);
    }

    @Override
    public void onResume()
    {
        Log.d("Lifecycle", "I'm in on resume of the mainActivity"); //This is getting called after pressing the notification.
        super.onResume();

        //This information should only be present if the activity is brought up after clicking a notification.
        if (getIntent() != null && getIntent().getExtras() != null)         //Problem here. This block of code is always run whether we click on the notification or if we resume the app by clicking on its icon.
        {
            Log.d("Lifecycle", " I see an intent from the notification");
            Bundle extras = getIntent().getExtras();
            double returned_probabililty = extras.getDouble("Probability");
            String returned_location = extras.getString("LocationName");

            TextView probabilityDisplayTV = (TextView) findViewById(R.id.Probability_Display);
            probabilityDisplayTV.setText("The returned probability from the intent is: " + returned_probabililty + " for the location: " + returned_location);
        }
        else    //Use last stored probability
        {
            Log.d("Lifecycle", "I'm getting the last stored probability");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            TextView probabilityDisplayTV = (TextView) findViewById(R.id.Probability_Display);
            probabilityDisplayTV.setText("The last probability recorded was: " + getDouble(prefs, "Probability", 0) + " for the location: " + prefs.getString("Location", "Error"));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Allow location functionality
                    access_fine_location = true;

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

//Some basic activity methods.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Lifecycle", "In onCreate of main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //Get permission to use location.

        //Used for finding the GPS location
        buildGoogleApiClient();


        //Code for trying to allow the user to input time between when notifications are sent.
        boolean changed = false;
        EditText edit = (EditText)findViewById(R.id.notificationFrequencyET);
        edit.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    Log.d("Lifecycle", "I am inside the onTextChanged method of m");
                    notificationFrequency = Integer.parseInt(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


//Methods for getting current location
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Lifecycle", "In onConnected of MainActivity");
        if (access_fine_location == true) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(100); // Update location every second

            FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            mLastLocation = FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Lifecycle", "In on onConnectionSuspended of MainActivity");
    }

    @Override
    public void onLocationChanged(Location location) {
        //This is in regards to GPS location. The user may change locations in the sense that they want to know the probability of the aurora somewhere else in the world.
        // However, this method is only called when location is set to current (meaning GPS location) and the GPS location changes while location is still set to current.
        if (location.equals("Current"))
        {
            Log.d("Lifecycle", "In onLocationChanged of MainActivity");
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
            programMessagesTV.setText("Your GPS location has changed!");
            //updateUI();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Lifecycle", "In onConnectionFailed of MainActivity");
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

//MainActivity lifecycle methods.
    @Override
    protected void onStart() {
        Log.d("Lifecycle", "in onStart of MainActivity");

        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuroraService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        Log.d("Lifecycle", "MainActivity receiver is registered with Aurora Service");

        super.onStart();
        mGoogleApiClient.connect();
        Log.d("Lifecycle", "At end of onStart in MainActivity");

        programMessagesTV = (TextView) findViewById(R.id.programMessagesTV);
        currentLocationTV = (TextView) findViewById(R.id.currentLocationTV);
        coordinatesDisplayTV = (TextView) findViewById(R.id.coordinatesTV);
        probabilityDisplayTV = (TextView) findViewById(R.id.Probability_Display);


        currentLocationTV.setText("The selected location is " + location);
    }


    @Override
    public void onStop() {
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

//Integration with the UI
    public void onClickChangeLoc(View v) {
        Log.d("Lifecycle", "In onClickChangeLoc of MainActivity");

        String previous_location  = location;

        switch (v.getId()) {
            case R.id.currentloc_btn:

                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(10000); // Update location every second

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);

                if (mLastLocation != null) {
                    lat = String.valueOf(mLastLocation.getLatitude());
                    lon = String.valueOf(mLastLocation.getLongitude());
                }
                //Make a new entry in our dictionary with the coordinates of our current location.
                //The rest of the entries are pre-programmed.

                Coordinates Current = new Coordinates(Double.parseDouble(lat), Double.parseDouble(lon));
                Places.put("Current", Current);

                Log.d("Lifecycle", "My current latitude is: " + Double.parseDouble(lat));
                Log.d("Lifecycle", "My current longitude is: " + Double.parseDouble(lon));

                //Tell the program to use the entry of the dictionary we just made.
                location = "Current";
                break;
            case R.id.limestone_btn:
                location = "Limestone";
                break;
            case R.id.qaanaaq_btn:
                location = "Qaanaaq";
                break;
            case R.id.franz_josef_land_btn:
                location = "Franz_Josef_Land";
                break;
            case R.id.FairBanksBtn:
                location = "FairBanks";
                break;
        }

        currentLocationTV.setText("The selected location is " + location);

        coordinatesDisplayTV.setText("The latitude is: " + Places.get(location).get_latitude() + " and the Longitude is " + Places.get(location).get_longitude() + ".");

        //If the alarm was on and the previous location is different from the new location we want a probability for, restart the alarm so that it will use the new location
        //A new location doesn't mean we changed gps locations. A new location means the user wants to get the probability for a new location that is either preprogrammed, one they have
        //previously entered, or if they switched to getting their current gps location. The onChangeLocation method only functions when the user wants the probability for their gps location
        //and they have moved since they started the function there.
        if (previous_location != location && alarmOn == true)
        {
            stopAlarm();
            startAlarm();
        }
    }

    public void onClickStartService(View v)
    {
        startAlarm();
    }

    public void startAlarm(){
        alarmOn = true;
        Log.d("Lifecycle", "I'm in startAlarm of mainActivity");

        Intent intent = new Intent(this, AlarmReceiver.class);

        intent.putExtra("probabilityThreshold", probability_threshold);
        intent.putExtra("LocationName", location);
        intent.putExtra("Latitude", Places.get(location).get_latitude());
        intent.putExtra("Longitude", Places.get(location).get_longitude());
        intent.putExtra("URL", "http://services.swpc.noaa.gov/text/aurora-nowcast-map.txt");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (3 * 1000), pendingIntent);            //One time alarm.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),  (notificationFrequency * 1000 /** 60 */ * 5), pendingIntent);

        TextView currentLocationTV = (TextView) findViewById(R.id.alarmStatusTV);
        currentLocationTV.setText("Notifications are on.");
    }

    public void onClickStopService(View v)
    {
        stopAlarm();
    }

    //Called anytime the user hits the stop service button or when the location is changed. If the location is changed, and the app was previously running, immedaitely restart the service.
    public void stopAlarm()
    {
        if (alarmOn)
        {
            alarmOn = false;
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1253, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);

            TextView currentLocationTV = (TextView) findViewById(R.id.alarmStatusTV);
            currentLocationTV.setText("Notifications are off.");
        }
        else
        {
            programMessagesTV.setText("The alarm is already off!");
        }

    }

    public void onClickQuickUpdate(View v)
    {
        //If alarm is on don't do anything because if the UI is open it will be updated with the new probability as soon as a new notification is broadcasted.
        if (alarmOn)
        {
            //Don't do anything
            Log.d("Lifecycle", "Im in the alarmOn == true case of the onClickQuickUpdate method in mainActivit");
            programMessagesTV.setText("Notifications are already streaming!");
        }
        else
        {
            //Easy case. Just call the downloader once.
            Log.d("Lifecycle", "I'm in startAlarm of mainActivity");

            Intent intent = new Intent(this, AlarmReceiver.class);

            intent.putExtra("probabilityThreshold", probability_threshold);
            intent.putExtra("LocationName", location);
            intent.putExtra("Latitude", Places.get(location).get_latitude());
            intent.putExtra("Longitude", Places.get(location).get_longitude());
            intent.putExtra("URL", "http://services.swpc.noaa.gov/text/aurora-nowcast-map.txt");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1 * 1000), pendingIntent);            //One time alarm.
        }
    }
}
