package com.example.accessibility;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    AppCompatButton upload;
    private int GALLERY = 1, CAMERA = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upload=findViewById(R.id.upload_video);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source();
            }
        });

    }
    private void Source() {
        AlertDialog.Builder source = new AlertDialog.Builder(this);
        source.setTitle("Pick Source For Video");
        String[] pictureDialogItems = {
                "Gallery",
                "Record"};
        source.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int pick) {
                        switch (pick) {
                            case 0:
                                Intent gallery = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(gallery, GALLERY);
                                break;
                            case 1:
                                Intent record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                startActivityForResult(record, CAMERA);
                                break;
                        }
                    }
                });
        source.show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String inputPath=null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            System.out.println("Choosing source failed after picking source");
            return;
        }
        if (requestCode == GALLERY) {
            System.out.println("Picked Gallery");
            if (data != null) {
                Uri contentURI = data.getData();
                String selectedVideoPath = makePath(contentURI);
                System.out.println("VIDEO PATH\n\n\n\n" + selectedVideoPath);
                //saveVideoToInternalStorage(selectedVideoPath);
                //videoView.setVideoURI(contentURI);
                //videoView.requestFocus();
                //videoView.start();
                inputPath=selectedVideoPath;
            }
            else{
                System.out.println("Data was null in Gallery");
            }
        } else if (requestCode == CAMERA) {
            Uri contentURI = data.getData();
            String recordedVideoPath = makePath(contentURI);
            //saveVideoToInternalStorage(recordedVideoPath);
            //videoView.setVideoURI(contentURI);
            //videoView.requestFocus();
            //videoView.start();
            inputPath=recordedVideoPath;
        }
        Intent selectAccessibility =new Intent(this, SelectAccessibility.class);
        selectAccessibility.putExtra("path",inputPath);
        startActivity(selectAccessibility);
        return;
    }
    public String makePath(Uri resource) {
        String[] mediaStore = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(resource, mediaStore, null, null, null);
        if (cursor != null) {
            int clm_idx = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(clm_idx);
        } else
            return null;
    }
}