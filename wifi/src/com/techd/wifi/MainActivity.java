package com.techd.wifi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.techd.wifi.model.Layout;

public class MainActivity extends Activity implements OnClickListener
 {      
    WifiManager wifi;       
    ListView lv;
    TextView textStatus;
    Button buttonScan;
    Button buttonRecord;
    Button buttonCalc;
    TextView locationResult;
    
    EditText scanName;
    
    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonScan = (Button) findViewById(R.id.button1);
        buttonScan.setOnClickListener(this);
        
        scanName = (EditText) findViewById(R.id.editText1);

        locationResult = (TextView) findViewById(R.id.textView2);
        buttonRecord = (Button) findViewById(R.id.button2);
        buttonRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dumpResults(scanName.getText().toString(), results);
			}
		});
        
        buttonCalc = (Button) findViewById(R.id.buttonCalc);
        buttonCalc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				locationResult.setText(processDump(scanName.getText().toString()));
			}
		});


        lv = (ListView)findViewById(R.id.listView1);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }   
        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.listview_row, new String[] { ITEM_KEY }, new int[] { R.id.text });
        lv.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
            	// Not threadsafe!!!!
               results = wifi.getScanResults();
               size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));                    
    }

    public void onClick(View view) 
    {
        arraylist.clear();          
        wifi.startScan();

        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
        try 
        {
            size = size - 1;
            while (size >= 0) 
            {   
                HashMap<String, String> item = new HashMap<String, String>();                       
                item.put(ITEM_KEY, results.get(size).SSID + "  BSSID="+results.get(size).BSSID+"  level=" + results.get(size).level);

                arraylist.add(item);
                size--;
                adapter.notifyDataSetChanged();                 
            } 
        }
        catch (Exception e)
        { }         
    }
    
    // global
    List<ScanResult> allResults = new ArrayList<ScanResult>();

    
    public void dumpResults(String filename, List<ScanResult> results) {

    	try {
    		File external = Environment.getExternalStorageDirectory();
    		File dir = new File(external.getAbsolutePath() + "/wifi");
    		dir.mkdirs();
    		
    		File file = new File(dir + "/"+filename+".csv");
    	    FileOutputStream fos = new FileOutputStream(file, true);
    	    long time = System.currentTimeMillis();
    	    for (int i=0; i< results.size(); i++) {
    	    	String ssid = results.get(i).SSID;
    	    	if (ssid.equals("TECHCRUNCH")) {
    	    		// Keep adding to global
    	    		allResults.add(results.get(i));
    	    		
    	    		// Write to specific file
	    	    	String data = String.valueOf(time) + "," +
	    	    			results.get(i).BSSID + "," +
	    	    			results.get(i).SSID +"," +
	    	    			results.get(i).level +"\n";
	    	    	
	    	    	fos.write(data.getBytes());
    	    	}
    	    }
    	    fos.close();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    	
    }
    
	public String processDump(String filename) {
		Layout layout = new Layout();
		layout.inferLocation2(allResults);
		return null;
	}
}