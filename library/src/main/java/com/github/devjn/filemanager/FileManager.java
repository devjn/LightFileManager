/*
 * Copyright 2017 devjn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.devjn.filemanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * FileManager
 */
final public class FileManager {

    private static volatile List<ResultCallback> callbacks = new ArrayList<>();
    private static volatile Options options = null;

    public static Options with(FragmentActivity activity) {
        return new Options(new ActivityWrapper(activity));
    }

    public static Options with(Fragment fragment) {
        return new Options(new FragmentWrapper(fragment));
    }

    static void deliverResult(String file) {
        for (ResultCallback callback : callbacks) {
            callback.onResult(file);
        }
        callbacks.clear();
        options = null;
    }

    static Options getOptions() {
        Options localInstance = options;
        if (localInstance == null) {
            synchronized (FileManager.class) {
                localInstance = options;
            }
        }
        return localInstance;
    }

    interface Wrapper {

        void startActivity(Intent intent);

        void startActivityForResult(Intent intent, int requestCode);

        FragmentManager getFragmentManager();

        Context getContext();

    }

    public final static class Options {

        private final Wrapper wrapper;

        private String mimeType = null;
        private boolean showHidden = false;
        private ResultCallback callback = null;

        public Options(Wrapper wrapper) {
            this.wrapper = wrapper;
        }

        public Options setContentType(String mimetype) {
            this.mimeType = mimetype;
            return this;
        }

        public Options showHidden(boolean show) {
            this.showHidden = show;
            return this;
        }

        public void startFileManager() {
            Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            wrapper.startActivity(intent);
            options = null;
        }

        public void startFileManager(int requestCode) {
            options = this;
            Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            wrapper.startActivityForResult(intent, requestCode);
        }

        public void startFileManager(ResultCallback callback) {
            this.callback = callback;
            options = this;
            Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            wrapper.startActivity(intent);
        }

        public void showDialogFileManager(ResultCallback callback) {
            this.callback = callback;
            options = this;
            callbacks.add(callback);
            DialogFragment fragment = FileManagerDialog.newInstance(mimeType, showHidden);
            fragment.show(wrapper.getFragmentManager(), "FILE_MANAGER");
        }


        ResultCallback getCallback() {
            return callback;
        }

        String getMimeType() {
            return mimeType;
        }

        boolean isShowHidden() {
            return showHidden;
        }

        public static class Manager {

        }

    }

    public interface ResultCallback {
        void onResult(String path);
    }


    final static class ActivityWrapper implements Wrapper {

        FragmentActivity activity;

        public ActivityWrapper(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void startActivity(Intent intent) {
            activity.startActivity(intent);
        }

        @Override
        public void startActivityForResult(Intent intent, int requestCode) {
            activity.startActivityForResult(intent, requestCode);
        }

        @Override
        public FragmentManager getFragmentManager() {
            return activity.getSupportFragmentManager();
        }

        @Override
        public Context getContext() {
            return activity;
        }
    }

    final static class FragmentWrapper implements Wrapper {

        Fragment fragment;

        public FragmentWrapper(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void startActivity(Intent intent) {
            fragment.startActivity(intent);
        }

        @Override
        public void startActivityForResult(Intent intent, int requestCode) {
            fragment.startActivityForResult(intent, requestCode);
        }

        @Override
        public FragmentManager getFragmentManager() {
            return fragment.getFragmentManager();
        }

        @Override
        public Context getContext() {
            return fragment.getContext();
        }
    }

}
