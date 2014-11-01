package com.flyn.net.wifi.support;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.flyn.util.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Wifi
{
    public static final String WPA2 = "WPA2";
    public static final String WPA = "WPA";
    public static final String WEP = "WEP";
    public static final String OPEN = "Open";
    public static final String WPA_EAP = "WPA-EAP";
    public static final String IEEE8021X = "IEEE8021X";
    public static final String[] EAP_METHOD = {"PEAP", "TLS", "TTLS"};
    public static final int WEP_PASSWORD_AUTO = 0;
    public static final int WEP_PASSWORD_ASCII = 1;
    public static final int WEP_PASSWORD_HEX = 2;
    static final String[] SECURITY_MODES = {"WEP", "WPA", "WPA2", "WPA-EAP", "IEEE8021X"};
    private static final int MAX_PRIORITY = 99999;

    public static boolean changePasswordAndConnect(Context ctx, WifiManager wifiMgr, WifiConfiguration config, String newPassword, int numOpenNetworksKept)
    {
        setupSecurity(config, getWifiConfigurationSecurity(config), newPassword);
        int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1)
        {
            return false;
        }

        return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
    }

    public static boolean connectToNewNetwork(Context ctx, WifiManager wifiMgr, ScanResult scanResult, String password, int numOpenNetworksKept)
    {
        String security = getScanResultSecurity(scanResult);

        if (security.equals("Open"))
        {
            checkForExcessOpenNetworkAndSave(wifiMgr, numOpenNetworksKept);
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(scanResult.SSID);
        config.BSSID = scanResult.BSSID;
        setupSecurity(config, security, password);

        int id = wifiMgr.addNetwork(config);
        if (id == -1)
        {
            return false;
        }

        if (!wifiMgr.saveConfiguration())
        {
            return false;
        }

        List<WifiConfiguration> configList = getWifiConfiguration(wifiMgr, config, true);
        if ((configList == null) || (configList.size() == 0))
        {
            return false;
        }
        config = configList.get(0);

        return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
    }

    public static boolean connectToConfiguredNetwork(Context ctx, WifiManager wifiMgr, WifiConfiguration config, boolean reassociate)
    {
        int oldPri = config.priority;

        int newPri = getMaxPriority(wifiMgr);
        if (newPri == -1)
        {
            return false;
        }
        newPri++;
        if (newPri > 99999)
        {
            newPri = shiftPriorityAndSave(wifiMgr);
            if (newPri == -1)
            {
                return false;
            }
            List<WifiConfiguration> configList = getWifiConfiguration(wifiMgr, config, true);
            if ((configList == null) || (configList.size() == 0))
            {
                return false;
            }
            config = configList.get(0);
        }

        config.priority = newPri;
        int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1)
        {
            return false;
        }

        if (!wifiMgr.enableNetwork(networkId, false))
        {
            config.priority = oldPri;
            return false;
        }

        if (!wifiMgr.saveConfiguration())
        {
            config.priority = oldPri;
            return false;
        }

        List<WifiConfiguration> configList = getWifiConfiguration(wifiMgr, config, true);
        if ((configList == null) || (configList.size() == 0))
        {
            return false;
        }
        config = configList.get(0);

        ReenableAllApsWhenNetworkStateChanged.schedule(ctx);

        if (!wifiMgr.enableNetwork(config.networkId, true))
        {
            return false;
        }

        boolean connect = reassociate ? wifiMgr.reassociate() : wifiMgr.reconnect();

        return connect;
    }

    private static void sortByPriority(List<WifiConfiguration> configurations)
    {
        Collections.sort(configurations, new Comparator<WifiConfiguration>()
        {
            @Override
            public int compare(WifiConfiguration object1, WifiConfiguration object2)
            {
                return object1.priority - object2.priority;
            }
        });
    }

    private static boolean checkForExcessOpenNetworkAndSave(WifiManager wifiMgr, int numOpenNetworksKept)
    {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null)
        {
            return false;
        }
        sortByPriority(configurations);

        boolean modified = false;
        int tempCount = 0;
        for (int i = configurations.size() - 1; i >= 0; i--)
        {
            WifiConfiguration config = configurations.get(i);
            if (!getWifiConfigurationSecurity(config).equals("Open"))
            {
                continue;
            }
            tempCount++;
            if (tempCount < numOpenNetworksKept)
            {
                continue;
            }
            modified = true;
            wifiMgr.removeNetwork(config.networkId);
        }

        if (modified)
        {
            return wifiMgr.saveConfiguration();
        }

        return true;
    }

    private static int shiftPriorityAndSave(WifiManager wifiMgr)
    {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null)
        {
            return -1;
        }
        sortByPriority(configurations);
        int size = configurations.size();
        for (int i = 0; i < size; i++)
        {
            WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }

    private static int getMaxPriority(WifiManager wifiManager)
    {
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        if (configurations == null)
        {
            return -1;
        }
        int pri = 0;
        for (WifiConfiguration config : configurations)
        {
            if (config.priority <= pri)
            {
                continue;
            }
            pri = config.priority;
        }

        return pri;
    }

    public static List<WifiConfiguration> getWifiConfiguration(WifiManager wifiMgr, ScanResult hotsopt, boolean compareSecurity)
    {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null)
        {
            return null;
        }

        List<WifiConfiguration> returnVal = new ArrayList<WifiConfiguration>();
        String ssid = convertToQuotedString(hotsopt.SSID);
        if (ssid.length() == 0)
        {
            return returnVal;
        }
        String bssid = hotsopt.BSSID;
        if (bssid == null)
        {
            return returnVal;
        }
        String hotspotSecurity = getScanResultSecurity(hotsopt);
        for (WifiConfiguration config : configurations)
        {
            if ((config.SSID == null) || (!ssid.equals(config.SSID)))
            {
                continue;
            }
            if ((config.BSSID != null) && (!bssid.equals(config.BSSID)))
            {
                continue;
            }
            if (!compareSecurity)
            {
                returnVal.add(config);
                break;
            }
            String configSecurity = getWifiConfigurationSecurity(config);
            if (!hotspotSecurity.equals(configSecurity))
            {
                continue;
            }
            returnVal.add(config);
            break;
        }

        return returnVal;
    }

    public static List<WifiConfiguration> getWifiConfiguration(WifiManager wifiMgr, WifiConfiguration configToFind, boolean compareSecurity)
    {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null)
        {
            return null;
        }

        List<WifiConfiguration> returnVal = new ArrayList<WifiConfiguration>();
        String ssid = configToFind.SSID;
        if (ssid.length() == 0)
        {
            return returnVal;
        }
        String bssid = configToFind.BSSID;
        String security = getWifiConfigurationSecurity(configToFind);
        for (WifiConfiguration config : configurations)
        {
            if ((config.SSID == null) || (!ssid.equals(config.SSID)))
            {
                continue;
            }
            if ((config.BSSID != null) && (bssid != null) && (!bssid.equals(config.BSSID)))
            {
                continue;
            }
            if (!compareSecurity)
            {
                returnVal.add(config);
                break;
            }
            String configSecurity = getWifiConfigurationSecurity(config);
            if (!security.equals(configSecurity))
            {
                continue;
            }
            returnVal.add(config);
            break;
        }

        return returnVal;
    }

    public static String getWifiConfigurationSecurity(WifiConfiguration wifiConfig)
    {
        if (wifiConfig.allowedKeyManagement.get(0))
        {
            if ((!wifiConfig.allowedGroupCiphers.get(3)) && ((wifiConfig.allowedGroupCiphers.get(0)) || (wifiConfig.allowedGroupCiphers.get(1))))
            {
                return "WEP";
            }

            return "Open";
        }
        if (wifiConfig.allowedProtocols.get(1))
        {
            return "WPA2";
        }
        if (wifiConfig.allowedKeyManagement.get(2))
        {
            return "WPA-EAP";
        }
        if (wifiConfig.allowedKeyManagement.get(3))
        {
            return "IEEE8021X";
        }
        if (wifiConfig.allowedProtocols.get(0))
        {
            return "WPA";
        }

        LogManager.w(Wifi.class, "Unknown security type from WifiConfiguration, falling back on open.");
        return "Open";
    }

    public static void setupSecurity(WifiConfiguration config, String security, String password)
    {
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        if (TextUtils.isEmpty(security))
        {
            security = "Open";
            LogManager.w(Wifi.class, "Empty security, assuming open");
        }

        if (security.equals("WEP"))
        {
            int wepPasswordType = 0;

            if (!TextUtils.isEmpty(password))
            {
                if (wepPasswordType == 0)
                {
                    if (isHexWepKey(password))
                    {
                        config.wepKeys[0] = password;
                    } else
                    {
                        config.wepKeys[0] = convertToQuotedString(password);
                    }
                } else
                {
                    config.wepKeys[0] = (wepPasswordType == 1 ? convertToQuotedString(password) : password);
                }
            }

            config.wepTxKeyIndex = 0;

            config.allowedAuthAlgorithms.set(0);
            config.allowedAuthAlgorithms.set(1);

            config.allowedKeyManagement.set(0);

            config.allowedGroupCiphers.set(0);
            config.allowedGroupCiphers.set(1);
        } else if ((security.equals("WPA")) || (security.equals("WPA2")))
        {
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(3);

            config.allowedKeyManagement.set(1);

            config.allowedPairwiseCiphers.set(2);
            config.allowedPairwiseCiphers.set(1);

            config.allowedProtocols.set(security.equals("WPA2") ? 1 : 0);

            if (!TextUtils.isEmpty(password))
            {
                if ((password.length() == 64) && (isHex(password)))
                {
                    config.preSharedKey = password;
                } else
                {
                    config.preSharedKey = convertToQuotedString(password);
                }
            }
        } else if (security.equals("Open"))
        {
            config.allowedKeyManagement.set(0);
        } else if ((security.equals("WPA-EAP")) || (security.equals("IEEE8021X")))
        {
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(3);
            if (security.equals("WPA-EAP"))
            {
                config.allowedKeyManagement.set(2);
            } else
            {
                config.allowedKeyManagement.set(3);
            }
            if (!TextUtils.isEmpty(password))
            {
                config.preSharedKey = convertToQuotedString(password);
            }
        }
    }

    private static boolean isHexWepKey(String wepKey)
    {
        int len = wepKey.length();

        if ((len != 10) && (len != 26) && (len != 58))
        {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key)
    {
        for (int i = key.length() - 1; i >= 0; i--)
        {
            char c = key.charAt(i);
            if (((c < '0') || (c > '9')) && ((c < 'A') || (c > 'F')) && ((c < 'a') || (c > 'f')))
            {
                return false;
            }
        }

        return true;
    }

    public static String convertToQuotedString(String string)
    {
        if (TextUtils.isEmpty(string))
        {
            return "";
        }

        int lastPos = string.length() - 1;
        if ((lastPos < 0) || ((string.charAt(0) == '"') && (string.charAt(lastPos) == '"')))
        {
            return string;
        }

        return "\"" + string + "\"";
    }

    public static String getScanResultSecurity(ScanResult scanResult)
    {
        String cap = scanResult.capabilities;
        for (int i = SECURITY_MODES.length - 1; i >= 0; i--)
        {
            if (cap.contains(SECURITY_MODES[i]))
            {
                return SECURITY_MODES[i];
            }
        }

        return "Open";
    }
}
