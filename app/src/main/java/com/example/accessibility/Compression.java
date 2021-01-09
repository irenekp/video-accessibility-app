package com.example.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
/**
public class Compression {
    static Long starttime;
    public static String local_compress(Context context, String predir, String pre_comp_op, int caller) {
        //loading.setVisibility(View.VISIBLE);
        final String[] output = new String[1];
        FFmpeg ffmpeg3 = FFmpeg.getInstance(context);
        try {
            ffmpeg3.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Toast.makeText(context, "onStart", Toast.LENGTH_SHORT);
                }

                @Override
                public void onFailure() {
                    Toast.makeText(context, "onFailure", Toast.LENGTH_SHORT);
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(context, "onSuccess", Toast.LENGTH_SHORT);
                }

                @Override
                public void onFinish() {

                    Toast.makeText(context, "onFinish", Toast.LENGTH_SHORT);
                    FFmpeg ffmpeg = FFmpeg.getInstance(context);
                    try {
                        //ffmpeg -i input.mp4 -vcodec libx265 -crf 28 output.mp4
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        output[0] = predir + "/" + timestamp.toString() + ".mp4";
                        String[] cmd = {"-i", pre_comp_op, "-vcodec", "libx264", "-crf", "28", output[0]};
                        // String[] cmd = {"-i", inputPath, "-vf","ass="+subtitlePath,output};
                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                            @Override
                            public void onStart() {
                                System.out.println("\n---------COMMAND\nSTART\n");
                            }

                            @Override
                            public void onProgress(String message) {
                                System.out.println("Second Task: Progress" + message);
                            }

                            @Override
                            public void onFailure(String message) {
                                System.out.println("\n---------COMMAND\nFAILURE\n" + message);
                            }

                            @Override
                            public void onSuccess(String message) {
                                System.out.println("\n---------COMMAND\nSUCCESS\n");
                            }

                            @Override
                            public void onFinish() {
                                System.out.println("\n---------COMMAND\nFINISH\n");
                                System.out.println("Path Is:" + output[0]);
                                if(caller==0){
                                    Subtitles.updateUi(context,output[0]);
                                }else{
                                    Description.updateUi(context,output[0]);
                                }
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        // Handle if FFmpeg is already running
                        if(caller==0){
                            Subtitles.errorUi(context);
                        }else{
                            Description.errorUi(context);
                        }
                        e.printStackTrace();
                    }
                }
            });
        } catch (FFmpegNotSupportedException e) {
            if(caller==0){
                Subtitles.errorUi(context);
            }else{
                Description.errorUi(context);
            }
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
            System.out.println("\n---------\nISSUE\n");
            // Handle if FFmpeg is not supported by device
        }
        return output[0];
    }

    public static void remote_compress(Context context, String predir, String selectedFilePath, int caller) throws FileNotFoundException {
        String postUrl = "http://3.22.70.87:8080/compressVideo";
        starttime=System.currentTimeMillis();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ContentResolver contentResolver = context.getContentResolver();
        final String contentType = "video/mp4";
        final AssetFileDescriptor fd;
        fd = contentResolver.openAssetFileDescriptor(Uri.fromFile(new File(selectedFilePath)), "r");
        System.out.println(selectedFilePath);

        RequestBody videoFile = new RequestBody() {
            @Override
            public long contentLength() {
                return fd.getDeclaredLength();
            }

            @Override
            public MediaType contentType() {
                return MediaType.parse(contentType);
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream is = fd.createInputStream()) {
                    sink.writeAll(Okio.buffer(Okio.source(is)));
                }
            }
        };
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadedVideo", selectedFilePath.substring((selectedFilePath.lastIndexOf('/') + 1), selectedFilePath.length()), videoFile)
                .build();

        System.out.println("Please Wait");

        postRequest(context,postUrl, postBodyImage, caller);
    }

    static void postRequest(Context context, String postUrl, RequestBody postBody, int caller) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.MINUTES);
        builder.readTimeout(10, TimeUnit.MINUTES);
        builder.writeTimeout(10, TimeUnit.MINUTES);

        OkHttpClient client = builder.build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                if(caller==0){
                    Subtitles.errorUi(context);
                }else{
                    Description.errorUi(context);
                }
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()
                System.out.println("Failed to Connect to Server");
            }

            @Override
            public void onResponse (Call call, Response response) throws IOException {
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()

                System.out.println("Running!");
                System.out.println("\nCOMPRESSING\n");
                String op;
                try {
                    String app_dir = "accessibility";
                    Timestamp ts=new Timestamp(System.currentTimeMillis());
                    op=Environment.getExternalStorageDirectory() + "/" + app_dir+"/"+ts.toString()+".mp4";
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + app_dir, ts.toString()+".mp4");
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                        System.out.println("we made accessibility!" + file.getAbsolutePath());
                    }
                    System.out.println("Writing file");
                    BufferedSink data = Okio.buffer(Okio.sink(file));
                    data.writeAll(response.body().source());
                    data.close();
                    Long endtime;
                    System.out.println("Done writing video file.");
                    endtime = System.currentTimeMillis();
                    System.out.println("Compressed Video!");
                    System.out.println("Compression took " + (endtime-starttime)/1000 + " seconds and " +(endtime-starttime)%1000 + " milliseconds");
                    if(caller==0){
                        Subtitles.updateUiOKHTTP(context,op);
                    }
                    else {
                        Description.updateUiOKHTTP(context,op);
                    }

                } catch (IOException e) {
                    if(caller==0){
                        Subtitles.errorUi(context);
                    }else{
                        Description.errorUi(context);
                    }
                    e.printStackTrace();
                }
            }
        });
    }


    public static boolean Decision_Compression(Context context, String inputPath, int priority){
        long duration = DecisionEngine.getVideoLength(context, inputPath);
        System.out.println("\n\nDURATION OF INPUT VIDEO: " + duration);
        System.out.println("USER PRIORITY IS " + priority);

        //getting average for local
        List<String[]> videos_local = DecisionEngine.getNearestFromCSV(context, 1, duration, 1);
        float local_average = 0;
        try {
            local_average = DecisionEngine.averageOut(videos_local, priority);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //getting average for remote
        List<String[]> videos_remote = DecisionEngine.getNearestFromCSV(context, 1, duration, 2);
        float remote_average = 0;
        try {
            remote_average = DecisionEngine.averageOut(videos_remote, priority);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("\nREMOTE AVERAGE: " + remote_average);
        System.out.println("\nLOCAL AVERAGE: " + local_average);

        //making offloading decision
        if(remote_average<local_average){
            System.out.println("\n\nOFFLOADING TASK");
            return true;
        }

        else{
            System.out.println("\n\nPERFORMING TASK LOCALLY");
            return false;
        }

    }

}

 **/