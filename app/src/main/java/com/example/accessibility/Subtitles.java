package com.example.accessibility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ContentResolver;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.sql.Timestamp;
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
    //TextView SrtStatus;
    AppCompatButton Start;
    String predir;
    String inputPath;
    String subtitlePath;
    static String output;

    static long starttime = 0;
    static long endtime=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitles);
        predir=Environment.getExternalStorageDirectory() + "/"+"accessibility";
        File appDir = new File(predir);
        if (!appDir.exists()) {
            appDir.mkdirs();
            System.out.println("we made accessibility!"+appDir.getAbsolutePath());
        }
        inputPath=getIntent().getStringExtra("path");
        //SrtStatus=findViewById(R.id.srt_status);
        AppCompatButton local=findViewById(R.id.local);
        Start=findViewById(R.id.start_subtitle);
        Start.setEnabled(false);
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starttime = System.currentTimeMillis();
                //String output=local_Subtitler(getApplicationContext(),predir,inputPath,subtitlePath);

                try {
                    remote_subtitler(getApplicationContext(), predir, inputPath, subtitlePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
        AppCompatButton Compress=findViewById(R.id.sub_compress);
        Compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //SrtStatus.setText("Compressing.");
                    }
                });
                try {
                    Compression.remote_compress(getApplicationContext(), predir, inputPath);
                }catch (Exception e){
                    System.out.println("Compression File Not Found");
                    e.printStackTrace();
                }
            }
        });
    }
    private static final int FILE_SELECT_CODE = 0;

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
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("d", "File Uri: " + uri.toString());
                    String path = PathExtracter.getPath(getApplicationContext(),uri);
                    Log.d("d", "File Path: " + path);
                    subtitlePath=path;
                    //SrtStatus.setText("Subtitles At Path:"+path);
                    Start.setEnabled(true);
                }
                break;
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
                                        }
                                    });
                                } catch (FFmpegCommandAlreadyRunningException e) {
                                    // Handle if FFmpeg is already running
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (FFmpegNotSupportedException e) {
                        Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
                        System.out.println("\n---------\nISSUE\n");
                        // Handle if FFmpeg is not supported by device
                    }
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
            System.out.println("\n---------\nISSUE\n");
            // Handle if FFmpeg is not supported by device
        }
        return output;
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
                }
            }
        };


        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadedVideo", selectedFilePath.substring((selectedFilePath.lastIndexOf('/') + 1)), videoFile)
                .addFormDataPart("subtitleFile", subtitlePath.substring((subtitlePath.lastIndexOf('/')+1)), RequestBody.create(MediaType.parse("text/csv"), subfile))
                .build();

        System.out.println("Please Wait");

        postRequest(postUrl, postBodyImage);
    }

    static void postRequest(String postUrl, RequestBody postBody) {

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

                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()
                System.out.println("Failed to Connect to Server");
            }

            @Override
            public void onResponse (Call call, Response response) throws IOException {
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()

                System.out.println("Running!");
                System.out.println("\nADDING SUBTITLES\n");

                try {
                    String app_dir = "accessibility";
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + app_dir, "subbed_video.mp4");
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
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
}