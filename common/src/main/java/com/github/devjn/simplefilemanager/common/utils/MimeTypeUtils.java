package com.github.devjn.simplefilemanager.common.utils;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;

/**
 * Created by @author Jahongir on 21 Mar 2017
 *
 * MimeTypeUtils.java
 */

public class MimeTypeUtils {

    private static HashMap<String, String> types = new HashMap<>();

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

}
