package com.joapps.jothorpe.supersignal;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class UpdateReceiver extends BroadcastReceiver {
    public static final String BROADCAST_ACTION = "supersignal.action";
    private final Handler handler = new Handler();
    Intent intent;
    Context context;
    String ssmessage;


    @Override
    public void onReceive(Context context, Intent intent) {

        ssmessage = intent.getStringExtra("data");


        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 10);

        this.context = context;

        this.intent = new Intent(BROADCAST_ACTION);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            display();
        }
    };

    private void display() {
        intent.putExtra("key", ssmessage);
        context.sendBroadcast(intent);
    }


}
