package com.example.animeshowtime

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmDialogFragment : DialogFragment() {
    // Use this instance of the interface to deliver action events
    private lateinit var listener: NoticeDialogListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animeFragmentSize = activity?.supportFragmentManager?.fragments?.size
        listener = animeFragmentSize?.let { activity?.supportFragmentManager?.fragments?.get(it-2) } as AnimeFragment
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Build the dialog and set up the button click handlers
            val builder = AlertDialog.Builder(it)

            builder.setMessage(R.string.dialog_remove_from_list)
                .setPositiveButton(R.string.confirm_dialog_remove_from_list
                ) { _, _ ->
                    // Send the positive button event back to the host activity
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(R.string.cancel_dialog_remove_from_list
                ) { _, _ ->
                    // Send the negative button event back to the host activity
                    listener.onDialogNegativeClick(this)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TYPE = "type"
        fun newInstance(id : Int, type: Int) =
            ConfirmDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                    putInt(ARG_TYPE, type)

                }
            }
    }
}