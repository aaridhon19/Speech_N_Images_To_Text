package com.example.speechnimagestotext;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraToTextPage extends AppCompatActivity {

    TextView getCameraAgain;

    ImageView deleteTextCamera, copyTextCamera;

    EditText editTextCamera;

    Uri imageUri;

    TextRecognizer textRecognizer;

    LinearLayout btnDownloadCamera;

    String mText, mTextSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_to_text_page);

        deleteTextCamera = findViewById(R.id.btnDeleteCamera);

        copyTextCamera = findViewById(R.id.btnCopyCamera);

        editTextCamera = findViewById(R.id.edTextCamera);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //Download
        btnDownloadCamera = findViewById(R.id.btnDownloadLinearCamera);
        btnDownloadCamera.setVisibility(View.INVISIBLE);

        //CustomSaveLayout
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_save_layout);
        LinearLayout btnSave = dialog.findViewById(R.id.btnSave);
        LinearLayout btnCancelSave = dialog.findViewById(R.id.btnCancelSave);
        EditText edTextFile = dialog.findViewById(R.id.edTextFile);

        getCameraAgain = findViewById(R.id.btnChooseAgainCamera);

        getCameraAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.with(CameraToTextPage.this)
                        .cameraOnly()    //User can only capture image using Camera
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        btnDownloadCamera.setOnClickListener(v -> dialog.show());

        btnCancelSave.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {

            mText = editTextCamera.getText().toString().trim();
            mTextSave = edTextFile.getText().toString().trim();

            if (isStoragePermissionGranted()) {

                if (!mTextSave.equals("")) {
                    // Create a file with .txt extension in the Downloads directory
                    String fileDirectory = Environment.DIRECTORY_DOWNLOADS;
                    String fileName = mTextSave + ".txt";
                    File directory = Environment.getExternalStoragePublicDirectory(fileDirectory);
                    File file = new File(directory, fileName);

                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(mText.getBytes());
                        fos.close();

                        // Add the saved file to MediaStore
                        ContentResolver contentResolver = getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, mTextSave);
                        values.put(MediaStore.Files.FileColumns.MIME_TYPE, ".txt");
                        values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
                        Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values);

                        if (uri != null) {
                            Toast.makeText(this, "File Text saved to Media Download", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
                        }

                        // Clear the EditText
                        editTextCamera.setText("");
                        dialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mTextSave.isEmpty();
                    // If the Text field is empty show corresponding Toast message
                    Toast.makeText(CameraToTextPage.this, "File name can not be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        copyTextCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editTextCamera.getText().toString();

                if (text.isEmpty()) {

                    Toast.makeText(CameraToTextPage.this, "There is Empty", Toast.LENGTH_SHORT).show();

                } else {

                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CameraToTextPage.this.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Data", editTextCamera.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(CameraToTextPage.this, "Text is Copy", Toast.LENGTH_SHORT).show();

                }

            }
        });

        deleteTextCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editTextCamera.getText().toString();

                if (text.isEmpty()) {

                    Toast.makeText(CameraToTextPage.this, "Text is Empty", Toast.LENGTH_SHORT).show();

                } else {

                    editTextCamera.setText("");

                }

            }
        });

        editTextCamera.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    btnDownloadCamera.setVisibility(View.GONE);
                } else {
                    btnDownloadCamera.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Permission is granted
                return true;
            } else {
                //Permission is revoked
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            //Permission is granted
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUri = data.getData();

                Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();

                recognizeText();
            }

        } else {

            Toast.makeText(this, "Image Not Selected", Toast.LENGTH_SHORT).show();

        }
    }

    private void recognizeText() {

        if (imageUri != null) {

            try {
                InputImage inputImage = InputImage.fromFilePath(CameraToTextPage.this, imageUri);

                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {

                                String recognizeText = text.getText();
                                editTextCamera.setText(recognizeText);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(CameraToTextPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void btnBackCamera(View view) {
        super.onBackPressed();
    }
}