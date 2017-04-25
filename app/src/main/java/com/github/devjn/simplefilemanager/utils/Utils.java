package com.github.devjn.simplefilemanager.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * Utils
 */

public class Utils {

    public static String fileExt(String name) {
        if (name.contains("?")) {
            name = name.substring(0, name.indexOf("?"));
        }
        if (name.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = name.substring(name.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 110);
        return noOfColumns;
    }

}
