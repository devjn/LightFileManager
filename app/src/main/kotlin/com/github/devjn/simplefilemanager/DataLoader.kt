package com.github.devjn.simplefilemanager

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * Loader
 */

object DataLoader {

    interface DataListener {
        fun onDataLoad(list: List<FileData>)
    }

    var listener : DataListener? = null

    fun loadData(folder: File) {
        val observe = Observable.fromCallable { fill(folder) }
        observe.subscribeOn(Schedulers.io())
                .flatMap({ unsorted -> Observable.fromIterable(unsorted) })
                .toSortedList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    listener?.onDataLoad(list)
                })
    }

    private fun fill(folder: File): List<FileData> {
        val files = folder.listFiles()
        val dataFiles = ArrayList<FileData>()
        for (file in files) {
            if (file.isDirectory) {
                val buf = file.listFiles()
                var num_item: Int
                if (buf != null) {
                    num_item = buf.size
                } else
                    num_item = 0

                dataFiles.add(FileData(file.name, file.absolutePath, true, num_item.toLong()))
            } else {
                dataFiles.add(FileData(file.name, file.absolutePath, false, file.length()))
            }
        }
        return dataFiles
    }

}
