package com.omejibika.endoscope

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 * Use the [ConfirmDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConfirmDialogFragment : DialogFragment() {

    /** リスナー **/
    private var listener: OnConfirmDialogListener? = null

    /**
     * ボタン押下インターフェース
     */
    interface OnConfirmDialogListener {
        /**
         * 選択イベント
         */
        fun sendConfirmResultCode(intent: Intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.listener = activity as OnConfirmDialogListener
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_title_confirm)
        builder.setMessage(R.string.confirm_dialog_message)
        builder.setPositiveButton(R.string.ok) {dialog, id ->
            var intent = Intent()
            intent.putExtra("confirmresult", true)
            this.listener?.sendConfirmResultCode(intent)
        }
        builder.setNegativeButton(R.string.cancel) {dialog, id ->
            var intent = Intent()
            intent.putExtra("confirmresult", false)
            this.listener?.sendConfirmResultCode(intent)
        }

        return builder.create()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            this.listener= activity as OnConfirmDialogListener
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun setOnConfirmDialogListener(listener: ConfirmDialogFragment.OnConfirmDialogListener) {
        this.listener = listener
    }

}
