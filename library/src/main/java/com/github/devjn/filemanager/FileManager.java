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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * FileManager
 */
@SuppressWarnings("ALL")
final public class FileManager {

    private static volatile List<ResultCallback> callbacks = new ArrayList<>();
    private static volatile Config config = new Config();
    private static volatile Options options = null;


    public static Config init(Context context) {
        FileManagerFileProvider.updateAuthority(context.getApplicationContext().getPackageName());
        return config;
    }

    public static Config init(Context context, Config config) {
        FileManager.config = config;
        context.getApplicationContext().getPackageName();
        return config;
    }


    public static Options.RequestManager with(@NonNull FragmentActivity activity) {
        return Options.newRequest(new ActivityWrapper(activity));
    }

    public static Options.RequestManager with(@NonNull Fragment fragment) {
        return Options.newRequest(new FragmentWrapper(fragment));
    }

    public static void setIconForExtension(@NonNull String extension, @DrawableRes int resIcon) {
        MimeTypeUtils.addIcon(extension, resIcon);
    }

    @NonNull
    public static Config getConfig() {
        return FileManager.config;
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

        private Options(Wrapper wrapper) {
            this.wrapper = wrapper;
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


        public static RequestManager newRequest(Wrapper wrapper) {
            return new Options(wrapper).new RequestManager();
        }

        public class RequestManager {

            /**
             * Filter content type the file manager will display.
             * For example to show only images use "image/*" or choose from {@link Filter}
             *
             * @param mimetype if null or empty all files are displayed
             */
            public RequestManager setContentType(String mimetype) {
                Options.this.mimeType = mimetype;
                return this;
            }

            /**
             * Whether file manager should display hidden files (the ones starting with '.')
             */
            public RequestManager showHidden(boolean show) {
                Options.this.showHidden = show;
                return this;
            }

            public void startFileManager() {
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivity(intent);
                options = null;
            }

            public void startFileManager(int requestCode) {
                options = Options.this;
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivityForResult(intent, requestCode);
            }

            public void startFileManager(ResultCallback callback) {
                options = Options.this;
                Options.this.callback = callback;
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivity(intent);
            }


            /**
             * Displays DialogFragment
             *
             * @param callback reslut to be delivered to
             */
            public void showDialogFileManager(ResultCallback callback) {
                options = Options.this;
                callbacks.add(callback);
                Options.this.callback = callback;
                DialogFragment fragment = FileManagerDialog.newInstance(mimeType, showHidden);
                fragment.show(wrapper.getFragmentManager(), "FILE_MANAGER");
            }

        }

    }

    public interface ResultCallback {
        /**
         * @param path path to file returned by {@link File#getAbsolutePath()}
         */
        void onResult(String path);
    }

    public interface Filter {
        String VIDEO = "video/*";
        String IMAGE = "image/*";
        String SOUND = "sound/*";
        String TEXT = "text/*";
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
