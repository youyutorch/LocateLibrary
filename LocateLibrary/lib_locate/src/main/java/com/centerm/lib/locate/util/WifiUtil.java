package com.centerm.lib.locate.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * @desc
 * @author tianyouyu
 * @date 2019/5/6 19:18
 */
public class WifiUtil {

    public static boolean isWifiEnabled(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 获取当前连接WIFI的SSID
     */
    public static String getSSID(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            WifiInfo winfo = manager.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return null;
    }

    public static String getWifiIp(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiEnabled(context)) {
            int ipAsInt = manager.getConnectionInfo().getIpAddress();
            if (ipAsInt == 0) {
                return null;
            } else {
                return intToInet(ipAsInt);
            }
        } else {
            return null;
        }
    }

    public static String getWifiMac(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiEnabled(context)) {
            return manager.getConnectionInfo().getBSSID();
        } else {
            return null;
        }
    }

    private static String intToInet(int ip) {
        StringBuffer buffer = new StringBuffer();
        buffer.append((ip & 0xFF) + ".");
        buffer.append(((ip >> 8) & 0xFF) + ".");
        buffer.append(((ip >> 16) & 0xFF) + ".");
        buffer.append((ip >> 24) & 0xFF);

        return buffer.toString();
    }
}
