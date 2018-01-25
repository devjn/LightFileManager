package com.github.devjn.simplefilemanager

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.widget.EditText


/**
 * Created by @author Jahongir on 24-Jan-18
 * devjn@jn-arts.com
 * DialogNewItem
 */
class DialogNewItem : DialogFragment() {


    private var type: Int = 0
    private lateinit var path: String
    private lateinit var editText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            type = it.getInt("what")
            path = it?.getString("path")
        }

        editText = EditText(context!!)
        editText.setPadding(Utils.dp(16f), Utils.dp(16f), Utils.dp(16f), Utils.dp(16f))
        editText.setEms(10)

        return AlertDialog.Builder(activity!!)
                // set Dialog Title
                .setTitle(R.string.action_new)
                // Set Dialog Message
                .setView(editText)
                // positive button
                .setPositiveButton(android.R.string.ok, { dialog, which ->
                    val name = editText.text.toString()
                    com.github.devjn.filemanager.DataLoader.getInstance().createFile(activity, path, name, type)
                    dismiss()
                })
                // negative button
                .setNegativeButton(android.R.string.cancel, { dialog, which -> dismiss() })
                .create()
    }


    companion object {

        fun show(fragmentManager: FragmentManager, what: Int, path: String) {
            val dialog = newInstance(what, path)
            dialog.show(fragmentManager, "DialogNewItem")
        }

        fun newInstance(what: Int, path: String): DialogNewItem {
            val args = Bundle()
            args.putInt("what", what)
            args.putString("path", path)
            val fragment = DialogNewItem()
            fragment.setArguments(args)
            return fragment
        }

    }

}