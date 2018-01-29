package com.dravite.homeux;

import android.util.Log;

/**
 * Provides static methods to measure exact relative times to check for loading performance issues. All timinigs are given in nanoseconds and printed in ns and ms.
 */
public class QuickTimer {

    static long time = 0;
    static long startTime = 0;

    /**
     * Converts the absolute value to a difference relative value. This is the elapsed time.
     * @param time The last time measurement
     * @return the elapsed time since the last time measurement
     */
    public static long nsDelta(long time){
        return System.nanoTime()-time;
    }

    public static long msDelta(long nsTime){
        return nsDelta(nsTime)/1000000;
    }

    public static void beginTimer(){
        time = System.nanoTime();
        startTime = System.nanoTime();
    }

    public static void print(String message){
        Log.d("Timer", message + " ----- took " + nsDelta(time) + " ns (or " + msDelta(time) + " ms)");
    }

    public static void printAndRestart(String message){
        print(message);
        time = System.nanoTime();
    }

    public static void endTimer(){
        Log.d("Timer", "Ending after " + nsDelta(startTime) + " ns (or " + msDelta(startTime) + " ms)" );
    }
}
