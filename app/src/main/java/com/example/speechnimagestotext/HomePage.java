package com.example.speechnimagestotext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    ArrayList<Integer> images = new ArrayList<>();

    ViewPager viewPagerHome;
    ViewPagerAdapter adapter;

    LinearLayout layoutSlide;

    private int[] layouts;

    private TextView[] dots;

    private static final int TIME_INTERVAL = 2000;

    private long backPressedTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        images.add(R.drawable.frame_home_one);
        images.add(R.drawable.frame_home_two);
        images.add(R.drawable.frame_home_three);

//        layouts = new int[images.size()];

        viewPagerHome = findViewById(R.id.viewPagerHome);

        adapter = new ViewPagerAdapter(this, images);
        viewPagerHome.setAdapter(adapter);

        layoutSlide = findViewById(R.id.sliderDots);

        // tombol dots (lingkaran kecil perpindahan slide)
        addBottomDots(0);

        viewPagerHome.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[images.size()];
        layoutSlide.removeAllViews();

        for (int i = 0; i < images.size(); i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(50);
            dots[i].setTextColor(getResources().getColor(R.color.inactiveDotColor));
            layoutSlide.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.activeDotColor));
    }

    public void btnImageToText(View view) {
        Intent intent = new Intent(HomePage.this, ImagePage.class);
        startActivity(intent);
    }

    public void btnSpeechToText(View view) {
        Intent intent = new Intent(HomePage.this, SpeechPage.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}