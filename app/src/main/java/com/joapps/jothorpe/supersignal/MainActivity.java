package com.joapps.jothorpe.supersignal;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    int STORAGE_PERMISSION_CODE = 1;
    TextView txtSSID, txtBSSID, txtMacAddress, txtIPAddress, txtFrequency, txtLinkSpeed, txtNetworkID, txtWiFiLevel;
    TextView txtSignalStrength, txtServiceState, txtCellLocation, txtSignalDisplay;
    ImageView imgSignalStrength, imgWiFiSignal;
    String WiFiLevel;
    BackgroundService myService;
    ServiceConnection myConnection;
    UpdateReceiver myBroadCastReceiver;


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(UpdateReceiver.BROADCAST_ACTION));
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent) {
        String text = intent.getStringExtra("key");
        if (text != null) {
            String displayText = text.substring(1);

            //if key charat 0
            if (text.substring(0, 1).equals("1")) {
                txtSignalStrength.setText(displayText);
                signalStrengthDisplay(Integer.parseInt(displayText));
            } else if (text.substring(0, 1).equals("2")) {
                txtServiceState.setText(displayText);
            } else if (text.substring(0, 1).equals("3")) {
                txtCellLocation.setText(displayText);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        checkLocationPermission2();
        setUpLogStorage();
        setUpservice();
        enableWiFiListener();


    }



    private void findViews() {

        txtSignalStrength = findViewById(R.id.txtSignalStrength);
        txtServiceState = findViewById(R.id.txtServiceState);
        txtCellLocation = findViewById(R.id.txtCellLocation);
        txtSSID = findViewById(R.id.txtSSID);
        txtBSSID = findViewById(R.id.txtBSSID);
        txtMacAddress = findViewById(R.id.txtMacAddress);
        txtIPAddress = findViewById(R.id.txtIPAddress);
        txtFrequency = findViewById(R.id.txtFrequency);
        txtLinkSpeed = findViewById(R.id.txtLinkSpeed);
        txtNetworkID = findViewById(R.id.txtNetworkID);
        txtWiFiLevel = findViewById(R.id.txtWiFiLevel);
        imgSignalStrength = findViewById(R.id.imgSignalStregth);
        txtSignalDisplay = findViewById(R.id.txtSignalDisplay);
        imgWiFiSignal = findViewById(R.id.imgWiFiSignal);


    }

    private void setUpservice() {

        myBroadCastReceiver = new UpdateReceiver();
        Intent i = new Intent(this, BackgroundService.class);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.joapps.jothorpe.signalstrength");
        registerReceiver(myBroadCastReceiver, intentFilter);

        myConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BackgroundService.MyLocalBinder binder = (BackgroundService.MyLocalBinder) service;
                myService = binder.getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        bindService(i, myConnection, Context.BIND_AUTO_CREATE);

    }

    public void checkLocationPermission2() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Location permission already granted", Toast.LENGTH_LONG).show();
        } else {
            requestLocationPermission();
        }

    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("Permission needed").setMessage("Location is needed because for this application to function").setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, STORAGE_PERMISSION_CODE);
                }
            }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog
                            .dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission NOT granted", Toast.LENGTH_LONG).show();
                requestLocationPermission();
            }
        }
    }

    private void setUpLogStorage() {
        File logFile = null;
        if (isExternalStorageWritable()) {


            File appDirectory = new File(this.getExternalFilesDir(null) + "/SuperSignal");

            File logDirectory = new File(appDirectory + "/log");

            logFile = new File(logDirectory, "logcat" + System.currentTimeMillis() + ".txt");


            // create app folder

            if (!appDirectory.exists()) {

                appDirectory.mkdir();

            }


            // create log folder

            if (!logDirectory.exists()) {

                logDirectory.mkdir();

            }

            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        try {

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


            /*try {
                Log.d("MainActivity", "Trying to wrtie to file");
                Process process = Runtime.getRuntime().exec("logcat -c");

                process = Runtime.getRuntime().exec("logcat -f " + logFile);

            } catch (IOException e) {
                Log.e("MainActivity", "Error Trying to wrtie to file");
                e.printStackTrace();

            }*/


        } else {

            // not accessible
            Toast.makeText(this, "Can't write log file", Toast.LENGTH_LONG).show();

        }

    }


    public boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            return true;

        }

        return false;

    }


    private void enableWiFiListener() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (String.valueOf(wifiInfo.getSupplicantState()).equals("COMPLETED")) {
                String SSID, BSSID, MacAddress, IPAddress, Frequency, LinkSpeed, NetworkID, RSSI, WiFiLevelString;
                int numberOfLevels = 5;

                SSID = wifiInfo.getSSID();
                BSSID = wifiInfo.getBSSID();
                MacAddress = wifiInfo.getMacAddress();
                IPAddress = String.valueOf(wifiInfo.getIpAddress());
                Frequency = String.valueOf(wifiInfo.getFrequency());
                LinkSpeed = String.valueOf(wifiInfo.getLinkSpeed());
                NetworkID = String.valueOf(wifiInfo.getNetworkId());
                RSSI = String.valueOf(wifiInfo.getRssi());
                int wifiLevelNumber = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
                WiFiLevel = String.valueOf(wifiLevelNumber);
                WiFiLevelString = String.valueOf(wifiLevelNumber) + " out of " + String.valueOf(numberOfLevels - 1);

                txtSSID.setText(SSID);
                txtBSSID.setText(BSSID);
                txtMacAddress.setText(MacAddress);
                txtIPAddress.setText(IPAddress);
                txtFrequency.setText(Frequency);
                txtLinkSpeed.setText(LinkSpeed);
                txtNetworkID.setText(NetworkID);
                txtWiFiLevel.setText(WiFiLevelString);
                wifiLevelDisplay();

            }
        } else {
            wifiManager.setWifiEnabled(true);
        }
    }

    private void wifiLevelDisplay() {
        switch (WiFiLevel) {
            case "0":
                imgWiFiSignal.setImageResource(R.drawable.no_wifi);
                break;
            case "1":
                imgWiFiSignal.setImageResource(R.drawable.poor_wifi);
                break;
            case "2":
                imgWiFiSignal.setImageResource(R.drawable.moderate_wifi);
                break;
            case "3":
                imgWiFiSignal.setImageResource(R.drawable.good_wifi);
                break;
            case "4":
                imgWiFiSignal.setImageResource(R.drawable.great_wifi);
                break;
            default:
                imgWiFiSignal.setImageResource(R.drawable.no_wifi);
                break;
        }
    }

    private void signalStrengthDisplay(int strength) {

        if (strength < -112) {
            txtSignalDisplay.setText("No Signal");
            imgSignalStrength.setImageResource(R.drawable.no_signal);

        } else if (isBetween(strength, -111, -100)) {
            txtSignalDisplay.setText("Poor Signal");
            //imgSignalStrength.setImageResource(R.drawable.poor_signal);

        } else if (isBetween(strength, -99, -80)) {
            txtSignalDisplay.setText("Moderate Signal");
            //imgSignalStrength.setImageResource(R.drawable.moderate_signal);

        } else if (isBetween(strength, -79, -61)) {
            txtSignalDisplay.setText("Good Signal");
            //imgSignalStrength.setImageResource(R.drawable.good_signal);

        } else if (isBetween(strength, -61, 0)) {
            txtSignalDisplay.setText("Great Signal");
            //imgSignalStrength.setImageResource(R.drawable.great_signal);

        } else {
            txtSignalDisplay.setText("Error");
            //imgSignalStrength.setImageResource(R.drawable.no_signal);
        }

    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

}

