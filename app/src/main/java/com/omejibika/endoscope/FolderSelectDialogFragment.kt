package com.omejibika.endoscope

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 * Use the [FolderSelectDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FolderSelectDialogFragment : DialogFragment() {

    /** リスナー **/
    private var listener: OnFolderSelectDialogListener? = null
    /** ファイル情報 **/
    private var fileData: File? = null
    /** 表示中のファイル情報リスト  */
    private var viewFileDataList: List<File>? = null

    /**
     * ボタン押下インターフェース
     */
    interface OnFolderSelectDialogListener {
        /**
         * 選択イベント
         * @param file ファイル
         */
        fun onClickFolderSelect(intent: Intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)

        this.listener = activity as OnFolderSelectDialogListener

        val pref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        var directoryPath = pref?.getString("directorypath", "")
        var storage = pref?.getString("storage", "1")
        if (directoryPath.equals("")) {
            if (storage.equals("1")) {
                // SDカード
                directoryPath = Common.getSdCardFilesDirPathListForLollipop(requireActivity().applicationContext)[0]
            } else {
                // 内部ストレージ
                directoryPath = Environment.getExternalStorageDirectory().path
            }
            val editor = pref?.edit()
            editor?.putString("directorypath", directoryPath)
            editor?.apply()
        }

        return show(directoryPath, 1)
    }

    private fun show(dirPath: String?, status: Int): Dialog {
        // ファイル情報
        fileData = File(dirPath)
        // ファイルリスト
        var fileArray = fileData?.listFiles()
        var nameList = Array<String>(1) {""}
        var dirCnt = 0

        if (fileArray != null) {
            // フォルダの数をチェック
            for (file in fileArray) {
                if (file.isDirectory) {
                    dirCnt++
                }
            }

            if (dirCnt > 0) {
                nameList = Array<String>(dirCnt) {""}
                dirCnt = 0
                // ファイル情報マップ
                var map: Map<String, File> = HashMap<String, File>()
                for (file in fileArray) {
                    if (file.isDirectory) {
                        nameList[dirCnt] = file.name
                        map.plus(Pair(file.name, file))
                        dirCnt++
                    }
                }

                // ソート
                nameList.sort();

                // ファイル情報リスト
                this.viewFileDataList = ArrayList<File>()

                for (i in nameList.indices) {
                    map[nameList[i]]?.let { (this.viewFileDataList as ArrayList<File>).add(it) }
                }
            }

        }

        var builder = AlertDialog.Builder(activity)
        builder.setTitle(dirPath)
        builder.setIcon(R.drawable.icon_6m_32)
        if (dirCnt > 0) {
            builder.setItems(nameList as Array<String>, { _, which ->
                /*** 選択されたパスをSharedPreferenceに保存する ***/
                var dirPathNew = dirPath?.substring(0, dirPath.length)!!.plus("/").plus(nameList[which])
                saveNewPath(dirPathNew)

                /*** サブフォルダの数をカウントする ***/
                val subDirCnt = countSubDir(dirPathNew)
                var intent = Intent()
                intent.putExtra("subdircnt", subDirCnt)
                this.listener?.onClickFolderSelect(intent)
            })
        }
        builder.setPositiveButton(R.string.decide) {dialog, id ->
            // 決定
            var intent = Intent()
            intent.putExtra("subdircnt", 0)
            this.listener?.onClickFolderSelect(intent)
        }
        builder.setNeutralButton(R.string.upper) {dialog, id ->
            // 上へ
            /*** 各ストレージのルートフォルダを取得 ***/
            var rootPath: String?
            val pref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val storage = pref?.getString("storage", "0")
            if (storage.equals("1")) {
                var pathList = Common.getSdCardFilesDirPathListForLollipop(requireActivity().applicationContext)
                if(pathList.isEmpty()) {
                    rootPath = Environment.getExternalStorageDirectory().path
                } else {
                    rootPath = Common.getSdCardFilesDirPathListForLollipop(requireActivity().applicationContext)[0]
                }
            } else {
                rootPath = Environment.getExternalStorageDirectory().path
            }

            /*** 一つ上のフォルダが存在するか確認 ***/
            var intent = Intent()
            var dirPathNew = dirPath
            if (!rootPath.equals(dirPath)) {
                // 存在する場合一つ上へ
                dirPathNew = dirPath?.substring(0, dirPath.length)
                dirPathNew = dirPathNew?.substring(0, dirPathNew?.lastIndexOf("/"))
                saveNewPath(dirPathNew!!)
            }
            val subDirCnt = countSubDir(dirPathNew!!)
            intent.putExtra("subdircnt", subDirCnt)
            this.listener?.onClickFolderSelect(intent)
        }
        builder.setNegativeButton(R.string.cancel) {dialog, id ->
            // キャンセル
            var intent = Intent()
            intent.putExtra("subdircnt", -1)
            this.listener?.onClickFolderSelect(intent)
        }

        val dig = builder.create()
        try {
            dig.show()
        } catch (e: Exception) {
            e.message
        }

        return dig
        //return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            this.listener= activity as OnFolderSelectDialogListener
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun countSubDir(dirPath: String): Int {
        // ファイル情報
        fileData = File(dirPath)
        // ファイルリスト
        var fileArray = fileData?.listFiles()

        var dirCnt = 0
        if (fileArray != null) {
            // ファイル情報マップ
            for (file in fileArray) {
                if (file.isDirectory) {
                    dirCnt++
                }
            }
        }

        return dirCnt
    }

    fun setOnFolderSelectDialogListener(listener: OnFolderSelectDialogListener) {
        this.listener = listener
    }

    /**
     * 新しく設定されたパスをSharedPreferencesに保存する
     * @param dirPathNew 新しいパス
     */
    private fun saveNewPath(dirPathNew: String) {
        val pref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putString("directorypath", dirPathNew)
        editor?.apply()
    }
}
