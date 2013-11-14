package com.techd.wifi.model;

import java.util.Map;

public class WifiScanResult {
	Map<String, Integer> routerSignal;  // bssid to signal
	
	Integer getSignal(String bssid) {
		return routerSignal.get(bssid);
	}
}
