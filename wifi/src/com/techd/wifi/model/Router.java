package com.techd.wifi.model;

public class Router {
	String ssid;
	String bssid; // unique key

	boolean known; // if true then location is set
	LocationPoint location;
	
	public Router(String ssid, String bssid) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.known = false;
		this.location = null;
	}
	
	public Router(String ssid, String bssid, LocationPoint location) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.known = true;
		this.location = location;
	}
	

}
