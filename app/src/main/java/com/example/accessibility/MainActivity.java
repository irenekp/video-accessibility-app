    package com.example.accessibility;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.util.Log;
import java.io.File;
import java.text.ParseException;
import java.util.List;

    public class MainActivity extends AppCompatActivity {
    AppCompatButton upload;
    private int GALLERY = 1, CAMERA = 2;
    int priority = 1;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File appDir = new File(Environment.getExternalStorageDirectory() + "/"+"accessibility");
        if (!appDir.exists()) {
            appDir.mkdirs();
            Log.d("we made accessibility!"+appDir.getAbsolutePath());
        }

        //checking priority mode


        upload=findViewById(R.id.upload_video);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch prioritySwitch = findViewById(R.id.priority);
                if(!prioritySwitch.isChecked()){priority = 1;}
                else{priority=2;}
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
            Log.d("Error: Choosing source failed");
            Toast.makeText(this,"Unable to Choose Source", Toast.LENGTH_SHORT);
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                String selectedVideoPath = PathExtracter.galleryPath(getApplicationContext(),contentURI);
                Log.d("SELECTED VIDEO PATH\n\n\n\n" + selectedVideoPath);
                inputPath=selectedVideoPath;
            }
            else{
                Log.d("Error: Choosing from gallery failed");
                Toast.makeText(this,"No Gallery File Chosen", Toast.LENGTH_SHORT);
            }
        } else if (requestCode == CAMERA) {

            Uri contentURI = data.getData();
            String recordedVideoPath = PathExtracter.galleryPath(getApplicationContext(),contentURI);
            inputPath=recordedVideoPath;
            Log.d("RECORDED VIDEO PATH\n\n\n\n" + recordedVideoPath);

        }
        Intent selectAccessibility =new Intent(this, SelectAccessibility.class);
        selectAccessibility.putExtra("priority", Integer.toString(priority));
        selectAccessibility.putExtra("path",inputPath);
        startActivity(selectAccessibility);
        return;
    }

}