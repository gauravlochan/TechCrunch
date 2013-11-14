package com.techd.wifi.model;

import java.util.Map;

// Read in a bunch of measurements 
// e.g. location topRight will have
//      n values for router x
public class Measurement {
	LocationPoint point;
	Map<String, Integer> routerSignal;  // bssid to signal
}
