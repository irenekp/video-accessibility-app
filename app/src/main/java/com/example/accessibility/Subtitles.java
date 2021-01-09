package com.example.accessibility;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class Subtitles extends AppCompatActivity {

    static Subtitles obj;
    private static int FILE_SELECT_CODE = 0;

    AppCompatButton Start;
    String predir;
    String inputPath;
    String subtitlePath;
    static String output;
    static long starttime = 0;
    static long endtime=0;
    static TextView subsstatus;
    static ImageView subsimage;
    static AppCompatButton Compress;
    static AppCompatButton serverBtn;
    int priority;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitles);
        obj = this;
        predir=Environment.getExternalStorageDirectory() + "/"+"accessibility";
        File appDir = new File(predir);
        if (!appDir.exists()) {
            appDir.mkdirs();
            System.out.println("we made accessibility!"+appDir.getAbsolutePath());
        }
        inputPath=getIntent().getStringExtra("path");
        priority = Integer.parseInt(getIntent().getStringExtra("priority"));
        serverBtn = findViewById(R.id.server);
        Compress=findViewById(R.id.sub_compress);
        AppCompatButton local=findViewById(R.id.local);
        Start=findViewById(R.id.start_subtitle);
        Start.setEnabled(false);
        subsstatus=findViewById(R.id.subsstatus);
        subsimage=findViewById(R.id.subsimage);
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subsstatus.setText("Please Wait, Subtitling...");
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
                Glide.with(getApplicationContext()).load(R.drawable.loading).into(imageViewTarget);
                starttime = System.currentTimeMillis();
                String[]x={inputPath,subtitlePath};
                if(!Decision_Subtitles(inputPath, priority)) {
                    output = local_Subtitler(getApplicationContext(), predir, inputPath, subtitlePath);
                }else{
                try {
                    remote_subtitler(getApplicationContext(), predir, inputPath, subtitlePath);
                } catch (Exception e) {
                    errorUi(getApplicationContext());
                    e.printStackTrace();
                }
                }
                Compress.setEnabled(true);
            }
        });

        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    subsstatus.setText("Please Wait, Generating...");
                    GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
                    Glide.with(getApplicationContext()).load(R.drawable.loading).into(imageViewTarget);
                    generateSRT(getApplicationContext(), predir, inputPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subsstatus.setText("Please Wait, Compressing...");
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
                Glide.with(getApplicationContext()).load(R.drawable.loading).into(imageViewTarget);
               try{
                   if(Decision_Compression(getApplicationContext(), inputPath, priority)){
                       remote_compress(getApplicationContext(), predir, output,1);

                   }
                   else {
                       local_compress(getApplicationContext(), predir, output, 1);
                   }
                }catch(Exception e){
                    System.out.println("Compression File Not Found");
                    errorUi(getApplicationContext());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //ToDo: Make type SRT mandatory
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            errorUi(getApplicationContext());
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public static void errorUiOKHTTP(Context context){
        obj.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                subsstatus.setText("An Error Has Occured");
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
                Glide.with(context).load(R.drawable.error).into(imageViewTarget);

            }
        });
    }

    public static void errorUi(Context context){
        subsstatus.setText("An Error Has Occured");
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
        Glide.with(context).load(R.drawable.error).into(imageViewTarget);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if( requestCode == FILE_SELECT_CODE) {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("d", "File Uri: " + uri.toString());
                    String path = PathExtracter.getPath(getApplicationContext(), uri);
                    Log.d("d", "File Path: " + path);
                    subtitlePath = path;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            subsstatus.setText("SRT location: " + path);
                        }
                    });
                    Start.setEnabled(true);
                }
            }

        super.onActivityResult(requestCode, resultCode, data);
    }
    public static String local_Subtitler(Context context, String predir, String inputPath, String subtitlePath){
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Toast.makeText(context, "onStart", Toast.LENGTH_SHORT);
                }

                @Override
                public void onFailure() {
                    Toast.makeText(context, "onFailure", Toast.LENGTH_SHORT);
                    errorUi(context);
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(context, "onSuccess", Toast.LENGTH_SHORT);
                }

                @Override
                public void onFinish() {
                    Toast.makeText(context, "onFinish", Toast.LENGTH_SHORT);
                    System.out.println("\n---------\nFINISH\n");
                    FFmpeg ffmpeg2 = FFmpeg.getInstance(context);
                    try {
                        ffmpeg2.loadBinary(new LoadBinaryResponseHandler() {
                            @Override
                            public void onStart() {
                                Toast.makeText(context, "onStart", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, "onFailure", Toast.LENGTH_SHORT);
                                errorUi(context);
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
                                    // to execute "ffmpeg -version" command you just need to pass "-version"
                                    //-i 'test video.mp4' -vf ass=subtitles.ass mysubtitledmovie.mp4
                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    //String output= Environment.getExternalStorageDirectory() +"/"+ app_dir+"/subs.ass";
                                    output= predir+"/"+timestamp.toString()+".mp4";
                                    // String[] cmd = {"-i",input,"-i",subs,"-c","copy","-c:s","mov_text",output};
                                    String[] cmd = {"-i",inputPath,"-i",subtitlePath,"-c","copy","-c:s","mov_text", output};
                                    // String[] cmd = {"-i", inputPath, "-vf","ass="+subtitlePath,output};
                                    ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                                        @Override
                                        public void onStart() {
                                            System.out.println("\n---------COMMAND\nSTART\n");
                                        }
                                        @Override
                                        public void onProgress(String message) {
                                            System.out.println("Second Task: Progress"+message);
                                        }
                                        @Override
                                        public void onFailure(String message) {
                                            System.out.println("\n---------COMMAND\nFAILURE\n" + message);
                                            errorUi(context);
                                        }
                                        @Override
                                        public void onSuccess(String message) {
                                            System.out.println("\n---------COMMAND\nSUCCESS\n");
                                        }
                                        @Override
                                        public void onFinish() {
                                            System.out.println("\n---------COMMAND\nFINISH\n");
                                            System.out.println("Path Is:"+ output);
                                           // done[0]=true;
                                            updateUi(context,output);
                                        }
                                    });
                                } catch (FFmpegCommandAlreadyRunningException e) {
                                    // Handle if FFmpeg is already running
                                    errorUi(context);
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (FFmpegNotSupportedException e) {
                        errorUi(context);
                        Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
                        System.out.println("\n---------\nISSUE\n");
                        // Handle if FFmpeg is not supported by device
                    }
                }
            });
        } catch (FFmpegNotSupportedException e) {
            errorUi(context);
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
            System.out.println("\n---------\nISSUE\n");
            // Handle if FFmpeg is not supported by device
        }
        return output;
    }
    public static void updateUi(Context context, String op){
        subsstatus.setText("Done! Output Video Path:"+op);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
        Glide.with(context).load(R.drawable.completed).into(imageViewTarget);
    }

    public static void updateUiOKHTTP(Context context, String op){
        obj.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                subsstatus.setText("Done! Output Video Path:"+op);
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(subsimage);
                Glide.with(context).load(R.drawable.completed).into(imageViewTarget);
            }
        });
    }

    
    public static void remote_subtitler(Context context, String predir, String selectedFilePath, String subtitlePath) throws FileNotFoundException {
        String postUrl = "http://3.22.70.87:8080/addSubtitles";

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ContentResolver contentResolver = context.getContentResolver();
        final String contentType = "video/mp4";
        final AssetFileDescriptor fd;
        fd = contentResolver.openAssetFileDescriptor(Uri.fromFile(new File(selectedFilePath)), "r");
        System.out.println(selectedFilePath);
        File subfile = new File(subtitlePath);

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
                }catch (Exception e){
                    errorUiOKHTTP(context);
                }
            }
        };


        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadedVideo", selectedFilePath.substring((selectedFilePath.lastIndexOf('/') + 1)), videoFile)
                .addFormDataPart("subtitleFile", subtitlePath.substring((subtitlePath.lastIndexOf('/')+1)), RequestBody.create(MediaType.parse("text/csv"), subfile))
                .build();

        System.out.println("Please Wait");

        postRequest(context,postUrl, postBodyImage);
    }

    static void postRequest(Context context, String postUrl, RequestBody postBody) {
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
                errorUiOKHTTP(context);
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()
                System.out.println("Failed to Connect to Server");
            }

            @Override
            public void onResponse (Call call, Response response) throws IOException {
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()

                System.out.println("Running!");
                System.out.println("\nADDING SUBTITLES\n");
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

                    System.out.println("Done writing video file.");
                    endtime = System.currentTimeMillis();
                    System.out.println("Subtitled Video!");

                    System.out.println("Subbing took " + (endtime-starttime)/1000 + " seconds and " +(endtime-starttime)%1000 + " milliseconds");
                    updateUiOKHTTP(context,op);
                } catch (IOException e) {
                    errorUiOKHTTP(context);
                    e.printStackTrace();
                }
            }

        });
    }

    public boolean Decision_Subtitles(String inputPath, int priority){
        long duration = DecisionEngine.getVideoLength(getApplicationContext(), inputPath);
        System.out.println("\n\nDURATION OF INPUT VIDEO: " + duration);
        System.out.println("USER PRIORITY IS " + priority);

        //getting average for local
        List<String[]> videos_local = DecisionEngine.getNearestFromCSV(getApplicationContext(), 3, duration, 1);
        float local_average = 0;
        try {
            local_average = DecisionEngine.averageOut(videos_local, priority);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //getting average for remote
        List<String[]> videos_remote = DecisionEngine.getNearestFromCSV(getApplicationContext(), 3, duration, 2);
        float remote_average = 0;
        try {
            remote_average = DecisionEngine.averageOut(videos_remote, priority);
        } catch (ParseException e) {
            e.printStackTrace();
        }

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

    /**
    private String getPath(Uri uri) {
        String path;
        Cursor cur = this.getContentResolver().query(uri,
                null, null, null, null);
        if (cur == null) {
            path = uri.getPath();
        } else {
            cur.moveToFirst();
            int idx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            path = cur.getString(idx);
            cur.close();
        }
        return path;
    }**/

    //generating srt and downloading
    public static void generateSRT(Context context, String predir, String selectedFilePath) throws FileNotFoundException {
        String postUrl = "http://3.22.70.87:8080/generateSRT";

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
                }catch (Exception e){
                    errorUiOKHTTP(context);
                }
            }
        };


        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadedVideo", selectedFilePath.substring((selectedFilePath.lastIndexOf('/') + 1)), videoFile)
                .build();

        System.out.println("Please Wait");

        postRequest2(context,postUrl, postBodyImage);
    }

    static void postRequest2(Context context, String postUrl, RequestBody postBody) {

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
                errorUi(context);
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()
                System.out.println("Failed to Connect to Server");
            }

            @Override
            public void onResponse (Call call, Response response) throws IOException {
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()

                System.out.println("Running!");
                System.out.println("\nADDING SUBTITLES\n");
                String op;
                try {
                    String app_dir = "accessibility";
                    Timestamp ts=new Timestamp(System.currentTimeMillis());
                    op=Environment.getExternalStorageDirectory() + "/" + app_dir+"/"+ts.toString()+".srt";
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + app_dir, ts.toString()+".srt");
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                        System.out.println("we made accessibility!" + file.getAbsolutePath());
                    }
                    System.out.println("Writing file");

                    BufferedSink data = Okio.buffer(Okio.sink(file));
                    System.out.println(response.body().source().toString());
                    data.writeAll(response.body().source());
                    data.close();
                    Long endtime;


                    System.out.println("Done writing srt file.");
                    updateUiOKHTTP(context,op);
                } catch (IOException e) {
                    errorUiOKHTTP(context);
                    e.printStackTrace();
                }
            }

        });
    }



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
                                Subtitles.updateUi(context,output[0]);

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
                    Subtitles.errorUiOKHTTP(context);
                }else{
                    Description.errorUiOKHTTP(context);
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
                        updateUiOKHTTP(context,op);

                } catch (IOException e) {
                        Subtitles.errorUiOKHTTP(context);
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