package com.omejibika.endoscope

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.grid_items.*

/**
 * A simple [Fragment] subclass.
 * Use the [UploadDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadDialogFragment : DialogFragment() {

    /** リスナー **/
    private var listener: OnUploadDialogListener? = null

    /**
     * ボタン押下インターフェース
     */
    interface OnUploadDialogListener {
        /**
         * 選択イベント
         */
        fun sendUploadResultCode(intent: Intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)

        this.listener = activity as OnUploadDialogListener
        val i = LayoutInflater.from(activity)
        val view: View? = i.inflate(R.layout.fragment_upload_dialog, container, false)

        /*** プリファレンスから保存されている文字列を取得 ***/
        val pref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val host = pref?.getString("host", "")
        val port = pref?.getString("port", "")
        val ssid = pref?.getString("ssid", "")
        val hostText = view?.findViewById<TextView>(R.id.HostText)
        val portText = view?.findViewById<TextView>(R.id.PortText)
        val ssidText = view?.findViewById<TextView>(R.id.SSIDText)
        hostText?.text = host
        portText?.text = port
        ssidText?.text = ssid

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_title_confirm)
        builder.setView(view)
        builder.setPositiveButton(R.string.ok) {dialog, id ->
            var inputID = view?.findViewById<TextView>(R.id.IDText)
            if (inputID?.text.toString() == "") {
                return@setPositiveButton
            }
            var intent = Intent()
            intent.putExtra("uploadresult", true)
            intent.putExtra("id", inputID?.text.toString())
            this.listener?.sendUploadResultCode(intent)
        }
        builder.setNegativeButton(R.string.cancel) {dialog, id ->
            var inputID = view?.findViewById<TextView>(R.id.IDText)
            var intent = Intent()
            intent.putExtra("uploadresult", false)
            intent.putExtra("id", inputID?.text)
            this.listener?.sendUploadResultCode(intent)
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            this.listener= activity as OnUploadDialogListener
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun setOnUploadDialogListener(listener: OnUploadDialogListener) {
        this.listener = listener
    }
}
