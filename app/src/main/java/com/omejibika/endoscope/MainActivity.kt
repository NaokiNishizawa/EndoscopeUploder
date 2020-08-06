package com.omejibika.endoscope

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.SocketException

class MainActivity : AppCompatActivity(), FolderSelectDialogFragment.OnFolderSelectDialogListener, ConfirmDialogFragment.OnConfirmDialogListener, UploadDialogFragment.OnUploadDialogListener {

    private val REQUEST_PERMISSION = 1000
    private val REQUEST_SETTING = 2000

    private var adapter: GridAdapter? = null
    private var confirmResult: Boolean = false
    private var uploadResult: Boolean = false
    private var inputID: String? = null
    //private var dataList: ArrayList<GridDataList>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /** Android 6, API 23以上でパーミッションの確認 **/
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission()
        }

        /** RecyclerViewをセット **/
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        var dataList = createDataList()
        if (dataList.size == 0) {
            //Common.showDialog(this, getString(R.string.dialog_title_info), getString(R.string.file_not_found_message))
            val toast = Toast.makeText(this, getString(R.string.file_not_found_message), Toast.LENGTH_SHORT)
            toast.show()
            return
        }

        /*** ファイルの更新日時でソート（降順） ***/
        dataList.sortByDescending { it.lastModified }

        adapter = GridAdapter(dataList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(GridItemDecoration(resources.getDimension(R.dimen.carousel_margin).toInt()))
        recyclerView.setHasFixedSize(false)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.itemAnimator?.changeDuration = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_SETTING -> {
                reload()
            }
        }
    }

    /**
     * パーミッションの確認
     */
    private fun checkPermission(): Unit {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 既に許可している
        } else {
            requestLocationPermission()
        }
    }

    /**
     * 許可を求める
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            val toast = Toast.makeText(this, "許可してください", Toast.LENGTH_SHORT)
            toast.show()
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }
    }

    /**
     * 結果の受け取り
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 使用が許可された
                reload()
            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    /**
     * オプションメニューを作成
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /** Inflate the menu; this adds items to the action bar if it is present. **/
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * メニューアイテムをクリックしたときの動作
     * @param item 選択したメニュー
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_selectfolder -> {
                /*** フォルダ選択ダイアログを表示 ***/
                val dialog = FolderSelectDialogFragment()
                dialog.show(supportFragmentManager, "simple")
                return true
            }
            R.id.action_upload -> {
                /*** 選択されたファイルをアップロード ***/
                val dialog = UploadDialogFragment()
                dialog.show(supportFragmentManager, "upload")
                return true
            }
            R.id.action_delete -> {
                /*** 確認ダイアログの表示 ***/
                val dialog = ConfirmDialogFragment()
                dialog.show(supportFragmentManager, "delete")
                return true
            }
            R.id.action_settings -> {
                /*** 設定画面へ遷移 ***/
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, REQUEST_SETTING)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickFolderSelect(intent: Intent) {
        val subDirCnt = intent.getIntExtra("subdircnt", 0)
        if (subDirCnt == 0) {
            reload()
        } else if (subDirCnt > 0){
            /*** フォルダ選択ダイアログを表示 ***/
            val dialog = FolderSelectDialogFragment()
            dialog.show(supportFragmentManager, "simple")
        }
    }

    private fun createDataList(): ArrayList<GridDataList> {
        val gridDataList = mutableListOf<GridDataList>()

        /** 指定したフォルダ内のファイルを取得 **/
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        var storage = pref?.getString("storage", "1")
        var directoryPath = pref?.getString("directorypath", "")
        if (directoryPath.equals("")) {
            if (storage.equals("1")) {
                var pathList = Common.getSdCardFilesDirPathListForLollipop(this)
                if(pathList.isEmpty()) {
                    // 内部ストレージ
                    directoryPath = Environment.getExternalStorageDirectory().path
                } else {
                    // SDカード
                    directoryPath = Common.getSdCardFilesDirPathListForLollipop(this)[0]
                }
            } else {
                // 内部ストレージ
                directoryPath = Environment.getExternalStorageDirectory().path
            }
            val editor = pref.edit()
            editor.putString("directorypath", directoryPath)
            editor.apply()
        }

        var dir = File(directoryPath)
        if (!dir.exists()) {
            // フォルダが見つからない場合
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_folder_not_found))
            val toast = Toast.makeText(this, getString(R.string.error_folder_not_found), Toast.LENGTH_SHORT)
            toast.show()
        } else {
            var files = dir.listFiles()
            if (files != null) {
                files.forEach {
                    if (it.isFile && (it.name.toLowerCase().endsWith(".png") || it.name.toLowerCase().endsWith(".jpg") || it.name.toLowerCase().endsWith(".jpeg") )) {
                        val lastModified = it.lastModified()
                        val gridData: GridDataList = GridDataList(it.absolutePath, it.name, lastModified)
                        gridDataList.add(gridData)
                    }
                }
            }
        }

        return gridDataList as ArrayList<GridDataList>
    }

    override fun sendConfirmResultCode(intent: Intent) {
        confirmResult = intent.getBooleanExtra("confirmresult", false)

        /*** 選択されたファイルを削除 ***/
        if (confirmResult) {
            fileDelete()
            reload()
        }
    }

    override fun sendUploadResultCode(intent: Intent) {
        uploadResult = intent.getBooleanExtra("uploadresult", false)
        inputID = intent.getStringExtra("id")

        if (!SSIDManager.checkConfiguredNetworks(this)) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_ssid_not_registered))
            val toast = Toast.makeText(this, getString(R.string.error_ssid_not_registered), Toast.LENGTH_SHORT)
            toast.show()
            return
        }

        /*** 選択されたファイルをUpload ***/
        if (uploadResult) {
            FileUploadAsyncTask().execute()
        }
    }

    /**
     * 選択されたファイルの数をカウントする
     */
    /*private fun selectFileCount(): Int {
        var count = 0
        for (data in adapter!!.dataList) {
        //for (data in dataList!!)
            if (data.isSelected) {
                count++
            }
        }

        return count
    }*/

    /**
     * 選択されたファイルを削除
     */
    private fun fileDelete() {
        //if (selectFileCount() > 0) {
            for (data in adapter!!.dataList) {
                if (data.isSelected) {
                    try {
                        val file = File(data.image)
                        file.delete()
                    } catch (e: Exception) {
                        //Common.showDialog(this, getString(R.string.dialog_title_error), e.message.toString())
                        val toast = Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
            }
        //} else {
        //    Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.file_not_select_message))
        //}
    }

    /**
     * 選択されたファイルをUpload
     */
    private fun fileUpload(id: Int): String {
        /** プリファレンスからFTPの接続情報を取得 **/
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val host = pref.getString("host", "")
        val port = pref.getString("port", "")
        val userName = pref.getString("username", "")
        val password = pref.getString("password", "")
        val savePath = pref.getString("savepath", "")
        val ssid = pref.getString("ssid", "")

        var resultStr = ""
        try {
            resultStr = validate(host, port, userName, password, ssid)
            if (resultStr.isBlank()) {
                /*** 現在接続されているWi-FiのSSIDを取得 ***/
                val currentSSID = SSIDManager.getSSID(this)
                if (!ssid.equals(currentSSID)) {
                    /*** プリファレンスに設定されているSSIDに接続 ***/
                    if (!SSIDManager.reconnectPreferenceSSID(this, ssid)) {
                        //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_ssid_not_registered))
                        //val toast = Toast.makeText(this, getString(R.string.error_ssid_not_registered), Toast.LENGTH_SHORT)
                        //toast.show()
                        resultStr = getString(R.string.error_ssid_not_registered)
                        return resultStr
                    }
                }

                /*** FTPで接続 ***/
                val ftpClient = connectFTP(host, port, userName, password, savePath)

                /*** ファイルをアップロード ***/
                for (data in adapter!!.dataList) {
                    if (data.isSelected) {
                        uploadFTP(ftpClient, id, data.image, data.filename)
                    }
                }

                /*** 完了処理 ***/
                ftpClient.disconnect()

                /*** 完了メッセージを出力 ***/
                //Common.showDialog(this, getString(R.string.dialog_title_info), getString(R.string.complete_message))
                //val toast = Toast.makeText(this, getString(R.string.complete_message), Toast.LENGTH_SHORT)
                //toast.show()
                resultStr = getString(R.string.complete_message)
            }
        } catch (e: Exception) {
            resultStr = e.toString()
            return resultStr
        }

        return resultStr
    }

    /**
     * FTPで接続
     * @param host ホスト名
     * @param port ポート
     * @param userName ユーザー名
     * @param password パスワード
     * @param savePath (サーバー側)保存先のパス
     * @return FTPClient
     */
    private fun connectFTP(host: String?, port: String?, userName: String?, password: String?, savePath: String?): FTPClient {
        val ftpClient = FTPClient()
        val timeout = 100 * 1000    // 100秒

        /*** タイムアウト設定 ***/
        ftpClient.defaultTimeout = timeout

        /*** 接続 ***/
        ftpClient.connect(host, port!!.toInt())
        if (!FTPReply.isPositiveCompletion(ftpClient.replyCode)) {
            // 接続エラー
            throw Exception(StringBuffer("FTP接続エラー Code=").append(ftpClient.replyCode).toString())
        }

        /*** ソケットタイムアウト設定 ***/
        ftpClient.soTimeout = timeout

        /*** ログイン ***/
        if (!ftpClient.login(userName, password)) {
            // 認証エラー
            throw Exception(StringBuffer("FTP認証エラー Code=").append(ftpClient.replyCode).toString())
        }

        /*** ファイル転送モード設定 ***/
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        ftpClient.enterLocalPassiveMode()
        ftpClient.setDataTimeout(timeout)

        /*** ディレクトリ移動 ***/
        if (!ftpClient.changeWorkingDirectory(savePath)) {
            throw Exception(StringBuffer("Failed changeWorkingDirectory() ").append(savePath).append(" Code=").append(ftpClient.replyCode).toString())
        }

        return ftpClient
    }

    /**
     * FTPでファイルをアップロード
     * @param ftpClient FTPClient
     * @param id ID
     * @param ftpFile アップロードするファイル
     */
    private fun uploadFTP(ftpClient: FTPClient, id: Int, filePath: String, fileName: String) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        var fis: FileInputStream = FileInputStream(filePath)
        ftpClient.storeFile(id.toString() + "_" + fileName, fis)
        fis.close()
    }

    /**
     * 入力チェック
     * @param host ホスト名
     * @param port ポート
     * @param userName ユーザー名
     * @param password パスワード
     *
     * @return 空文字 or エラー
     */
    private fun validate(host: String?, port: String?, userName: String?, password: String?, ssid: String?): String {
        /*** ホスト名 ***/
        if (host.equals("")) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_host_not_defined))
            //val toast = Toast.makeText(this, getString(R.string.error_host_not_defined), Toast.LENGTH_SHORT)
            //toast.show()
            return getString(R.string.error_host_not_defined)
        }

        /*** ポート ***/
        if (port.equals("")) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_port_not_defined))
            //val toast = Toast.makeText(this, getString(R.string.error_port_not_defined), Toast.LENGTH_SHORT)
            //toast.show()
            return getString(R.string.error_port_not_defined)
        }

        /*** ユーザー名 ***/
        if (userName.equals("")) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_username_not_defined))
            //val toast = Toast.makeText(this, getString(R.string.error_username_not_defined), Toast.LENGTH_SHORT)
            //toast.show()
            return getString(R.string.error_username_not_defined)
        }

        /*** パスワード ***/
        if (password.equals("")) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_password_not_defined))
            //val toast = Toast.makeText(this, getString(R.string.error_password_not_defined), Toast.LENGTH_SHORT)
            //toast.show()
            return getString(R.string.error_password_not_defined)
        }

        /*** SSID ***/
        if (ssid.equals("")) {
            //Common.showDialog(this, getString(R.string.dialog_title_error), getString(R.string.error_ssid_not_defined))
            //val toast = Toast.makeText(this, getString(R.string.error_ssid_not_defined), Toast.LENGTH_SHORT)
            //toast.show()
            return getString(R.string.error_ssid_not_defined)
        }

        return ""
    }

    private fun reload() {
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    inner class FileUploadAsyncTask: AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            return fileUpload(inputID!!.toInt())
        }

        override fun onPostExecute(result: String?) {
            //super.onPostExecute(result)
            //　結果をToastで表示
            Log.d("debug", "call onPostExecute result $result")
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
        }
    }
}
