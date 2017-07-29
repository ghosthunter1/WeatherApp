package com.example.ghost.weather;

/**
 * Created by ghost on 7/28/17.
 */

public class Days {
    private static String today = "Today";
    private static String sund = "Sun";
    private static String mond = "Mon";
    private static String tues = "Tues";
    private static String wede = "Wed";
    private static String thurs = "Thurs";
    private static String frid = "Fri";
    private static String satu = "Satu";


    public static final String[] mon = {today, tues, wede, thurs, frid, satu, sund};
    public static final String[] tue = {today, wede, thurs, frid, satu, sund, mond};
    public static final String[] wed = {today, thurs, frid, satu, sund, mond, tues};
    public static final String[] thu = {today, frid, satu, sund, mond, tues, wede};
    public static final String[] fri = {today, satu, sund, mond, tues, wede, thurs};
    public static final String[] sat = {today, sund, mond, tues, wede, thurs, frid};
    public static final String[] sun = {today, mond, tues, wede, thurs, frid, satu};

}
