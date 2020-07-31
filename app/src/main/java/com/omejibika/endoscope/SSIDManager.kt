package com.omejibika.endoscope

import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

class SSIDManager {
    companion object {
        /**
         * 現在接続中のSSIDを取得する
         */
        fun getSSID(context: Context): String {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) return ""
            val connectInfo = wifiManager.connectionInfo

            val state = WifiInfo.getDetailedStateOf(connectInfo?.supplicantState)
            return when (state) {
                null -> ""
                NetworkInfo.DetailedState.CONNECTED, NetworkInfo.DetailedState.OBTAINING_IPADDR -> wifiManager.connectionInfo.ssid
                else -> ""
            }
        }

        /**
         * 登録されているSSIDが存在するかチェックする
         */
        fun checkConfiguredNetworks(context: Context): Boolean {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            return wifiManager.configuredNetworks != null
        }

        /**
         * 指定したSSIDに再接続する
         */
        fun reconnectPreferenceSSID(context: Context, ssid: String): Boolean {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            /*** SSIDが既に登録されている場合 ***/
            var targetConfiguration: WifiConfiguration? = null
            for (c0 in wifiManager.configuredNetworks) {
                if (c0.SSID.replace("\"", "") == ssid) {
                    targetConfiguration = c0
                    break
                }
            }

            if (targetConfiguration != null) {
                /*** Wi-Fiに接続 ***/
                wifiManager.enableNetwork(targetConfiguration.networkId, true)
                for (c0 in wifiManager.configuredNetworks) {
                        wifiManager.enableNetwork(c0.networkId, false)
                }
                return true
            } else {
                return false
            }
        }
    }
}