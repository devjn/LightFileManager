package com.github.devjn.filemanager;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.devjn.filemanager.ViewStyle.*;

/**
 * Created by @author Jahongir on 24-Jan-18
 * devjn@jn-arts.com
 * ViewStyle
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AUTO, DEFAULT_GRID, DEFAULT_LIST})
public @interface ViewStyle {

    int AUTO = 0;

    int DEFAULT_GRID = 10;
    int DEFAULT_LIST = 20;

}
