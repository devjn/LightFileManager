package com.github.devjn.simplefilemanager.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.github.devjn.simplefilemanager.R;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * DirectoryPrefernces
 */

public class DirectoryPrefernces extends DialogPreference {

    private String directory;

    /**
     * Resource of the dialog layout
     */
    private int mDialogLayoutResId = R.layout.pref_dialog_directory;

    public DirectoryPrefernces(Context context) {
        this(context, null);
    }

    public DirectoryPrefernces(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public DirectoryPrefernces(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public DirectoryPrefernces(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String folder) {
        this.directory = folder;

        // Save to SharedPreference
        persistString(folder);
    }

    /**
     * Called when a Preference is being inflated and the default value attribute needs to be read
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String s = a.getString(index);
        if(s == null)
            s = Environment.getExternalStorageDirectory().getAbsolutePath();
        return s;
    }

    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }


    /**
     * Implement this to set the initial value of the Preference.
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setDirectory(restorePersistedValue ?
                getPersistedString(directory) : (String) defaultValue);
    }

}
