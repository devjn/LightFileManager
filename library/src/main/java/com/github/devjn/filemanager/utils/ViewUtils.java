package com.github.devjn.filemanager.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

import com.github.devjn.filemanager.FileManager;
import com.github.devjn.filemanager.R;
import com.github.devjn.filemanager.ViewStyle;

/**
 * Created by @author Jahongir on 24-Jan-18
 * devjn@jn-arts.com
 * ViewUtils
 */

public final class ViewUtils {

    public static float density = 1;

    public static void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
    }

    public static GridLayoutManager getLayoutManagerForStyle(Context context, boolean isPortrait) {
        @ViewStyle int style = FileManager.getInstance().getConfig().getDisplayFileStyle();
        switch (style) {
            case ViewStyle.DEFAULT_GRID:
                return new GridLayoutManager(context, Utils.calculateNoOfColumns(context), GridLayoutManager.VERTICAL, false);
            case ViewStyle.DEFAULT_LIST:
                return new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false);
            case ViewStyle.AUTO:
            default:
                return new GridLayoutManager(context, isPortrait ? 1 : Utils.calculateNoOfColumns(context), GridLayoutManager.VERTICAL, false);
        }
    }

    public static int getViewForStyle(boolean isPortrait) {
        @ViewStyle int style = FileManager.getInstance().getConfig().getDisplayFileStyle();
        switch (style) {
            case ViewStyle.DEFAULT_GRID:
                return R.layout.grid_item;
            case ViewStyle.DEFAULT_LIST:
                return R.layout.list_item;
            case ViewStyle.AUTO:
            default:
                return isPortrait ? R.layout.list_item : R.layout.grid_item;
        }
    }

    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }

}
