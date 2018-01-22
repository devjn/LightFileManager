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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;

import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * FileManager
 */
@SuppressWarnings("ALL")
public final class FileManager {

    private static final String TAG = FileManager.class.getSimpleName();
    private static volatile FileManager Instance = null;

    public static FileManager getInstance() {
        FileManager localInstance = Instance;
        if (localInstance == null) {
            Log.w(TAG, "localInstance is null");
            synchronized (FileManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileManager();
                }
            }
        }
        return localInstance;
    }

    private final SparseArray<RequestHolder> entities;
    private final GlobalConfig globalConfig;
    private final RequestHolder globalRequest;
    private final AtomicInteger ids;

    private FileManager() {
        this.entities = new SparseArray<>();
        this.globalConfig = new GlobalConfig();
        this.globalRequest = new RequestHolder(globalConfig.getOptions(), null);
        this.ids = new AtomicInteger();
    }

    public static void initialize(Context context) {
        FileManagerFileProvider.updateAuthority(context.getApplicationContext().getPackageName());
    }

//    public static void initialize(Context context, Config config) {
//        FileManager.globalConfig = config;
//        context.getApplicationContext().getPackageName();
//    }


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
    public GlobalConfig getConfig() {
        return this.globalConfig;
    }

    @NonNull
    public Options getOptions() {
        return this.globalConfig.getOptions();
    }


    static void deliverResult(int resultId, String file) {
        RequestHolder holder = FileManager.getInstance().entities.get(resultId);
        if (holder != null) {
            holder.callback.onResult(file);
        } else
            Log.wtf(TAG, "No ResultCallback for id : " + resultId + " , file \"" + file + "\" not delivered");
        FileManager.getInstance().entities.delete(resultId);
    }

    static RequestHolder getRequestHolder(int id) {
        if (id == -1) return FileManager.getInstance().globalRequest;
        return FileManager.getInstance().entities.get(id);
    }


    public static class GlobalConfig extends Config {

        final FileManager.Options options;

        private GlobalConfig() {
            this.options = new FileManager.Options(this);
        }

        public FileManager.Options getOptions() {
            return options;
        }

        /**
         * Whether file manager will display hidden files (the ones starting with '.')
         */
        public boolean isShowHidden() {
            return options.isShowHidden();
        }

        /**
         * Whether file manager should display hidden files (the ones starting with '.')
         */
        public GlobalConfig showHidden(boolean show) {
            this.options.showHidden = show;
            return this;
        }

        @Override
        public GlobalConfig setCustomImageLoader(ImageLoader loader) {
            return (GlobalConfig) super.setCustomImageLoader(loader);
        }

        @Override
        public GlobalConfig setDefaultFolder(String folder) {
            return (GlobalConfig) super.setDefaultFolder(folder);
        }

        @Override
        public GlobalConfig setShowFolderCount(boolean show) {
            return (GlobalConfig) super.setShowFolderCount(show);
        }

    }


    public final static class Options {

        private transient final Wrapper wrapper;
        private final Config config;
        private final int id;

        private String mimeType = null;
        private boolean showHidden = false;

        private Options(Wrapper wrapper) {
            this.config = new Config(FileManager.getInstance().getConfig());
            this.id = FileManager.getInstance().ids.getAndIncrement();
            this.wrapper = wrapper;
        }

        Options(Config config) {
            this.config = config;
            this.wrapper = null;
            this.id = -1;
        }


        public int getId() {
            return id;
        }

        String getMimeType() {
            return mimeType;
        }

        boolean isShowHidden() {
            return showHidden;
        }

        Config getConfig() {
            return config;
        }


        public final static RequestManager newRequest(Wrapper wrapper) {
            return new Options(wrapper).new RequestManager();
        }

        public final class RequestManager {

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
                onStart(null);
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.putExtra(Config.EXTRA_ID, Options.this.id);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivity(intent);
            }

            public void startFileManager(int requestCode) {
                onStart(null);
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.putExtra(Config.EXTRA_ID, Options.this.id);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivityForResult(intent, requestCode);
            }

            public void startFileManager(ResultCallback callback) {
                onStart(callback);
                Intent intent = new Intent(wrapper.getContext(), FileManagerActivity.class);
                intent.putExtra(Config.EXTRA_ID, Options.this.id);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                wrapper.startActivity(intent);
            }


            /**
             * Displays DialogFragment
             *
             * @param callback reslut to be delivered to
             */
            public void showDialogFileManager(ResultCallback callback) {
                onStart(callback);
                DialogFragment fragment = FileManagerDialog.newInstance(Options.this.id);
                fragment.show(wrapper.getFragmentManager(), "FILE_MANAGER");
            }

            private void onStart(ResultCallback callback) {
                FileManager.getInstance().entities.put(Options.this.id, new RequestHolder(Options.this, callback));
            }

        }

    }


    final static class RequestHolder {
        final ResultCallback callback;
        final Options options;

        public RequestHolder(@NonNull Options options, @Nullable ResultCallback callback) {
            this.callback = callback;
            this.options = options;
        }

        public int getId() {
            return options.id;
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

    interface Wrapper {

        void startActivity(Intent intent);

        void startActivityForResult(Intent intent, int requestCode);

        FragmentManager getFragmentManager();

        Context getContext();

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
