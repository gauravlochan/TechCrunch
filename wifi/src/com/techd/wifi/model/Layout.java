package com.techd.wifi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.wifi.ScanResult;

import com.techd.wifi.util.Statistics;

// This is our master structure for the event
public class Layout {
	List<Router> routers; // bssid to router
	List<Measurement> measurements;
	
	// 5c:0e:8b:e5:64:f0
	// 5c:0e:8b:f6:20:b0
	// fc:0a:81:05:1c:b0
	// fc:0a:81:05:23:f0
	// fc:0a:81:05:a9:90
	// fc:0a:81:05:ac:10
	
	public Layout() {
		// Read in router info
		routers = new ArrayList<Router>(6);
//		routers.put("5c:0e:8b:e5:64:f0", new Router("TECHCRUNCH","5c:0e:8b:e5:64:f0", new LocationPoint(50,100)));
//		routers.put("5c:0e:8b:f6:20:b0", new Router("TECHCRUNCH","5c:0e:8b:f6:20:b0", new LocationPoint(80,100)));
//		routers.put("fc:0a:81:05:1c:b0", new Router("TECHCRUNCH","fc:0a:81:05:1c:b0", new LocationPoint(80,0)));
//		routers.put("fc:0a:81:05:23:f0", new Router("TECHCRUNCH","fc:0a:81:05:23:f0", new LocationPoint(0, 30)));
//		routers.put("fc:0a:81:05:ac:10", new Router("TECHCRUNCH","fc:0a:81:05:ac:10", new LocationPoint(100, 60)));

		routers.add(new Router("TECHCRUNCH","5c:0e:8b:e5:64:f0", new LocationPoint(50,100)));
		routers.add(new Router("TECHCRUNCH","5c:0e:8b:f6:20:b0", new LocationPoint(80,100)));
		routers.add(new Router("TECHCRUNCH","fc:0a:81:05:1c:b0", new LocationPoint(80,0)));
		routers.add(new Router("TECHCRUNCH","fc:0a:81:05:23:f0", new LocationPoint(0, 30)));
		routers.add(new Router("TECHCRUNCH","fc:0a:81:05:ac:10", new LocationPoint(100, 60)));

		// Read in measurements
		// TODO: Build the logic based on inferences later.  For now use the prefetched APs
		
	}

	/**
	 * Given the reading at a user point, get location using known routers
	 * 
	 * @param measurements
	 * @return
	 */
	public LocationPoint inferLocation2(List<ScanResult> scanResults) {
		// e should have multiple readings from this to each AP
		Map<Router,List<Integer>> routerReadings = new HashMap<Router, List<Integer>>();
		Map<Router,Double> routerStdDev = new HashMap<Router, Double>();

		// Go through each scan result to get router values
		for (ScanResult scanResult: scanResults) {
			String bssid = scanResult.BSSID;
			for (Router router: routers) {
				if (router.bssid.equals(bssid)) {
					List<Integer> reads = routerReadings.get(router);
					if (reads == null) {
						reads = new ArrayList<Integer>();
						routerReadings.put(router, reads);
					}
					reads.add(scanResult.level);
				}
			}
		}

		for (Router router: routers) {
			List<Integer> dBValues = routerReadings.get(router);
			Statistics stats = new Statistics(dBValues);
			routerStdDev.put(router, stats.getStdDev());
		}
		return null;
	}

	/**
	 * Given the reading at a user point, get location using known routers
	 * 
	 * @param measurements
	 * @return
	 */
	public LocationPoint inferLocation(List<WifiScanResult> scanResults) {
		// e should have multiple readings from this to each AP
		Map<Router,List<Integer>> routerReadings = new HashMap<Router, List<Integer>>();
		Map<Router,Double> routerStdDev = new HashMap<Router, Double>();

		for (Router router: routers) {
			ArrayList<Integer> dBValues = new ArrayList<Integer>();

			// Go through each scan result to get router values
			for (WifiScanResult scanResult: scanResults) {
				Integer dBValue = scanResult.getSignal(router.bssid);
				if (dBValue != null) {
					dBValues.add(dBValue);
				}
			}
			routerReadings.put(router, dBValues);
			Statistics stats = new Statistics(dBValues);
			routerStdDev.put(router, stats.getStdDev());
		}
		return null;
	}
	
}
