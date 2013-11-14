package com.techd.wifi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is our master structure for the event
public class Layout {
	Map<String, Router> routers; // bssid to router
	List<Measurement> measurements;
	
	// 5c:0e:8b:e5:64:f0
	// 5c:0e:8b:f6:20:b0
	// fc:0a:81:05:1c:b0
	// fc:0a:81:05:23:f0
	// fc:0a:81:05:a9:90
	// fc:0a:81:05:ac:10
	
	public Layout() {
		// Read in router info
		routers = new HashMap<String, Router>(6);
		routers.put("5c:0e:8b:e5:64:f0", new Router("TECHCRUNCH","5c:0e:8b:e5:64:f0", new LocationPoint(50,100)));
		routers.put("5c:0e:8b:f6:20:b0", new Router("TECHCRUNCH","5c:0e:8b:f6:20:b0", new LocationPoint(80,100)));
		routers.put("fc:0a:81:05:1c:b0", new Router("TECHCRUNCH","fc:0a:81:05:1c:b0", new LocationPoint(80,0)));
		routers.put("fc:0a:81:05:23:f0", new Router("TECHCRUNCH","fc:0a:81:05:23:f0", new LocationPoint(0, 30)));
		routers.put("fc:0a:81:05:ac:10", new Router("TECHCRUNCH","fc:0a:81:05:ac:10", new LocationPoint(100, 60)));
		
		// Read in measurements
		// TODO: Build the logic based on infer
		
	}

	/**
	 * Given the reading at a user point, try to predict the locaion
	 * 
	 * @param measurements
	 * @return
	 */
	public LocationPoint inferLocation(List<Measurement> measurements) {
		return null;
		
	}
	
}
