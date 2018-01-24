package com.github.devjn.simplefilemanager

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.devjn.filemanager.FileManager
import com.github.devjn.filemanager.ViewStyle

/**
 * Created by @author Jahongir on 24-Feb-2018
 * devjn@jn-arts.com
 * DialogViewSettingsFragment
 */
class DialogViewSettingsFragment : DialogFragment() {

    private var listener: DismissListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_view_settings, container, false)

        val btnList = root.findViewById<View>(R.id.view_list)
        val btnGrid = root.findViewById<View>(R.id.view_grid)

        btnList.setOnClickListener {
            FileManager.getInstance().config.setViewStyle(ViewStyle.DEFAULT_LIST)
            dismiss()
        }
        btnGrid.setOnClickListener {
            FileManager.getInstance().config.setViewStyle(ViewStyle.DEFAULT_GRID)
            dismiss()
        }

        getDialog().setTitle("View");

        return root
    }

    override fun dismiss() {
        listener?.onDismiss()
        super.dismiss()
    }

    companion object {

        fun show(fragmentManager: FragmentManager) {
            val dialog = DialogViewSettingsFragment()
            dialog.show(fragmentManager, "DialogViewSettingsFragment")
        }

        fun show(fragmentManager: FragmentManager, dismissListener: DismissListener) {
            val dialog = DialogViewSettingsFragment()
            dialog.listener = dismissListener
            dialog.show(fragmentManager, "DialogViewSettingsFragment")
        }

    }

    interface DismissListener {
        fun onDismiss()
    }

}