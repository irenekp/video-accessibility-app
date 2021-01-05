package com.example.accessibility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
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
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.nio.file.Path;
import java.sql.Timestamp;

import static android.view.View.VISIBLE;

public class Description extends AppCompatActivity {
    private static int RECORD = 1, FILES = 0;
    public static MediaRecorder myAudioRecorder;
    //TextView desc_updates;
    String predir = Environment.getExternalStorageDirectory() + "/accessibility";
    String inputPath;
   // ImageView loading;
    VideoView video;
    AppCompatButton compress;
    //String pre_comp_op;
    static String output;
    public static long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        //desc_updates = findViewById(R.id.description_updates);
        //loading = findViewById(R.id.audio_rec);
        video = findViewById(R.id.desc_video);
        AppCompatButton desc = findViewById(R.id.start_desc);
        inputPath = getIntent().getStringExtra("path");
        compress = findViewById(R.id.compress_desc);
        compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loading.setVisibility(View.VISIBLE);
                try {
                    Compression.remote_compress(getApplicationContext(), predir, output);
                    //System.out.println("RECIVED COMPRESSED:" + compOp);
          //          desc_updates.setText("Video Compressed");
                    //loading.setVisibility(View.INVISIBLE);
                }catch (Exception e){
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
                                //video.setVideoPath(inputPath);
                                //video.start();
                                //Glide.with(getApplicationContext()).load(R.drawable.audio).asGif().into(loading);
                                //video.setVisibility(VISIBLE);
                                //loading.setVisibility(VISIBLE);
                                try {
                                    myAudioRecorder.prepare();
                                    myAudioRecorder.start();
            //                        desc_updates.setText("Recording");
                                    Auto_Stop_Task as = new Auto_Stop_Task(audioOp);
                                    as.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                //SrtStatus.setText(path);
                //desc_updates.setText("Audio Src:"+path);
                // Get the file instance
                // File file = new File(path);
                // Initiate the upload
                local_describe(getApplicationContext(),path,predir,inputPath);
                //pre_comp_op=output;
                //System.out.println("RECIEVED FROM DESCRIBER:"+);
                /**
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        desc_updates.setText("Described File is Generated at:"+output);
                        loading.setVisibility(View.INVISIBLE);
                    }
                });
                 **/
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //desc_updates.setText("Stopped");
                  //      desc_updates.setText("Audio File at:"+audioOp);
                    }
                });
                System.out.println("Done Recording");
                //flag=1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            local_describe(getApplicationContext(),this.audioOp,predir,inputPath);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // desc_updates.setText("Described File is Generated at:"+output);
                }
            });
            //finishedRecording(audioOp);
        }
    }
    public static String local_describe(Context context, String audioPath, String predir, String inputPath) {
        //desc_updates.setText("Creating New File");
        //video.stopPlayback();
        //Glide.with(this).load(R.drawable.loading).asGif().into(loading);
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
                            }

                            @Override
                            public void onSuccess(String message) {
                                System.out.println("\n---------COMMAND\nSUCCESS\n");
                            }

                            @Override
                            public void onFinish() {
                                System.out.println("\n---------COMMAND\nFINISH\n");
                                System.out.println("Path Is:" + output);
                                //done[0]=true;
                                //desc_updates.setText("Video Created");
                                //loading.setVisibility(View.INVISIBLE);
                                //pre_comp_op = output;
                                //video.setVideoPath(output);
                                //video.start();
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
        System.out.println("DESCRIBER IS SENDIG BACK:"+output);
        return output;
    }
}