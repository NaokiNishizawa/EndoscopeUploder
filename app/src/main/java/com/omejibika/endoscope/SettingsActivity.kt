package com.omejibika.endoscope

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        /*** ActionBarに戻るボタンを表示する ***/
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*** プリファレンスから保存されている文字列を取得 ***/
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val host = pref.getString("host", "")
        val port = pref.getString("port", "")
        val userName = pref.getString("username", "")
        val password = pref.getString("password", "")
        val remoteRootPath = pref.getString("savepath", "/home")
        val ssid = pref.getString("ssid", "")
        var storage = pref.getString("storage", "1")

        val hostText = findViewById<EditText>(R.id.HostText)
        val portText = findViewById<EditText>(R.id.PortText)
        val userNameText = findViewById<EditText>(R.id.UserNameText)
        val passwordText = findViewById<EditText>(R.id.PasswordText)
        val savePathText = findViewById<EditText>(R.id.SavePathText)
        val ssidText = findViewById<EditText>(R.id.SSIDText)
        val storageRadioGroup = findViewById<RadioGroup>(R.id.StorageRadioGroup)
        val sdCardRadio = findViewById<RadioButton>(R.id.SDCardRadio)
        val internalStorageRadio = findViewById<RadioButton>(R.id.InternalStorageRadio)

        hostText.setText(host)
        portText.setText(port)
        userNameText.setText(userName)
        passwordText.setText(password)
        savePathText.setText(remoteRootPath)
        ssidText.setText(ssid)
        //storageRadioGroup.setOnCheckedChangeListener {_, id ->
        //    // 参照するストレージが変更された場合は、参照先を一旦リセットする
        //    val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        //    val editor = pref.edit()
        //    editor.putString("directorypath", "")
        //    editor.apply()
        //}
        if (storage == "1") {
            sdCardRadio.isChecked = true
        } else {
            internalStorageRadio.isChecked = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    /**
     * ActionBar戻るボタンクリック
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            /*** プリファレンスに保存 ***/
            savePreferences()

            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*** プリファレンスに保存 ***/
        savePreferences()

        finish()
    }

    /**
     * プリファレンスに保存
     */
    private fun savePreferences() {
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val hostText = findViewById<EditText>(R.id.HostText)
        val portText = findViewById<EditText>(R.id.PortText)
        val userNameText = findViewById<EditText>(R.id.UserNameText)
        val passwordText = findViewById<EditText>(R.id.PasswordText)
        val savePathText = findViewById<EditText>(R.id.SavePathText)
        val ssidText = findViewById<EditText>(R.id.SSIDText)
        var storage = pref.getString("storage", "1")
        var directoryPath = pref.getString("directorypath", "")
        val storageRadioGroup = findViewById<RadioGroup>(R.id.StorageRadioGroup)
        val id = storageRadioGroup.checkedRadioButtonId
        val selectedRadioButton = storageRadioGroup.findViewById<RadioButton>(id)
        val index = storageRadioGroup.indexOfChild(selectedRadioButton)
        when (index) {
            0 -> {
                // SDカード
                if (storage == "2") {
                    directoryPath = ""
                }

                storage = "1"
            }
            1 -> {
                // 内部ストレージ
                if (storage == "1") {
                    directoryPath = ""
                }

                storage = "2"
            }
        }


        val editor = pref.edit()
        editor.putString("host", hostText.text.toString())
        editor.putString("port", portText.text.toString())
        editor.putString("username", userNameText.text.toString())
        editor.putString("password", passwordText.text.toString())
        editor.putString("savepath", savePathText.text.toString())
        editor.putString("ssid", ssidText.text.toString())
        editor.putString("storage", storage)
        editor.putString("directorypath", directoryPath)
        editor.apply()
    }
}
