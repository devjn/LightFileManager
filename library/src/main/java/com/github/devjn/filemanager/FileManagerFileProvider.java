package com.github.devjn.filemanager;

import android.support.v4.content.FileProvider;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * FileManagerFileProvider
 */
public class FileManagerFileProvider extends FileProvider {

    private static String FILES_AUTHORITY;

    public static void updateAuthority(String packageName) {
        FILES_AUTHORITY = packageName  + ".filemanager.fileprovider";
    }

    public static String getAuthority(){
        return FILES_AUTHORITY;
    };

}
