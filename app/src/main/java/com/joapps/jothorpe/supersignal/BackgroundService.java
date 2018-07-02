package com.joapps.jothorpe.supersignal;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class BackgroundService extends Service {

    private final IBinder myBinder = new MyLocalBinder();
    TelephonyManager mTelephonyManager;
    String sStrength, phonestate, cellLocation;
    int wait = 0;
    int wait2 = 0;
    public static final String TAG = "BackgroundService";

    public class MyLocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mTelephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);


                    if (signalStrength.isGsm()) {
                        if (signalStrength.getGsmSignalStrength() != 99)
                            sStrength = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113);
                        else
                            sStrength = String.valueOf(signalStrength.getGsmSignalStrength());
                    } else {
                        sStrength = String.valueOf(signalStrength.getCdmaDbm());
                    }


                    Log.i(TAG, "Signal Strength: " + sStrength);

                    if (wait2 == 0) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Intent i = new Intent();
                                i.setAction("com.joapps.jothorpe.signalstrength");
                                i.putExtra("data", "1" + sStrength);
                                sendBroadcast(i);
                            }
                        }, 2000);

                        wait2++;
                    }

                    Intent i = new Intent();
                    i.setAction("com.joapps.jothorpe.signalstrength");
                    i.putExtra("data", "1" + sStrength);
                    sendBroadcast(i);


                }
            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            mTelephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onServiceStateChanged(ServiceState serviceState) {
                    super.onServiceStateChanged(serviceState);

                    switch (serviceState.getState()) {
                        case ServiceState.STATE_EMERGENCY_ONLY:
                            phonestate = "STATE_EMERGENCY_ONLY";

                            break;
                        case ServiceState.STATE_IN_SERVICE:
                            phonestate = "STATE_IN_SERVICE";

                            break;
                        case ServiceState.STATE_OUT_OF_SERVICE:
                            phonestate = "STATE_OUT_OF_SERVICE";

                            break;
                        case ServiceState.STATE_POWER_OFF:
                            phonestate = "STATE_POWER_OFF";

                            break;
                        default:
                            phonestate = "Unknown";

                            break;
                    }

                    Log.i(TAG, "Service State: " + phonestate);

                    if (wait == 0) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Intent j = new Intent();
                                j.setAction("com.joapps.jothorpe.signalstrength");
                                j.putExtra("data", "2" + phonestate);
                                sendBroadcast(j);
                            }
                        }, 1000);

                        wait++;
                    }

                    Intent j = new Intent();
                    j.setAction("com.joapps.jothorpe.signalstrength");
                    j.putExtra("data", "2" + phonestate);
                    sendBroadcast(j);

                }
            }, PhoneStateListener.LISTEN_SERVICE_STATE);

            mTelephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCellLocationChanged(CellLocation location) {
                    super.onCellLocationChanged(location);


                    int cid = 0;
                    int lac = 0;

                    if (location != null) {
                        if (location instanceof GsmCellLocation) {
                            cid = ((GsmCellLocation) location).getCid();
                            lac = ((GsmCellLocation) location).getLac();
                        } else if (location instanceof CdmaCellLocation) {
                            cid = ((CdmaCellLocation) location).getBaseStationId();
                            lac = ((CdmaCellLocation) location).getSystemId();
                        }
                    }


                    cellLocation = "Gsm location area code:" + Integer.toString(lac) + "- Cell ID:" + Integer.toString(cid);

                    Log.i(TAG, "Cell Location: " + cellLocation);

                    Intent j = new Intent();
                    j.setAction("com.joapps.jothorpe.signalstrength");
                    j.putExtra("data", "3" + cellLocation);
                    sendBroadcast(j);


                }

            }, PhoneStateListener.LISTEN_CELL_LOCATION);

        }

    }

}

