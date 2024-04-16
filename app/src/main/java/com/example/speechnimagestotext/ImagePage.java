package com.example.speechnimagestotext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ImagePage extends AppCompatActivity {

    ConstraintLayout btnChooseImage, btnChooseCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_page);

        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnChooseCamera = findViewById(R.id.btnChooseCamera);
    }

    public void btnChooseImage(View view) {

        Intent intent = new Intent(ImagePage.this, ImageToTextPage.class);
        startActivity(intent);
    }

    public void btnChooseCamera(View view) {

        Intent intent = new Intent(ImagePage.this, CameraToTextPage.class);
        startActivity(intent);
    }

    public void btnBackImages(View view) {
        super.onBackPressed();
    }
}