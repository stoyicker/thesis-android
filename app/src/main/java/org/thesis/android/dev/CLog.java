package org.thesis.android.dev;

import android.util.Log;

import org.thesis.android.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CLog {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale
            .ENGLISH);

    public static void e(String tag, String msg) {
        Log.e(tag, FORMATTER.format(new Date()) + ": " + msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.d(tag, FORMATTER.format(new Date()) + ": " + msg);
    }

    public static void wtf(String tag, Throwable e) {
        if (BuildConfig.DEBUG)
            Log.wtf(tag, FORMATTER.format(new Date()) + ": " + e);
    }
}
