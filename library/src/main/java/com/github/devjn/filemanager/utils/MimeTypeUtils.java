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

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.github.devjn.filemanager.FileData;
import com.github.devjn.filemanager.FileManager;
import com.github.devjn.filemanager.R;

import java.util.HashMap;

/**
 * Created by @author Jahongir on 21 Mar 2017
 * <p>
 * MimeTypeUtils.java
 */

public class MimeTypeUtils {

    private static final HashMap<String, String> types = new HashMap<>();
    private static final HashMap<String, Integer> icons = new HashMap<>();

    static {
        init();
    }

    private static void init() {
        types.put("323", "text/h323");

        types.put("3g2", "video/3gpp2");
        types.put("3gp", "video/3gpp");
        types.put("3gp2", "video/3gpp2");
        types.put("3gpp", "video/3gpp");
        types.put("avi", "video/x-msvideo");
        types.put("flv", "video/x-flv");
        types.put("mp2", "video/mpeg");
        types.put("mp2v", "video/mpeg");
        types.put("mp4", "video/mp4");
        types.put("mp4v", "video/mp4");
        types.put("mpa", "video/mpeg");
        types.put("mpe", "video/mpeg");
        types.put("mpeg", "video/mpeg");
        types.put("mpg", "video/mpeg");
        types.put("mpv2", "video/mpeg");
//        types.put("mqv", "video/quicktime");
//        types.put("qt", "video/quicktime");
        types.put("webm", "video/webm");

        types.put("aa", "audio/audible");
        types.put("AAC", "audio/aac");
        types.put("ac3", "audio/ac3");
        types.put("aif", "audio/aiff");
        types.put("aifc", "audio/aiff");
        types.put("aiff", "audio/aiff");
        types.put("flac", "audio/flac");
        types.put("mp3", "audio/mpeg");
        types.put("oga", "audio/ogg");
        types.put("ogg", "audio/ogg");
        types.put("wma", "audio/x-ms-wma");


        types.put("gif", "image/gif");
        types.put("jpe", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("jpg", "image/jpeg");
        types.put("png", "image/png");
        types.put("svg", "image/svg+xml");

        types.put("webp", "image/webp");
        types.put("qti", "image/x-quicktime");
        types.put("qtif", "image/x-quicktime");


        types.put("doc", "application/msword");
        types.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
        types.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        types.put("dot", "application/msword");
        types.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
        types.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");


        types.put("html", "text/html");

        types.put("json", "application/json");


        types.put("ogv", "video/ogg");


        types.put("ppa", "application/vnd.ms-powerpoint");
        types.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        types.put("ppm", "image/x-portable-pixmap");
        types.put("pps", "application/vnd.ms-powerpoint");
        types.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        types.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        types.put("ppt", "application/vnd.ms-powerpoint");
        types.put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        types.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");


        types.put("swf", "application/x-shockwave-flash");

        types.put("txt", "text/plain");


        icons.put("doc", R.drawable.ic_file_doc);
        icons.put("docx", R.drawable.ic_file_doc);
        icons.put("pdf", R.drawable.ic_file_pdf);
        icons.put("txt", R.drawable.ic_file_txt);
        icons.put("html", R.drawable.ic_file_html);
        icons.put("zip", R.drawable.ic_file_zip);
        icons.put("audio", R.drawable.ic_file_audio);

    }

    public static String getType(String filename) {
        return types.get(filename);
    }

    public static boolean isImage(@NonNull String extension) {
        String type = getType(extension);
        return type != null && type.startsWith("image");
    }

    public static boolean isVideo(@NonNull String extension) {
        String type = getType(extension);
        return type != null && type.startsWith("video");
    }

    public static boolean isAudio(@NonNull String extension) {
        String type = getType(extension);
        return type != null && type.startsWith("audio");
    }

    public static boolean isImageFile(@NonNull String filename) {
        return getType(Utils.fileExt(filename)).startsWith("image");
    }

    public static boolean isVideoFile(@NonNull String filename) {
        return getType(Utils.fileExt(filename)).startsWith("video");
    }


    public static void addIcon(@NonNull String extension, @DrawableRes int resIcon) {
        icons.put(extension, resIcon);
    }

    public static void setIconForFile(ImageView imageView, FileData fileData) {
        String ext = fileData.getExtension();
        final int drawable;
        if (fileData.isFolder())
            drawable = R.drawable.ic_folder;
        else if (fileData.hasExtension()) {
            if (MimeTypeUtils.isImage(ext) || MimeTypeUtils.isVideo(ext)) {
                FileManager.getInstance().getConfig().getImageLoader().load(imageView, fileData);
                return;
            } else if (MimeTypeUtils.isAudio(ext))
                drawable = R.drawable.ic_file_audio;
            else drawable = MapCompat.getOrDefault(MimeTypeUtils.icons, ext, R.drawable.ic_file);
        } else drawable = R.drawable.ic_file;

        imageView.setImageResource(drawable);
    }

}
