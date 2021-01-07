package com.example.accessibility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

import static android.view.View.VISIBLE;

public class Description extends AppCompatActivity {
    private static int RECORD = 1, FILES = 0;
    public static MediaRecorder myAudioRecorder;
    String predir = Environment.getExternalStorageDirectory() + "/accessibility";
    String inputPath;
    static AppCompatButton compress;
    static TextView descstatus;
    static VideoView descvideo;
    static ImageView loadinggif;
    static String output;
    public static long duration;
    static long endtime = 0, starttime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        AppCompatButton desc = findViewById(R.id.start_desc);
        inputPath = getIntent().getStringExtra("path");
        compress = findViewById(R.id.compress_desc);
        descstatus=findViewById(R.id.descstatus);
        descvideo=findViewById(R.id.descvideo);
        loadinggif=findViewById(R.id.loadinggif);
        compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    descstatus.setText("Compressing...");
                    GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loadinggif);
                    Glide.with(getApplicationContext()).load(R.drawable.loading).into(imageViewTarget);
                    //Compression.remote_compress(getApplicationContext(), predir, output,1);
                    Compression.local_compress(getApplicationContext(), predir, output,1);
                }catch (Exception e){
                    errorUi(getApplicationContext());
                    System.out.println("File Not Found");
                    e.printStackTrace();
                }
            }
        });
        desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickSrc();
            }
        });
    }
    public static void completedUi(Context context, String op){
        compress.setEnabled(true);
        descstatus.setText("Video Saved at:"+op);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loadinggif);
        Glide.with(context).load(R.drawable.completed).into(imageViewTarget);
    }
    public static void errorUi(Context context){
        descstatus.setText("An Error Has Occured");
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loadinggif);
        Glide.with(context).load(R.drawable.error).into(imageViewTarget);
    }

    public void pickSrc() {
        AlertDialog.Builder source = new AlertDialog.Builder(this);
        source.setTitle("Pick Source For Audio");
        String[] pictureDialogItems = {
                "Files",
                "Record"};
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        File videoFile = new File(inputPath);
        retriever.setDataSource(this, Uri.fromFile(videoFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(time);
        retriever.release();
        source.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int pick) {
                        switch (pick) {
                            case 0:
                                Intent intent = new Intent();
                                intent.setType("audio/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Audio "), FILES);
                                break;
                            case 1:
                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                String audioOp = predir + "/"+timestamp.toString()+".mp3";
                                myAudioRecorder = new MediaRecorder();
                                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                                myAudioRecorder.setOutputFile(audioOp);
                                try {
                                        descstatus.setText("Recording...");
                                        loadinggif.setEnabled(true);
                                        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loadinggif);
                                        Glide.with(getApplicationContext()).load(R.drawable.audio).into(imageViewTarget);
                                        descvideo.setVideoPath(inputPath);
                                        descvideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                mp.setVolume(0f, 0f);
                                                mp.setLooping(true);
                                            }
                                        });
                                        descvideo.start();
                                    myAudioRecorder.prepare();
                                    myAudioRecorder.start();
                                    Auto_Stop_Task as = new Auto_Stop_Task(audioOp);
                                    as.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    errorUi(getApplicationContext());
                                }
                            default:
                                break;
                        }
                    }
                });
        source.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String inputPath = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(),"Audio Not Picked", Toast.LENGTH_SHORT);
            System.out.println("Choosing source failed after picking source");
            errorUi(getApplicationContext());
            return;
        }
        if (requestCode == FILES) {
            System.out.println("Picked Files");
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                Log.d("d", "File Uri: " + uri.toString());
                // Get
                String path = PathExtracter.getPath(getApplicationContext(),uri);
                Log.d("d", "File Path: " + path);

               local_describe(getApplicationContext(),path,predir,inputPath);
                try {
                    starttime = System.currentTimeMillis();
                    //remote_describe(getApplicationContext(), path, predir, inputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    errorUi(getApplicationContext());
                }

            }
        }
        return;
    }

    public class Auto_Stop_Task extends AsyncTask<Void, Void, Integer> {
        //int flag=0;
        String audioOp;
        Auto_Stop_Task(String audioOp){
            this.audioOp=audioOp;
        }
        @Override
        protected Integer doInBackground(Void... arg0) {
            try {
                Thread.sleep(Description.duration);
                Description.myAudioRecorder.stop();
                Description.myAudioRecorder.reset();
                System.out.println("Done Recording");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        descvideo.stopPlayback();
                        descstatus.setText("Audio Saved at: "+audioOp);
                    }
                });

                //flag=1;
            } catch (InterruptedException e) {
                e.printStackTrace();
                errorUi(getApplicationContext());
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        descstatus.setText("Generating Video");
                        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(loadinggif);
                        Glide.with(getApplicationContext()).load(R.drawable.loading).into(imageViewTarget);
                    }
                });

                local_describe(getApplicationContext(),this.audioOp,predir,inputPath);
                //remote_describe(getApplicationContext(), this.audioOp, predir, inputPath);
            } catch (Exception e) {
                e.printStackTrace();
                errorUi(getApplicationContext());
            }

        }
    }
    public static String local_describe(Context context, String audioPath, String predir, String inputPath) {
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
                        //ffmpeg -i input.mp4 -i input.mp3 -c copy -map 0:v:0 -map 1:a:0 output.mp4
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        output = predir + "/" + timestamp.toString() + ".mp4";
                        String[] cmd = {"-i", inputPath, "-i", audioPath, "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", output};
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
                                errorUi(context);
                            }

                            @Override
                            public void onSuccess(String message) {
                                System.out.println("\n---------COMMAND\nSUCCESS\n");
                            }

                            @Override
                            public void onFinish() {
                                System.out.println("\n---------COMMAND\nFINISH\n");
                                System.out.println("Path Is:" + output);
                                completedUi(context,output);
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        // Handle if FFmpeg is already running
                        e.printStackTrace();
                        errorUi(context);
                    }
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
            System.out.println("\n---------\nISSUE\n");
            // Handle if FFmpeg is not supported by device
            errorUi(context);
        }
        System.out.println("DESCRIBER IS SENDING BACK:"+output);
        return output;
    }

    public static void remote_describe(Context context, String audioPath, String predir, String selectedFilePath) throws FileNotFoundException {
        String postUrl = "http://3.22.70.87:8080/addDescription";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ContentResolver contentResolver = context.getContentResolver();
        final String contentType = "video/mp4";
        final AssetFileDescriptor fd;
        fd = contentResolver.openAssetFileDescriptor(Uri.fromFile(new File(selectedFilePath)), "r");
        System.out.println(selectedFilePath);
        File audiofile = new File(audioPath);

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
                .addFormDataPart("audioFile", audioPath.substring((audioPath.lastIndexOf('/')+1)), RequestBody.create(MediaType.parse("audio/mp3"), audiofile))
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
                errorUi(context);
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()
                System.out.println("Failed to Connect to Server");
            }

            @Override
            public void onResponse (Call call, Response response) throws IOException {
                // In order to access thTextView inside the UI thread, the code is executed inside runOnUiThread()

                System.out.println("Running!");
                System.out.println("\nADDING DESCRIPTION\n");

                try {
                    String app_dir = "accessibility";
                    Timestamp ts=new Timestamp(System.currentTimeMillis());
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
                    System.out.println("Described Video!");
                    System.out.println("Overlaying audio took " + (endtime-starttime)/1000 + " seconds and " +(endtime-starttime)%1000 + " milliseconds");
                    String op=Environment.getExternalStorageDirectory() + "/" + app_dir+"/"+ts.toString()+".mp4";
                    completedUi(context,op);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorUi(context);
                }
            }
        });
    }
}