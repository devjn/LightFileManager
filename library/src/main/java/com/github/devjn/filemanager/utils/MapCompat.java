package com.github.devjn.filemanager.utils;

import android.support.annotation.NonNull;

import java.util.Map;


/**
 * Created by @author Jahongir on 19-Jan-18
 * devjn@jn-arts.com
 * MapCompat
 */
public class MapCompat {

    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
    }

}
