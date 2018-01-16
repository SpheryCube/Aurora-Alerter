package org.mssm.httpwww.auroraalerter_2;

/**
 * Created by daniel14 on 9/21/16org/mssm/httpwww/auroraalerter_2/Coordinates.java:3.
 */

class Coordinates
{
        double latitude;
        double longitude;

        public Coordinates(double Latitude, double Longitude)                //Constructor
        {
                 latitude = Latitude;
                 longitude = Longitude;
        }

        public double get_latitude()
        {
                return latitude;
        }
        public double get_longitude()
        {
                return longitude;
        }
}
