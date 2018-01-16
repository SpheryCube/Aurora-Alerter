package org.mssm.httpwww.auroraalerter_2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel14 on 9/16/16.
 */

public class MyAuroraDataClass {

    public List<List<Integer>> my_list_of_lists = new ArrayList<>();        //Each list inside the outer list corresponds to a single latitude value. Each list spans across all the longitude values.
    public int latitude_index; //Gets reassigned everytime we look at a new location.
    public int longitude_index; //Gets reassigned everytime we look at a new location.

    public int get_prob_from_coordinates(double latitude, double longitude)       //Returns the probably (out of 100) of seeing the aurora at a given longitude and latitude.
    {
        //Almost perfect
        latitude_index = (int) Math.round(511.0/180*(latitude + 90));
        longitude_index = (int) Math.round(1023.0/360*(longitude+180));

        Log.d("Lifecycle", "The latitude index (row) is: " + Integer.toString(latitude_index) + " and the longitude index (column) is " + Integer.toString(longitude_index));


        int probablity = my_list_of_lists.get(latitude_index).get(longitude_index);
        return probablity;

        //Get average probability of aurora over a certain radius around your location.
        //FInd visible aurora above horizon from your location. Get highest probability.

    }

    public void extract_data(InputStream stream) throws IOException         //Extracts data from stream and puts the data into a 2 dimensional list that is a field of this class.
    {
        //Get rid of all irrelevant information and seperate data into rows.
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        List<String> latitudesList = new ArrayList<String>();      //Contains a set of strings. Each string represents a constant latitude with varying longitude.

        String my_line = reader.readLine();
        while (my_line != null) {
            my_line = my_line.trim();
            if (my_line.charAt(0) != '#')       //Parse out the lines that are comments (lines that start with #)
            {
                //Log.d("ReadingText", my_line);
                latitudesList.add(my_line); //Each line represents a constant latitude and has 1024 longitudes values in it
            }
            my_line = reader.readLine();
        }

        //Get the digits for each line and put them into arrays.
        for (String line : latitudesList)       //For each line,
        {
            String[] digit_chars = line.split("\\s+"); //Seperate the digit characters by (one or more) white spaces.
            List<Integer> list_of_integers = new ArrayList<>();
            for (String digit_char : digit_chars)   //For each digit character we have in that line after sepearation
            {
                list_of_integers.add(Integer.parseInt(digit_char)); //Convert the digit character into an integer and append it to an array of integers.
            }
            my_list_of_lists.add(list_of_integers); //Appending a sublist
        }
    }
}

