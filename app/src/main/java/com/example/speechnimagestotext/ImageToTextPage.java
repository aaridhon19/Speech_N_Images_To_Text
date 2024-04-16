package com.example.speechnimagestotext;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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

public class ImageToTextPage extends AppCompatActivity {

    ImageView delete, copy;

    TextView getImageAgain;

    EditText editTextImage;

    Uri imageUri;

    TextRecognizer textRecognizer;

    LinearLayout btnDownloadImage;

    String mText, mTextSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text_page);

        delete = findViewById(R.id.btnDeleteImage);

        copy = findViewById(R.id.btnCopyImage);

        getImageAgain = findViewById(R.id.btnChooseAgainImage);

        editTextImage = findViewById(R.id.edTextImage);

        //Download
        btnDownloadImage = findViewById(R.id.btnDownloadLinearImage);
        btnDownloadImage.setVisibility(View.INVISIBLE);

        //CustomSaveLayout
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_save_layout);
        LinearLayout btnSave = dialog.findViewById(R.id.btnSave);
        LinearLayout btnCancelSave = dialog.findViewById(R.id.btnCancelSave);
        EditText edTextFile = dialog.findViewById(R.id.edTextFile);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        getImageAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    ImagePicker.with(ImageToTextPage.this)
                            .galleryOnly()    //User can only select image from Gallery
                            .crop()                    //Crop image(Optional), Check Customization for more option
                            .compress(1024)            //Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                            .start();    //Default Request Code is ImagePicker.REQUEST_CODE
                }
            }
        });

        btnDownloadImage.setOnClickListener(v -> dialog.show());

        btnCancelSave.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {

            mText = editTextImage.getText().toString().trim();
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
                        editTextImage.setText("");
                        dialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mTextSave.isEmpty();
                    // If the Text field is empty show corresponding Toast message
                    Toast.makeText(ImageToTextPage.this, "File name can not be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editTextImage.getText().toString();

                if (text.isEmpty()) {

                    Toast.makeText(ImageToTextPage.this, "There is Empty", Toast.LENGTH_SHORT).show();

                } else {

                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(ImageToTextPage.this.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Data", editTextImage.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);

                    Toast.makeText(ImageToTextPage.this, "Text is Copy", Toast.LENGTH_SHORT).show();

                }

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editTextImage.getText().toString();

                if (text.isEmpty()) {

                    Toast.makeText(ImageToTextPage.this, "Text is Empty", Toast.LENGTH_SHORT).show();
                } else {

                    editTextImage.setText("");

                }

            }
        });

        editTextImage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    btnDownloadImage.setVisibility(View.GONE);
                } else {
                    btnDownloadImage.setVisibility(View.VISIBLE);
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
                InputImage inputImage = InputImage.fromFilePath(ImageToTextPage.this, imageUri);

                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {

                                String recognizeText = text.getText();
                                editTextImage.setText(recognizeText);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(ImageToTextPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnBackImage(View view) {
        super.onBackPressed();
    }
}