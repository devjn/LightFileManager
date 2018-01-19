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

package com.github.devjn.filemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import com.github.devjn.filemanager.FileData;
import com.github.devjn.filemanager.FileManagerFileProvider;
import com.github.devjn.filemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

/**
 * Created by @author Jahongir on 25-Apr-17
 * devjn@jn-arts.com
 * IntentUtils
 */

public class IntentUtils {

    /**
     * @param context
     * @param fileData file to open
     * @return true if handler found, false otherwise
     */
    public static boolean openFile(@NonNull Context context, @NonNull FileData fileData) {
        File file = new File(fileData.getPath());
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        String mimeType = myMime.getMimeTypeFromExtension(Utils.fileExt(fileData.getPath().substring(1)));

        Uri openUri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                FileProvider.getUriForFile(context, FileManagerFileProvider.getAuthority(), file) :
                Uri.fromFile(file);
        newIntent.setDataAndType(openUri, mimeType);
        newIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        if (newIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(newIntent);
            return true;
        }
        return false;
    }

    public static void shareFile(Activity activity, FileData fileData) {
        File file = new File(fileData.getPath());
        Uri uriToShare = FileProvider.getUriForFile(
                activity, FileManagerFileProvider.getAuthority(), file);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(Utils.fileExt(fileData.getPath().substring(1)));
        Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType(mimeType)
                .setStream(uriToShare)
                .getIntent();
        shareIntent.setData(uriToShare);
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (shareIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share_via)));
        }
    }


    public static void shareFiles(Activity activity, List<FileData> datas) {
        ArrayList<Uri> files = new ArrayList<>();

        for (FileData data : datas) {
            if (data.isFolder()) continue;
            File file = new File(data.getPath());
            Uri uri = FileProvider.getUriForFile(activity, FileManagerFileProvider.getAuthority(), file);
            files.add(uri);
        }
        Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("*/*")
                .getIntent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (shareIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share_via)));
        }
    }

}
