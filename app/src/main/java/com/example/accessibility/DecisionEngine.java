package com.example.accessibility;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DecisionEngine {

    public void profiler(Context context) {
        int nwType = networktype(context);
        float batteryPer = getBatteryPercentage(context);
        boolean ischarging = batteryChargingStatus(context);
        float latency = networkLatency(context);
    }

    public static boolean decision_engine(int func, String[] paths) {

        switch (func) {
            case 1:
                //compression
            case 2:
                //description
            case 3:
                //subtitles

        }
        return false;
    }

    public int networktype(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting()) {
            return 1;
        } else if (mobile.isConnectedOrConnecting()) {
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

    public static boolean batteryChargingStatus(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return bm.isCharging();
    }

    public static float networkLatency(Context context) {
        String host = "3.22.70.87";
        int timeout = 3000;
        float latency = (float) 0.0;
        for (int i = 0; i < 5; i++) {
            long beforeTime = System.currentTimeMillis();
            try {
                Boolean reachable = InetAddress.getByName(host).isReachable(timeout);
                if (!reachable) {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Cant Ping Server");
                e.printStackTrace();
            }
            long afterTime = System.currentTimeMillis();
            long timeDifference = afterTime - beforeTime;
            latency += timeDifference;
        }
        latency /= 5;
        return latency;
    }

    public static long getVideoLength(Context context, String inputPath) {
        //MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //retriever.setDataSource(context, Uri.fromFile(new File(inputPath)));
        MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(new File(inputPath)));
        long duration = mp.getDuration();
        mp.release();

        //String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //long duration = Long.parseLong(time);
        //retriever.release();
        duration = duration / 1000; //converting to seconds
        return duration;
    }

    public static List<String[]> getNearestFromCSV(Context context, int func, long duration, int mode) {
        String videoSize;
        float[] durations = {120, 300, 600, 900, 1200};

        //finding nearest video length
        int idx = 0;
        int dist = (int) Math.abs(durations[0] - duration);
        for (int i = 1; i< durations.length; i++) {
            int cdist = (int) Math.abs(durations[i] - duration);
            if (cdist < dist) {
                idx = i;
                dist = cdist;
            }
        }

        videoSize = Float.toString(durations[idx]);
        System.out.println("Nearest video length is: " + videoSize);

        List<String[]> nearestVideos = new ArrayList<>();

        try {
            int flag = 0;
            final CSVReader reader = new CSVReader(new InputStreamReader(context.getAssets().open(func + ".csv")));
            List<String[]> fullCSV = reader.readAll();

            if (mode == 1) //mode -> 1=local, 2=wifi
            {   System.out.println("Got here!");
                for (int i = 0; i < fullCSV.size(); i++) {
                        if (fullCSV.get(i)[2].equalsIgnoreCase("Local"))
                        {
                            if(Float.parseFloat(fullCSV.get(i)[5]) == Float.parseFloat(videoSize)) {
                                nearestVideos.add(fullCSV.get(i));
                            }
                        }
                    }
                }
            else if (mode == 2) //if we're looking for WiFi videos
            {
                for (int i = 0; i < fullCSV.size(); i++) {
                    if (fullCSV.get(i)[2].equalsIgnoreCase("Wifi")) {
                        if(Float.parseFloat(fullCSV.get(i)[5]) == Float.parseFloat(videoSize)) {
                            System.out.println(fullCSV.get(i)[9]);
                            nearestVideos.add(fullCSV.get(i));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nearestVideos;
    }

    public static float averageOut(List<String[]> videos, int priority) throws ParseException {
        float averagedStatistic;

        //priority -> 1=exectime, 2=battery consumption
        float execTime = 0;
        float batteryConsumption = 0;

        if(priority == 1){
            //first, finding exec times for all entries
            for(int i = 0; i<videos.size(); i++){

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date startparsed = dateFormat.parse(videos.get(i)[3]);
                    Date endparsed = dateFormat.parse(videos.get(i)[4]);

                    execTime += endparsed.getTime() - startparsed.getTime();
            }

            execTime /= (videos.size()*1000); //1000 to convert from milliseconds to seconds
            averagedStatistic = execTime;
        }

        else if(priority == 2){
            for(int i = 0; i<videos.size(); i++){
                batteryConsumption += Float.parseFloat(videos.get(i)[9]);
            }
            batteryConsumption /= videos.size();
            averagedStatistic = batteryConsumption;
        }
        else{ averagedStatistic = Integer.MAX_VALUE;}
        return averagedStatistic;
    }


}
