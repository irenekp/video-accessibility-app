package com.example.accessibility;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import java.net.InetAddress;

public class DecisionEngine {
    public void profiler(Context context){
        int nwType=networktype(context);
        float batteryPer=getBatteryPercentage(context);
        boolean ischarging=batteryChargingStatus(context);
        float latency=networkLatency(context);
        //math math math
    }
    public static boolean decision_engine(int func, String[]paths){
        switch(func){
            case 0:
                //subtitles
                //path->video->size->profiler->predictedload->can it handle
            case 1:
                //description
            case 2:
                //compression

        }
        return false;
    }

    public int networktype(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting ()) {
            return 1;
        } else if (mobile.isConnectedOrConnecting ()) {
            return 2;
        } else {
            return 0;
        }
    }
    public static int getBatteryPercentage(Context context) {

        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        } else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            return (int) (batteryPct * 100);
        }
    }
    public static boolean batteryChargingStatus(Context context){
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return bm.isCharging();
    }
    public static float networkLatency(Context context){
        String host = "3.22.70.87";
        int timeout = 3000;
        float latency=(float)0.0;
        for(int i=0;i<5;i++){
            long beforeTime = System.currentTimeMillis();
            try {
                Boolean reachable = InetAddress.getByName(host).isReachable(timeout);
                if(!reachable){
                    throw new Exception();
                }
            }catch (Exception e){
                System.out.println("Cant Ping Server");
                e.printStackTrace();
            }
            long afterTime = System.currentTimeMillis();
            long timeDifference = afterTime - beforeTime;
            latency+=timeDifference;
        }
        latency/=5;
        return latency;
    }
}
