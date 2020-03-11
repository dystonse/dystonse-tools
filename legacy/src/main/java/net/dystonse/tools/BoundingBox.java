package net.dystonse.tools;

import java.util.Arrays;

public class BoundingBox {

    static final int MIN_LON = 6;
    static final int MAX_LON = 15;
    static final int MIN_LAT = 47;
    static final int MAX_LAT = 56;
    
    int minX, minY, maxX, maxY;  

    static void optAssert(boolean assertion, String message) {
        if(!assertion) {
            throw new IllegalArgumentException(message);
        }
    }

    public BoundingBox(String rectString) {
        optAssert(rectString != null, "String was null.");
        String[] rectStrings = rectString.split(";");
        optAssert(rectStrings.length == 4, "String contained " + rectStrings.length + " numbers.");
        float[] rectValues = new float[4];
        for(int i = 0; i < 4; i++) {
            rectValues[i] = Float.parseFloat(rectStrings[i]);
            if(rectValues[i] < MIN_LON * 1_000_000)
                rectValues[i] *= 1_000_000;
        }
        
        Arrays.sort(rectValues);
        for(int i = 0; i < 2; i++)
            optAssert(rectValues[i] >  MIN_LON * 1_000_000 && rectValues[i] <  MAX_LON * 1_000_000, "Lon values must be between " + MIN_LON + " and " + MAX_LON + " degrees. You can use floats or integers, in any order.");
        for(int i = 2; i < 4; i++)
            optAssert(rectValues[i] >  MIN_LAT * 1_000_000 && rectValues[i] <  MAX_LAT * 1_000_000, "Lat values must be between " + MIN_LAT + " and " + MAX_LAT + " degrees. You can use floats or integers, in any order.");
        minX = (int)rectValues[0];
        maxX = (int)rectValues[1];
        minY = (int)rectValues[2];
        maxY = (int)rectValues[3];
    }

    public String getQueryString() {
        return "look_minx="+minX+"&look_maxx="+maxX+"&look_miny="+minY+"&look_maxy="+maxY;
    }
}