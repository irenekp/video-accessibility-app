package com.example.accessibility;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.sql.Timestamp;

public class Describer {
    public static Boolean[] done={false};
    public static String[] output = new String[1];
    public static String local_describe(Context context, String audioPath, String predir, String inputPath) {
        //desc_updates.setText("Creating New File");
        //video.stopPlayback();
        //Glide.with(this).load(R.drawable.loading).asGif().into(loading);
        //final String[] output = new String[1];
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
                        output[0] = predir + "/" + timestamp.toString() + ".mp4";
                        String[] cmd = {"-i", inputPath, "-i", audioPath, "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", output[0]};
                        // String[] cmd = {"-i", inputPath, "-vf","ass="+subtitlePath,output};
                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                            @Override
                            public void onStart() {
                                Log.d("LOG: Start");
                            }

                            @Override
                            public void onProgress(String message) {
                                Log.d("Second Task: Progress" + message);
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.d("LOG: Fail" + message);
                            }

                            @Override
                            public void onSuccess(String message) {
                                Log.d("LOG: Success");
                            }

                            @Override
                            public void onFinish() {
                                Log.d("LOG: Finish");
                                Log.d("Path Is:" + output[0]);
                                done[0]=true;
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
            Toast.makeText(context, "ERR!", Toast.LENGTH_SHORT);
            Log.d("LOG: ERR!");
            // Handle if FFmpeg is not supported by device
        }
        Log.d("LOG: DESCRIBER O/P:"+output[0]);
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
