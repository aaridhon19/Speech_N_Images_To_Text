package com.example.speechnimagestotext;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.speechnimagestotext.R.drawable.btn_choose_image;
import static com.example.speechnimagestotext.R.drawable.btn_speech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechPage extends AppCompatActivity {

    private final int CHOOSE_FROM_DEVICE = 42;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;

    protected static final int RESULT_SPEECH = 1;

    TextView btnChooseSpeech, descSpeech, tvSpeechToText, tvTextToSpeech;

    LinearLayout btnTextToSpeech, btnSpeechToText, btnCopy, btnDelete, btnDownload, btnUpload;
    ImageView btnSpeak, btnSpeaker, deleteText, copyText;
    EditText edTextSpeech;
    String ID_BahasaIndonesia = "id", mText, filename = "", filepath = "", mTextSave;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_page);

        //Speech to text
        edTextSpeech = findViewById(R.id.edTextSpeech);
        btnSpeak = findViewById(R.id.btnSpeak);
        deleteText = findViewById(R.id.btnDeleteSpeech);
        copyText = findViewById(R.id.btnCopySpeech);

        //Copy
        btnCopy = findViewById(R.id.btnCopyLinear);
        btnCopy.setVisibility(View.INVISIBLE);

        //Delete
        btnDelete = findViewById(R.id.btnDeleteLinear);
        btnDelete.setVisibility(View.INVISIBLE);

        //Download
        btnDownload = findViewById(R.id.btnDownloadLinear);
        btnDownload.setVisibility(View.INVISIBLE);

        //Upload
        btnUpload = findViewById(R.id.btnUploadLinear);

        //CustomSaveLayout
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_save_layout);
        LinearLayout btnSave = dialog.findViewById(R.id.btnSave);
        LinearLayout btnCancelSave = dialog.findViewById(R.id.btnCancelSave);
        EditText edTextFile = dialog.findViewById(R.id.edTextFile);

        btnChooseSpeech = findViewById(R.id.choose_file_speech);
        descSpeech = findViewById(R.id.descSpeech);

        btnSpeechToText = findViewById(R.id.btnSpeechToText);
        btnTextToSpeech = findViewById(R.id.btnTextToSpeech);

        tvSpeechToText = findViewById(R.id.tvSpeechToText);
        tvTextToSpeech = findViewById(R.id.tvTextToSpeech);

        btnSpeaker = findViewById(R.id.btnSpeaker);

        // Initialize two String variables for storing filename and filepath
        filename = ".txt";
        filepath = "MyFileText";

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int lang = textToSpeech.setLanguage(Locale.forLanguageTag(ID_BahasaIndonesia));
            }
        });

        btnCancelSave.setOnClickListener(v -> dialog.dismiss());

        edTextSpeech.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    btnCopy.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);
                    btnDownload.setVisibility(View.GONE);
                } else {
                    btnCopy.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDownload.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSave.setOnClickListener(v -> {

            mText = edTextSpeech.getText().toString().trim();
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
                        edTextSpeech.setText("");
                        dialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mTextSave.isEmpty();
                    // If the Text field is empty show corresponding Toast message
                    Toast.makeText(SpeechPage.this, "File name can not be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDownload.setOnClickListener(v -> dialog.show());

        btnUpload.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                callFileTxtFromDevice();
            }
        });

        copyText.setOnClickListener(v -> {

            String text = edTextSpeech.getText().toString();

            if (text.isEmpty()) {

                Toast.makeText(SpeechPage.this, "There is Empty", Toast.LENGTH_SHORT).show();

            } else {

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Data", edTextSpeech.getText().toString());
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(SpeechPage.this, "Text is Copy", Toast.LENGTH_SHORT).show();

            }

        });

        deleteText.setOnClickListener(v -> {

            String text = edTextSpeech.getText().toString();

            if (text.isEmpty()) {

                Toast.makeText(SpeechPage.this, "Text is Empty", Toast.LENGTH_SHORT).show();
            } else {

                edTextSpeech.setText("");
                btnCopy.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);
                btnDownload.setVisibility(View.INVISIBLE);
            }

        });

        btnSpeaker.setOnClickListener(v -> {
            String text = edTextSpeech.getText().toString();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            btnCopy.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnDownload.setVisibility(View.VISIBLE);
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_STORAGE);
                } else {
                    Intent mic_google = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    mic_google.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    mic_google.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ID_BahasaIndonesia);

                    try {
                        startActivityForResult(mic_google, RESULT_SPEECH);
                        edTextSpeech.setText("");
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Maaf, Device Kamu Tidak Support Speech To Text", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edTextSpeech.setText(text.get(0));
                    btnCopy.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDownload.setVisibility(View.VISIBLE);
                }
                break;
        }
        if (requestCode == CHOOSE_FROM_DEVICE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                String text = readTextFromUri(uri);
                if (text != null) {
                    edTextSpeech.setText(text);
                } else {
                    Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void btnTextToSpeech(View view) {
        String string_head = getString(R.string.text_speech);
        String string_desc = getString(R.string.text_speech_desc);
        Drawable drawableSpeech = getResources().getDrawable(btn_choose_image);
        Drawable drawableText = getResources().getDrawable(btn_speech);

        btnChooseSpeech.setText(string_head);
        descSpeech.setText(string_desc);
        btnSpeechToText.setBackground(drawableSpeech);
        btnTextToSpeech.setBackground(drawableText);
        tvTextToSpeech.setTextColor(getColor(R.color.white));
        tvSpeechToText.setTextColor(getColor(R.color.text_grey_speech));
        btnSpeaker.setVisibility(View.VISIBLE);
        btnSpeak.setVisibility(View.INVISIBLE);
    }

    public void btnSpeechToText(View view) {
        String string_head = getString(R.string.speech_text);
        String string_desc = getString(R.string.speech_text_desc);
        Drawable drawableText = getResources().getDrawable(btn_choose_image);
        Drawable drawableSpeech = getResources().getDrawable(btn_speech);

        btnChooseSpeech.setText(string_head);
        descSpeech.setText(string_desc);
        btnSpeechToText.setBackground(drawableSpeech);
        btnTextToSpeech.setBackground(drawableText);
        tvTextToSpeech.setTextColor(getColor(R.color.text_grey_speech));
        tvSpeechToText.setTextColor(getColor(R.color.white));
        btnSpeaker.setVisibility(View.INVISIBLE);
        btnSpeak.setVisibility(View.VISIBLE);
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

    //read content the file
    private String readTextFromUri(Uri uri) {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line);
                    text.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return text.toString();
    }

    private void callFileTxtFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, CHOOSE_FROM_DEVICE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No file manager found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void btnBackSpeech(View view) {

        Intent intent = new Intent(SpeechPage.this, HomePage.class);
        startActivity(intent);
    }
}


