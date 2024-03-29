package com.example.accessibility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelectAccessibility extends AppCompatActivity {
    AppCompatButton subtitles;
    AppCompatButton description;
    String inputPath=null;
    int priority = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_accessibility);
        subtitles=findViewById(R.id.subtitle);
        description=findViewById(R.id.audio_desc);
        Intent intent = getIntent();
        inputPath=intent.getStringExtra("path");
        priority = Integer.parseInt(intent.getStringExtra("priority"));

        subtitles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent subs =new Intent(getApplicationContext(), Subtitles.class);
                subs.putExtra("priority", Integer.toString(priority));
                subs.putExtra("path",inputPath);
                startActivity(subs);
            }
        });
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent desc =new Intent(getApplicationContext(), Description.class);
                desc.putExtra("priority", Integer.toString(priority));
                desc.putExtra("path",inputPath);
                startActivity(desc);
            }
        });
    }
}