package com.github.devjn.simplefilemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

/**
 * Created by @author Jahongir on 24-Feb-2018
 * devjn@jn-arts.com
 * Utils
 */

public class Utils {

    private static float density = 1;

    static {
        density = App.applicationContext.getResources().getDisplayMetrics().density;
    }

    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }

    public static void showListDialog(Context context, String[] items, DialogInterface.OnClickListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.dialog_select_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setAdapter(adapter, listener);
        builder.create().show();
    }

}
