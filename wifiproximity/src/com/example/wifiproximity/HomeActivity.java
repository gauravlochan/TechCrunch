package com.example.wifiproximity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class HomeActivity extends Activity {

//    private static final String SERVER_IP = "http://10.10.1.81:8090";
//    private static final String SERVER_LINK = SERVER_IP + "/krypton/foo/device/link";
//    private static final String SERVER_UNLINK = SERVER_IP + "/krypton/foo/device/unlink";

    int nearcount = 0;
    int farcount = 0;
    boolean isNear = false;
    private final static int COUNT_THRESHOLD = 3;
    private final static int NEAR_THRESHOLD = -49;
    private final static int FAR_THRESHOLD = -50;

    private WifiManager mWifiService;
    private WifiReceiver mReceiver;

    private EditText mDeviceNameET;
    private Button mGoButton;
    private ProgressBar mProgressBar;
    private TextView mMessage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mMessage = (TextView) findViewById(R.id.tv_message);
        mDeviceNameET = (EditText) findViewById(R.id.et_name);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mGoButton = (Button) findViewById(R.id.btn_go);

        mWifiService = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }


    public void onClickGoButton(View v) {
        if (!TextUtils.isEmpty(mDeviceNameET.getText().toString())) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMessage.setVisibility(View.VISIBLE);
            mGoButton.setEnabled(false);
            mDeviceNameET.setEnabled(false);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mDeviceNameET.getWindowToken(), 0);
            mWifiService.startScan();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mReceiver = new WifiReceiver();
        registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = mWifiService.getScanResults();
            for (ScanResult result : results) {
                if (result.SSID.equals("demoAP")) {
                    trackDemoAp(result);
                }
            }

            mWifiService.startScan();
        }
    }


    private void trackDemoAp(ScanResult result) {
        if ((result.level > NEAR_THRESHOLD) && (!isNear)) {
            nearcount++;
        } else if ((result.level < FAR_THRESHOLD) && (isNear)) {
            farcount++;
        }

        String name = mDeviceNameET.getText().toString();

        // If it is currently far, and has come close to the server
        // enough times
        if ((nearcount >= COUNT_THRESHOLD) && (!isNear)) {
            new SendShitToServerTask(getServerIp() + "/krypton/foo/device/link/" + name).execute();
            mMessage.setText("I sense a demoAP.");
            isNear = true;
            farcount = 0;
        }

        // If it is currently near, and has moved far away from the server
        // enough times
        if ((farcount >= COUNT_THRESHOLD) && (isNear)) {
            new SendShitToServerTask(getServerIp() + "/krypton/foo/device/unlink/" + name).execute();
            mMessage.setText("Nope... nothing yet.");
            isNear = false;
            nearcount = 0;
        }
    }


    private class SendShitToServerTask extends AsyncTask<Void, Void, Void> {

        String u;

        public SendShitToServerTask(String s) {
            u = s;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpCodeAndResponse response = runOkHttpGetRequest(u);
                Log.w("ASDASDASD", response.getCode() + " :: " + response.getResponse());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    private HttpCodeAndResponse runOkHttpGetRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpCodeAndResponse codeAndResponse = new HttpCodeAndResponse();
        InputStream in = null;
        try {
            HttpURLConnection connection = client.open(new URL(url));
            codeAndResponse.setCode(String.valueOf(connection.getResponseCode()));
            in = connection.getInputStream();
            byte[] response = readFully(in);
            codeAndResponse.setResponse(new String(response));

        } finally {
            if (in != null) in.close();
        }

        return codeAndResponse;
    }


    private byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }


    private String getServerIp() {
        CheckBox foo = (CheckBox) findViewById(R.id.cb_useremote);
        EditText local = (EditText) findViewById(R.id.et_local_ip);
        EditText remote = (EditText) findViewById(R.id.et_remote_ip);

        if (foo.isChecked()) {
            // Use local
            return local.getText().toString();
        } else {
            // Use remote
            return remote.getText().toString();
        }
    }


}
