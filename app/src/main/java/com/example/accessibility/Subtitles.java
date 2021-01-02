package com.example.accessibility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.database.Cursor;
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

import java.io.File;
import java.sql.Timestamp;

public class Subtitles extends AppCompatActivity {
    TextView SrtStatus;
    AppCompatButton Start;
    String app_dir="accessibility";
    String inputPath;
    String subtitlePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitles);
        File appDir = new File(Environment.getExternalStorageDirectory() + "/"+app_dir);
        if (!appDir.exists()) {
            appDir.mkdirs();
            System.out.println("we made accessibility!"+appDir.getAbsolutePath());
        }
        inputPath=getIntent().getStringExtra("path");
        SrtStatus=findViewById(R.id.srt_status);
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
                FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                try {
                    ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                        @Override
                        public void onStart() {
                            Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onFinish() {
                            Toast.makeText(getApplicationContext(), "onFinish", Toast.LENGTH_SHORT);
                            System.out.println("\n---------\nFINISH\n");
                            FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                            try {
                                // to execute "ffmpeg -version" command you just need to pass "-version"
                                //-i 'test video.mp4' -vf ass=subtitles.ass mysubtitledmovie.mp4
                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                String output= Environment.getExternalStorageDirectory() +"/"+ app_dir+"/subs.ass";
                                //String output= Environment.getExternalStorageDirectory() +"/"+ app_dir+"/"+timestamp.toString()+".mp4";
                                String[] cmd = {"-i",subtitlePath,output};
                                // String[] cmd = {"-i", inputPath, "-vf","ass="+subtitlePath,output};
                                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                                    @Override
                                    public void onStart() {
                                    }
                                    @Override
                                    public void onProgress(String message) {
                                        System.out.println("First Task: Progress"+message);
                                    }
                                    @Override
                                    public void onFailure(String message) {
                                    }
                                    @Override
                                    public void onSuccess(String message) {
                                    }
                                    @Override
                                    public void onFinish() {
                                         System.out.println("Path Is:"+output);
                                        FFmpeg ffmpeg2 = FFmpeg.getInstance(getApplicationContext());
                                        try {
                                            ffmpeg2.loadBinary(new LoadBinaryResponseHandler() {
                                                @Override
                                                public void onStart() {
                                                    Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT);
                                                }

                                                @Override
                                                public void onFailure() {
                                                    Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT);
                                                }

                                                @Override
                                                public void onSuccess() {
                                                    Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT);
                                                }

                                                @Override
                                                public void onFinish() {
                                                    Toast.makeText(getApplicationContext(), "onFinish", Toast.LENGTH_SHORT);
                                                    FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                                                    try {
                                                        // to execute "ffmpeg -version" command you just need to pass "-version"
                                                        //-i 'test video.mp4' -vf ass=subtitles.ass mysubtitledmovie.mp4
                                                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                                        //String output= Environment.getExternalStorageDirectory() +"/"+ app_dir+"/subs.ass";
                                                        String predir=Environment.getExternalStorageDirectory() +"/"+ app_dir;
                                                        String output= Environment.getExternalStorageDirectory() +"/"+ app_dir+"/"+timestamp.toString()+".mp4";
                                                        // String[] cmd = {"-i",input,"-i",subs,"-c","copy","-c:s","mov_text",output};
                                                        String[] cmd = {"-i",inputPath,"-i",subtitlePath,"-c","copy","-c:s","mov_text",output};
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
                                                                System.out.println("Path Is:"+output);

                                                            }
                                                        });
                                                    } catch (FFmpegCommandAlreadyRunningException e) {
                                                        // Handle if FFmpeg is already running
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (FFmpegNotSupportedException e) {
                                            Toast.makeText(getApplicationContext(), "issue", Toast.LENGTH_SHORT);
                                            System.out.println("\n---------\nISSUE\n");
                                            // Handle if FFmpeg is not supported by device
                                        }
                                    }
                                });
                            } catch (FFmpegCommandAlreadyRunningException e) {
                                // Handle if FFmpeg is already running
                                System.out.println("\n---------COMMAND\nISSUE\n");
                            }
                        }
                    });
                } catch (FFmpegNotSupportedException e) {
                    Toast.makeText(getApplicationContext(), "issue", Toast.LENGTH_SHORT);
                    System.out.println("\n---------\nISSUE\n");
                    // Handle if FFmpeg is not supported by device
                }
            }
        });
    }
    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("d", "File Uri: " + uri.toString());
                    // Get
                    String path = getPath(uri);
                    Log.d("d", "File Path: " + path);
                    subtitlePath=path;
                    SrtStatus.setText(path);
                    Start.setEnabled(true);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
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
    }
}