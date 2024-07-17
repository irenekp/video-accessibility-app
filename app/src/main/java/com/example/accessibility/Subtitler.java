package com.example.accessibility;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.sql.Timestamp;


public class Subtitler {
    public static Boolean[] done={false};
    public static String[] output = new String[1];
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
                    Log.d("\n---------\nFINISH\n");
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
                                    output[0] = predir+"/"+timestamp.toString()+".mp4";
                                    // String[] cmd = {"-i",input,"-i",subs,"-c","copy","-c:s","mov_text",output};
                                    String[] cmd = {"-i",inputPath,"-i",subtitlePath,"-c","copy","-c:s","mov_text", output[0]};
                                    // String[] cmd = {"-i", inputPath, "-vf","ass="+subtitlePath,output};
                                    ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                                        @Override
                                        public void onStart() {
                                        }
                                        @Override
                                        public void onProgress(String message) {
                                            Log.d("Second Task: Progress"+message);
                                        }
                                        @Override
                                        public void onFailure(String message) {
                                        }
                                        @Override
                                        public void onSuccess(String message) {
                                        }
                                        @Override
                                        public void onFinish() {
                                            Log.d("Path Is:"+ output[0]);
                                            done[0]=true;
                                        }
                                    });
                                } catch (FFmpegCommandAlreadyRunningException e) {
                                    // Handle if FFmpeg is already running
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (FFmpegNotSupportedException e) {
                        Toast.makeText(context, "ERR!", Toast.LENGTH_SHORT);
                        // Handle if FFmpeg is not supported by device
                    }
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(context, "issue", Toast.LENGTH_SHORT);
            // Handle if FFmpeg is not supported by device
        }
        return returnforme(output[0]);
    }
    static String returnforme(String op){
        if(done[0]==true){
            return op;
        }
        while(done[0]!=true){};
        return output[0];
    }
}
